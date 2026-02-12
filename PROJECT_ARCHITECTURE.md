# KnowledgeHub - Project Architecture Documentation

> **AI Context Document** - This document provides a comprehensive overview of the KnowledgeHub microservices architecture for AI assistants and developers.

## 📋 Project Overview

**KnowledgeHub** is an educational resource management platform built with a microservices architecture. It manages students, teachers, and academic resources (PFA - Projet de Fin d'Année, PFE - Projet de Fin d'Études, Stage reports) with centralized authentication via JWT tokens.

### Technology Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.2.1, Spring Cloud 2023.0.0 |
| **Java Version** | Java 21 |
| **API Gateway** | Spring Cloud Gateway |
| **Database** | MySQL (auth, user, resource services), MongoDB (notification service) |
| **Authentication** | JWT (JSON Web Tokens) |
| **AI/Search Service** | Python FastAPI |
| **Build Tool** | Maven (multi-module) |
| **Documentation** | SpringDoc OpenAPI (Swagger) |
| **Utilities** | Lombok, MapStruct |

---

## 🏗️ Architecture Diagram

```
                                    ┌─────────────────────┐
                                    │    Client Apps      │
                                    │  (Web/Mobile/API)   │
                                    └──────────┬──────────┘
                                               │
                                               ▼
                                    ┌─────────────────────┐
                                    │    API Gateway      │
                                    │     (Port 8080)     │
                                    │  - JWT Validation   │
                                    │  - Route Requests   │
                                    └──────────┬──────────┘
                                               │
           ┌───────────────┬───────────────┬───┴───┬───────────────┬───────────────┐
           ▼               ▼               ▼       ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │Auth Service │ │User Service │ │  Resource   │ │Notification │ │ AI & Search │
    │ (Port 8081) │ │ (Port 8082) │ │   Service   │ │   Service   │ │   Service   │
    │             │ │             │ │ (Port 8083) │ │ (Port 8084) │ │ (Port 8000) │
    │   MySQL     │ │   MySQL     │ │   MySQL     │ │  MongoDB    │ │   Python    │
    └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
           │               │               │               │
           ▼               ▼               ▼               ▼
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                        Inter-Service Communication                          │
    │              (OpenFeign Clients, REST Template, HTTP Calls)                 │
    └─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Services Overview

### 1. API Gateway (`api-gateway`)
- **Port:** 8080
- **Purpose:** Central entry point for all client requests
- **Responsibilities:**
  - Route requests to appropriate microservices
  - JWT token validation via `JwtAuthenticationFilter`
  - Add authentication headers (`X-Auth-Id`, `X-User-Role`, `X-User-Email`) to downstream requests
  - Path rewriting for service routing

**Route Configuration:**
| Route Path | Target Service | Target Port |
|------------|----------------|-------------|
| `/auth-service/**` | auth-service | 8081 |
| `/user-service/**` | user-service | 8082 |
| `/ressource-service/**` | resource-service | 8083 |
| `/notification-service/**` | notification-service | 8084 |
| `/ai-service/**` | ai-and-search-service | 8000 |

---

### 2. Auth Service (`auth-service`)
- **Port:** 8081
- **Database:** MySQL (`knowledgehub_auth`)
- **Purpose:** Centralized authentication and authorization

**Key Features:**
- User registration (Admin only)
- Login with JWT token generation
- Password management (reset, change)
- Token validation
- Bulk user creation via CSV upload

**Main Entities:**
```java
AuthUser {
    Long id
    String email (username)
    String password (BCrypt hashed)
    Role role (STUDENT | TEACHER | ADMIN)
    Boolean enabled
    Boolean accountNonLocked
    Integer failedLoginAttempts
    LocalDateTime lastLogin
    LocalDateTime passwordChangedAt
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

---

### 3. User Service (`user-service`)
- **Port:** 8082
- **Database:** MySQL (`knowledgehub_user`)
- **Purpose:** Manage user profiles and class assignments

**Key Features:**
- Student and Teacher profile management
- Class (Classe) management
- Student-to-class assignment

**Main Entities:**
```java
// Base class for all users
UserBase {
    String authId           // Links to auth-service
    String email
    String nom
    String prenom
}

Student extends UserBase {
    Long studentId
    Classe classe           // Many-to-One relationship
}

Teacher extends UserBase {
    Long teacherId
}

Classe {
    Long classeId
    String classeName
    List<Student> students  // One-to-Many relationship
}
```

---

### 4. Resource Service (`resource-service`)
- **Port:** 8083
- **Database:** MySQL (`knowledgehub_ressource`)
- **Purpose:** Manage academic documents and student submissions

**Key Features:**
- Teacher document uploads (tutorials, lecture notes, etc.)
- Student submissions (PFA, PFE, Stage reports)
- File management with extracted text for search
- Review workflow for submissions

**Main Entities:**
```java
Document {
    Long id
    String title
    String description
    DocumentCategory category
    String subject
    Long teacherId
    String teacherName
    String teacherEmail
    File file
    List<String> keywords
    Boolean isPublished
}

PFA {  // Projet de Fin d'Année
    Long id
    String title
    Integer year
    SubmissionStatus status
    Long studentId
    String studentName
    String studentEmail
    Long supervisorId
    String supervisorName
    File reportFile
    Date submissionDate
    String supervisorFeedback
}

PFE {  // Projet de Fin d'Études
    // Similar structure to PFA
}

Stage {  // Internship
    // Similar structure with additional fields
}

File {
    Long id
    String fileName
    String originalFileName
    Boolean isPublished
    Long fileSize
    String storagePath
    Date uploadDate
    Long uploadedByUserId
    String uploadedByUserName
    Integer pageCount
    String extractedText     // For search functionality
    List<String> keywords
}
```

**Enums:**
```java
DocumentCategory {
    TUTORIAL, LECTURE_NOTES, GUIDE, RESEARCH_PAPER,
    REFERENCE_MANUAL, CASE_STUDY, EXERCISE_SHEET,
    SOLUTION_MANUAL, PRESENTATION, OTHER
}

SubmissionStatus {
    SUBMITTED, UNDER_REVIEW, REJECTED, APPROVED
}

FileType {
    REPORT, JOURNAL, ATTESTATION,
    EDUCATIONAL_MATERIAL, SUPPLEMENTARY
}
```

---

### 5. Notification Service (`notification-service`)
- **Port:** 8084
- **Database:** MongoDB (`notification_db`)
- **Purpose:** Handle email and push notifications

**Key Features:**
- Email notifications via SMTP (Gmail)
- Push notifications
- Notification tracking (delivered, read status)
- Retry mechanism for failed deliveries

**Main Entity:**
```java
Notification {
    String id              // MongoDB ObjectId
    String userId
    String content
    NotificationType type  // EMAIL | PUSH
    String subject
    String recipientEmail
    String deviceToken
    boolean delivered
    boolean read
    LocalDateTime createdAt
    LocalDateTime deliveredAt
    LocalDateTime readAt
    boolean emailSent
    int retryCount
    LocalDateTime lastRetryAt
}
```

---

### 6. Notification Client (`notification-client`)
- **Type:** Shared library (not a standalone service)
- **Purpose:** Reusable Feign client for other services to send notifications

---

### 7. AI and Search Service (`ai-and-search-service`)
- **Port:** 8000
- **Framework:** Python FastAPI
- **Purpose:** AI-powered search and recommendations

**Key Features:**
- Semantic search across documents
- AI-powered recommendations
- Health check endpoints

**Note:** Routers for `/ai/**` and `/search/**` are currently placeholders (not yet implemented).

---

## 🔐 Security & Authentication Flow

### JWT Token Flow:
1. Client sends credentials to `/auth-service/api/public/login`
2. Auth service validates credentials and returns JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. API Gateway validates token via `JwtAuthenticationFilter`
5. Gateway adds headers to downstream requests:
   - `X-Auth-Id`: User's auth service ID
   - `X-User-Role`: STUDENT | TEACHER | ADMIN
   - `X-User-Email`: User's email

### Role-Based Access:
| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access, user registration, bulk uploads, class management |
| **TEACHER** | Upload documents, review student submissions |
| **STUDENT** | Submit PFA/PFE/Stage, view library resources |

---

## 🔄 Inter-Service Communication

### OpenFeign Clients:
- `UserServiceClient` (in resource-service): Fetches student/class info from user-service

### REST Template / HTTP:
- Notification requests from auth-service to notification-service
- Services communicate via internal URLs (localhost in development)

---

## 📁 Project Structure

```
KnowledgeHub/
├── pom.xml                          # Parent POM (multi-module)
├── api-gateway/
│   └── src/main/java/tn/esprim/knowledgehub/
│       ├── config/                  # Gateway configuration
│       └── filter/                  # JwtAuthenticationFilter
├── auth-service/
│   └── src/main/java/tn/esprim/knowledgehub/auth/
│       ├── controller/              # AuthController, BulkUserController
│       ├── dto/                     # LoginRequest, RegisterRequest, AuthResponse
│       ├── entity/                  # AuthUser
│       ├── repository/
│       └── service/                 # AuthService, BulkUserService
├── user-service/
│   └── src/main/java/tn/esprim/knowledgehub/user/
│       ├── controller/              # StudentController, TeacherController, AdminController
│       ├── dto/request/             # CreateStudentRequest, StudentToClassRequest
│       ├── dto/response/            # ApiResponse, StudentDTO
│       ├── entity/                  # Student, Teacher, Classe, UserBase
│       ├── repository/
│       └── service/
├── resource-service/
│   └── src/main/java/tn/esprim/knowledgehub/resource/
│       ├── client/                  # UserServiceClient (Feign)
│       ├── controller/
│       │   ├── admin/               # AdminDocumentController
│       │   ├── library/             # LibraryController
│       │   ├── student/             # StudentPFA/PFE/StageController
│       │   └── teacher/             # TeacherDocument/ReviewController
│       ├── dto/
│       ├── entity/                  # Document, PFA, PFE, Stage, File
│       ├── enums/                   # DocumentCategory, SubmissionStatus, FileType
│       ├── repository/
│       └── service/
├── notification-service/
│   └── src/main/java/tn/esprim/knowledgehub/notification/
│       ├── config/
│       ├── controller/              # NotificationController
│       ├── dto/request/             # NotificationRequest
│       ├── dto/response/            # NotificationResponse
│       ├── entity/                  # Notification, NotificationType
│       ├── repository/
│       ├── scheduler/               # Retry jobs
│       └── service/
├── notification-client/             # Shared Feign client library
└── ai-and-search-service/           # Python FastAPI service
    ├── main.py
    ├── requirements.txt
    └── app/
        └── routers/                 # ai_router, search_router (TODO)
```

---

## 🗄️ Database Schema Overview

### knowledgehub_auth (MySQL)
- `auth_users` - Authentication credentials and roles

### knowledgehub_user (MySQL)
- `student` - Student profiles
- `teacher` - Teacher profiles
- `classe` - Class definitions

### knowledgehub_ressource (MySQL)
- `teacher_documents` - Teacher-uploaded documents
- `pfa_submissions` - PFA submissions
- `pfe_submissions` - PFE submissions
- `stage_submissions` - Internship submissions
- `files` - File metadata and storage info

### notification_db (MongoDB)
- `notifications` - Notification documents

---

## 🚀 Development Setup

### Prerequisites:
- Java 21
- Maven 3.8+
- MySQL 8.0+
- MongoDB 6.0+
- Python 3.10+ (for AI service)

### Running Services:
```bash
# Start MinIO (using Docker)
docker run -d --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  quay.io/minio/minio server /data --console-address ":9001"

# Build all services
mvn clean install

# Run each service (in separate terminals)
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd resource-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# Run Python AI service
cd ai-and-search-service
pip install -r requirements.txt
python main.py
```

### API Documentation:
Each service exposes Swagger UI at:
- Auth: http://localhost:8081/swagger-ui.html
- User: http://localhost:8082/swagger-ui.html
- Resource: http://localhost:8083/swagger-ui.html
- Notification: http://localhost:8084/swagger-ui.html
- AI Service: http://localhost:8000/docs (FastAPI auto-docs)

---

## 📝 Notes for AI Assistants

### Current State:
1. **Fully Implemented:** Auth, User, Notification services
2. **Partially Implemented:** Resource service (controllers have placeholder endpoints)
3. **Placeholder:** AI & Search service (routers not yet implemented)

### Key Patterns:
- All services follow standard Spring Boot layered architecture (Controller → Service → Repository)
- DTOs separate API contracts from entities
- Generic `ApiResponse<T>` wrapper for consistent responses
- Header-based auth context propagation (`X-Auth-Id`, `X-User-Role`)

### Common Tasks:
- Adding endpoints: Create in controller, implement in service
- New entity: Add entity, repository, service, DTO, controller
- Inter-service call: Use Feign client or RestTemplate
