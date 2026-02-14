# KnowledgeHub - API Endpoints Documentation

> **Complete API Reference** - This document contains all 50 REST API endpoints with detailed request/response structures for each microservice.

## Table of Contents
1. [Auth Service Endpoints](#auth-service-endpoints)
2. [User Service Endpoints](#user-service-endpoints)
3. [Resource Service Endpoints](#resource-service-endpoints)
4. [Notification Service Endpoints](#notification-service-endpoints)
5. [AI & Search Service Endpoints](#ai--search-service-endpoints)

---

## Auth Service Endpoints

**Base URL:** `http://localhost:8080/auth-service` (via Gateway) or `http://localhost:8081` (direct)

### 1. Login

**Endpoint:** `POST /api/public/login`  
**Access:** Public  
**Description:** Authenticates user credentials and returns JWT token

**Request:**
```json
{
  "username": "student@example.com",
  "password": "password123"
}
```

**Request Structure:**
```java
LoginRequest {
  String username   // Can be username or email
  String password
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "status": "OK",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "student@example.com",
  "email": "student@example.com",
  "role": "STUDENT",
  "authId": 1,
  "userId": 10
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Invalid credentials",
  "status": "UNAUTHORIZED"
}
```

**Response Structure:**
```java
AuthResponse<T> {
  boolean success
  String message
  HttpStatus status
  T data
  String error
  String token
  String username
  String email
  Role role              // STUDENT | TEACHER | ADMIN
  Long authId            // ID from auth-service
  Long userId            // ID from user-service (studentId or teacherId)
}
```

---

### 2. Register User

**Endpoint:** `POST /api/register`  
**Access:** Admin only  
**Headers Required:** `X-User-Role: ADMIN`  
**Description:** Creates a new user account

**Request:**
```json
{
  "email": "newstudent@example.com",
  "password": "SecurePass123",
  "prenom": "John",
  "nom": "Doe",
  "role": "STUDENT"
}
```

**Request Structure:**
```java
RegisterRequest {
  @NotBlank @Email String email
  @NotBlank @Size(min=8) String password
  @NotBlank String prenom
  @NotBlank String nom
  Role role = STUDENT    // Default: STUDENT
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "status": "OK",
  "data": {
    "id": 15,
    "email": "newstudent@example.com",
    "role": "STUDENT",
    "enabled": true,
    "accountNonLocked": true,
    "createdAt": "2026-02-12T10:30:00"
  }
}
```

**Response (Error - Not Admin):**
```json
{
  "success": false,
  "error": "Only admins can register users",
  "status": "FORBIDDEN"
}
```

---

### 3. Update Password

**Endpoint:** `PUT /api/edit-password`  
**Access:** Authenticated users  
**Headers Required:** `X-Auth-Id: {userId}`  
**Description:** Updates the password for authenticated user

**Request:**
```json
{
  "password": "NewSecurePassword123"
}
```

**Request Structure:**
```java
AuthRequest {
  String username    // Optional
  String password
}
```

**Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "status": "OK",
  "data": {
    "id": 1,
    "email": "student@example.com",
    "role": "STUDENT",
    "passwordChangedAt": "2026-02-12T11:00:00"
  }
}
```

---

### 4. Request Password Reset

**Endpoint:** `POST /api/public/reset-password`  
**Access:** Public  
**Description:** Sends password reset link to email

**Request:**
```json
{
  "email": "student@example.com"
}
```

**Request Structure:**
```java
CreateResetRequest {
  String email
}
```

**Response:**
```json
{
  "success": true,
  "message": "If the email exists, a reset link has been sent",
  "status": "OK"
}
```

---

### 5. Reset Password with Token

**Endpoint:** `POST /api/public/reset-password/{token}`  
**Access:** Public  
**Description:** Resets password using reset token

**Request:**
```json
{
  "newPassword": "BrandNewPassword123",
  "confirmPassword": "BrandNewPassword123"
}
```

**Request Structure:**
```java
ResetRequest {
  String newPassword
  String confirmPassword
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password reset successfully",
  "status": "OK"
}
```

---

### 6. Validate Token

**Endpoint:** `POST /api/validate`  
**Access:** Public (requires token)  
**Headers Required:** `Authorization: Bearer {token}`  
**Description:** Validates JWT token and returns user info

**Response:**
```json
{
  "valid": true,
  "userId": 1,
  "email": "student@example.com",
  "role": "STUDENT"
}
```

**Response Structure:**
```java
TokenValidationResponse {
  boolean valid
  Long userId
  String email
  Role role
}
```

---

### 7. Bulk Upload Students

**Endpoint:** `POST /api/students/upload`  
**Access:** Admin only  
**Headers Required:** `X-User-Role: ADMIN`  
**Content-Type:** `multipart/form-data`  
**Description:** Bulk creates student accounts from CSV file

**Request:**
- Form parameter: `file` (CSV file)
- CSV Format: `email,password,prenom,nom`

**Response:**
```json
{
  "success": true,
  "message": "Users created and notifications sent successfully",
  "status": "OK",
  "data": [
    {
      "email": "student1@example.com",
      "success": true,
      "message": "User created successfully",
      "authId": 20,
      "userId": 15
    },
    {
      "email": "student2@example.com",
      "success": false,
      "message": "Email already exists"
    }
  ]
}
```

**Response Structure:**
```java
AuthResponse<List<BulkUserCreationResult>> {
  // ... standard AuthResponse fields
  List<BulkUserCreationResult> data
}

BulkUserCreationResult {
  String email
  boolean success
  String message
  Long authId
  Long userId
}
```

---

### 8. Bulk Upload Teachers

**Endpoint:** `POST /api/teachers/upload`  
**Access:** Admin only  
**Headers Required:** `X-User-Role: ADMIN`  
**Content-Type:** `multipart/form-data`  
**Description:** Bulk creates teacher accounts from CSV file

**Request:** Same as student upload  
**Response:** Same structure as student upload

---

## User Service Endpoints

**Base URL:** `http://localhost:8080/user-service` (via Gateway) or `http://localhost:8082` (direct)

### 1. Create Student

**Endpoint:** `POST /api/students/create`  
**Access:** Admin/System  
**Description:** Creates a new student profile

**Request:**
```json
{
  "authId": "5",
  "email": "student@example.com",
  "prenom": "John",
  "nom": "Doe"
}
```

**Request Structure:**
```java
CreateStudentRequest {
  String authId
  Long StudentId        // Optional
  String email
  String prenom
  String nom
}
```

**Response:**
```json
{
  "success": true,
  "message": "Student created successfully",
  "data": {
    "studentId": 10,
    "authId": "5",
    "email": "student@example.com",
    "nom": "Doe",
    "prenom": "John",
    "classe": null
  }
}
```

**Response Structure:**
```java
ApiResponse<Student> {
  String message
  boolean success
  Student data
}

Student {
  Long studentId
  String authId
  String email
  String nom
  String prenom
  Classe classe
}
```

---

### 2. Create Multiple Students

**Endpoint:** `POST /api/students/create-multiple`  
**Access:** Admin/System  
**Description:** Creates multiple student profiles in batch

**Request:**
```json
[
  {
    "authId": "5",
    "email": "student1@example.com",
    "prenom": "John",
    "nom": "Doe"
  },
  {
    "authId": "6",
    "email": "student2@example.com",
    "prenom": "Jane",
    "nom": "Smith"
  }
]
```

**Request Structure:**
```java
List<CreateStudentRequest>
```

**Response:**
```json
{
  "success": true,
  "message": "Student created successfully",
  "data": [
    {
      "studentId": 10,
      "authId": "5",
      "email": "student1@example.com",
      "nom": "Doe",
      "prenom": "John",
      "classe": null
    },
    {
      "studentId": 11,
      "authId": "6",
      "email": "student2@example.com",
      "nom": "Smith",
      "prenom": "Jane",
      "classe": null
    }
  ]
}
```

---

### 3. Get All Students

**Endpoint:** `GET /api/students/all`  
**Access:** Admin/Teacher  
**Description:** Retrieves all students

**Response:**
```json
{
  "success": true,
  "message": "All students retrieved successfully",
  "data": [
    {
      "studentId": 10,
      "authId": "5",
      "email": "student@example.com",
      "nom": "Doe",
      "prenom": "John",
      "classe": {
        "classeId": 1,
        "classeName": "3A-INFO"
      }
    }
  ]
}
```

---

### 4. Get Student by Auth ID

**Endpoint:** `GET /api/students/by-auth-id/{authId}`  
**Access:** Authenticated  
**Description:** Retrieves student by auth service ID

**Response:**
```json
{
  "success": true,
  "message": "Student retrieved successfully",
  "data": {
    "studentId": 10,
    "authId": "5",
    "email": "student@example.com",
    "nom": "Doe",
    "prenom": "John",
    "classe": {
      "classeId": 1,
      "classeName": "3A-INFO"
    }
  }
}
```

---

### 5. Get Student by ID

**Endpoint:** `GET /api/students/by-id/{id}`  
**Access:** Authenticated  
**Description:** Retrieves student by student ID

**Response:**
```json
{
  "success": true,
  "message": "Student retrieved successfully",
  "data": {
    "studentId": 10,
    "authId": "5",
    "email": "student@example.com",
    "nom": "Doe",
    "prenom": "John",
    "classe": {
      "classeId": 1,
      "classeName": "3A-INFO"
    }
  }
}
```

**Response Structure:**
```java
ApiResponse<StudentDTO> {
  String message
  boolean success
  StudentDTO data
}

StudentDTO {
  Long studentId
  String authId
  String email
  String nom
  String prenom
  ClasseBasicDTO classe
}

ClasseBasicDTO {
  Long classeId
  String classeName
}
```

---

### 6. Get Student by Email

**Endpoint:** `GET /api/students/by-email/{email}`  
**Access:** Authenticated  
**Description:** Retrieves student by email address

**Response:** Same as Get Student by ID

---

### 7. Create Teacher

**Endpoint:** `POST /api/teachers/create`  
**Access:** Admin/System  
**Description:** Creates a new teacher profile

**Request:**
```json
{
  "authId": "8",
  "email": "teacher@example.com",
  "prenom": "Marie",
  "nom": "Dupont"
}
```

**Request Structure:**
```java
CreateStudentRequest {  // Reused for teachers
  String authId
  Long StudentId
  String email
  String prenom
  String nom
}
```

**Response:**
```json
{
  "success": true,
  "message": "Teachers created successfully",
  "data": {
    "teacherId": 5,
    "authId": "8",
    "email": "teacher@example.com",
    "nom": "Dupont",
    "prenom": "Marie"
  }
}
```

**Response Structure:**
```java
ApiResponse<Teacher> {
  String message
  boolean success
  Teacher data
}

Teacher {
  Long teacherId
  String authId
  String email
  String nom
  String prenom
}
```

---

### 8. Create Multiple Teachers

**Endpoint:** `POST /api/teachers/create-multiple`  
**Access:** Admin/System  
**Description:** Creates multiple teacher profiles in batch

**Request:** Array of CreateStudentRequest (same as students)  
**Response:** Array of Teacher objects

---

### 9. Get All Teachers

**Endpoint:** `GET /api/teachers/all`  
**Access:** Admin  
**Description:** Retrieves all teachers

**Response:**
```json
{
  "success": true,
  "message": "All teachers retrieved successfully",
  "data": [
    {
      "teacherId": 5,
      "authId": "8",
      "email": "teacher@example.com",
      "nom": "Dupont",
      "prenom": "Marie"
    }
  ]
}
```

---

### 10. Get Teacher by Auth ID

**Endpoint:** `GET /api/teachers/by-auth-id/{authId}`  
**Access:** Authenticated  
**Description:** Retrieves teacher by auth service ID

**Response:**
```json
{
  "success": true,
  "message": "Teacher retrieved successfully",
  "data": {
    "teacherId": 5,
    "authId": "8",
    "email": "teacher@example.com",
    "nom": "Dupont",
    "prenom": "Marie"
  }
}
```

---

### 11. Get Teacher by Email

**Endpoint:** `GET /api/teachers/by-email/{email}`  
**Access:** Authenticated  
**Description:** Retrieves teacher by email address

**Response:** Same as Get Teacher by Auth ID

---

### 12. Get All Classes

**Endpoint:** `GET /api/admin/classe/`  
**Access:** Admin  
**Description:** Retrieves all classes

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "classeId": 1,
      "classeName": "3A-INFO",
      "students": [
        {
          "studentId": 10,
          "authId": "5",
          "email": "student@example.com",
          "nom": "Doe",
          "prenom": "John"
        }
      ]
    }
  ]
}
```

**Response Structure:**
```java
ApiResponse<List<Classe>> {
  String message
  boolean success
  List<Classe> data
}

Classe {
  Long classeId
  String classeName
  List<Student> students
}
```

---

### 13. Get Class by ID

**Endpoint:** `GET /api/admin/classe/{id}`  
**Access:** Admin  
**Description:** Retrieves class with all students

**Response:**
```json
{
  "success": true,
  "data": {
    "classeId": 1,
    "classeName": "3A-INFO",
    "students": [
      {
        "studentId": 10,
        "authId": "5",
        "email": "student@example.com",
        "nom": "Doe",
        "prenom": "John"
      }
    ]
  }
}
```

---

### 14. Add Student to Class

**Endpoint:** `POST /api/admin/add-student-classe`  
**Access:** Admin only  
**Headers Required:** `X-User-Role: ADMIN`  
**Description:** Assigns a student to a class

**Request:**
```json
{
  "classId": 1,
  "studentId": 10
}
```

**Request Structure:**
```java
StudentToClassRequest {
  Long classId
  Long studentId
}
```

**Response:**
```json
{
  "success": true,
  "message": "Student added to classe successfully",
  "data": {
    "studentId": 10,
    "authId": "5",
    "email": "student@example.com",
    "nom": "Doe",
    "prenom": "John",
    "classe": {
      "classeId": 1,
      "classeName": "3A-INFO"
    }
  }
}
```

---

### 15. Add Multiple Students to Classes

**Endpoint:** `POST /api/admin/add-students-classes`  
**Access:** Admin only  
**Headers Required:** `X-User-Role: ADMIN`  
**Description:** Assigns multiple students to their respective classes

**Request:**
```json
[
  {
    "classId": 1,
    "studentId": 10
  },
  {
    "classId": 1,
    "studentId": 11
  },
  {
    "classId": 2,
    "studentId": 12
  }
]
```

**Request Structure:**
```java
List<StudentToClassRequest>
```

**Response (All Success):**
```json
{
  "success": true,
  "message": "all students were added to their classe successfully",
  "data": [
    {
      "studentId": 10,
      "authId": "5",
      "email": "student1@example.com",
      "nom": "Doe",
      "prenom": "John",
      "classe": {
        "classeId": 1,
        "classeName": "3A-INFO"
      }
    },
    {
      "studentId": 11,
      "authId": "6",
      "email": "student2@example.com",
      "nom": "Smith",
      "prenom": "Jane",
      "classe": {
        "classeId": 1,
        "classeName": "3A-INFO"
      }
    }
  ]
}
```

**Response (Partial Success):**
```json
{
  "success": false,
  "message": "some students were not added to classe: [Student not found: 999, Class not found: 888]",
  "data": [
    {
      "studentId": 10,
      "authId": "5",
      "email": "student1@example.com",
      "nom": "Doe",
      "prenom": "John",
      "classe": {
        "classeId": 1,
        "classeName": "3A-INFO"
      }
    }
  ]
}
```

---

## Resource Service Endpoints

**Base URL:** `http://localhost:8080/ressource-service` (via Gateway) or `http://localhost:8083` (direct)

### Currently Implemented Endpoints

### 1. Test Feign Client - Get All Classes

**Endpoint:** `GET /api/student/pfa/public/test`  
**Access:** Public (test endpoint)  
**Description:** Tests Feign client by fetching all classes from user-service

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "classeId": 1,
      "classeName": "3A-INFO",
      "students": []
    }
  ]
}
```

---

### 2. Test Feign Client - Get Class by ID

**Endpoint:** `GET /api/student/pfa/public/test/{id}`  
**Access:** Public (test endpoint)  
**Description:** Tests Feign client by fetching class by ID

**Response:**
```json
{
  "success": true,
  "data": {
    "classeId": 1,
    "classeName": "3A-INFO",
    "students": [
      {
        "studentId": 10,
        "authId": "5",
        "email": "student@example.com",
        "nom": "Doe",
        "prenom": "John"
      }
    ]
  }
}
```

---

### 3. Test Feign Client - Get Student by ID

**Endpoint:** `GET /api/student/pfa/public/test/student/{id}`  
**Access:** Public (test endpoint)  
**Description:** Tests Feign client by fetching student by ID

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 10,
    "authId": "5",
    "email": "student@example.com",
    "nom": "Doe",
    "prenom": "John",
    "classe": {
      "classeId": 1,
      "classeName": "3A-INFO"
    }
  }
}
```

---

### File Management Endpoints

The resource service includes a comprehensive file management system using MinIO for object storage.

#### 1. Upload File

**Endpoint:** `POST /api/files/upload`  
**Access:** Authenticated users  
**Content-Type:** `multipart/form-data`  
**Headers Required:** `X-Auth-Id`, `X-User-Email`  
**Description:** Uploads a file to MinIO storage and saves metadata

**Request Parameters:**
- `file`: File to upload (multipart)
- `fileType`: File type enum
- `bucketType`: Bucket type (`documents`, `pfa`, `pfe`, `stage`)

**Request Example (cURL):**
```bash
curl -X POST http://localhost:8083/api/files/upload \
  -H "X-Auth-Id: 1" \
  -H "X-User-Email: teacher@example.com" \
  -F "file=@/path/to/document.pdf" \
  -F "fileType=EDUCATIONAL_MATERIAL" \
  -F "bucketType=documents"
```

**Response:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileId": 1,
    "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "fileSize": 1048576,
    "contentType": "application/pdf",
    "storagePath": "knowledgehub-documents/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "fileType": "EDUCATIONAL_MATERIAL",
    "uploadDate": "2026-02-12T10:30:00",
    "uploadedByUserId": 1,
    "uploadedByUserName": "teacher@example.com",
    "pageCount": 25,
    "isPublished": false,
    "keywords": [],
    "message": "File uploaded successfully"
  }
}
```

**Response Structure:**
```java
ApiResponse<FileUploadResponse> {
  boolean success
  String message
  FileUploadResponse data
  String error
}

FileUploadResponse {
  Long fileId
  String fileName              // Generated UUID filename
  String originalFileName      // Original uploaded filename
  String title                 // File title (optional)
  String description           // File description (optional)
  Long fileSize               // File size in bytes
  String contentType          // MIME type
  String storagePath          // MinIO storage path
  FileType fileType           // REPORT | EDUCATIONAL_MATERIAL | etc.
  Date uploadDate
  Long uploadedByUserId
  String uploadedByUserName
  Integer pageCount           // For PDF files
  Boolean isPublished
  List<String> keywords
  String message
}
```

---

#### 2. Download File

**Endpoint:** `GET /api/files/download/{fileId}`  
**Access:** Authenticated users  
**Description:** Downloads a file from MinIO storage

**Response:** Binary file content with appropriate headers

**Example:**
```bash
curl -X GET http://localhost:8083/api/files/download/1 \
  -H "Authorization: Bearer {token}" \
  -o downloaded_file.pdf
```

**Response Headers:**
```
Content-Disposition: attachment; filename="document.pdf"
Content-Type: application/pdf
Content-Length: 1048576
```

---

#### 3. Get Presigned URL

**Endpoint:** `GET /api/files/{fileId}/presigned-url?expiryMinutes=60`  
**Access:** Authenticated users  
**Description:** Generates a temporary download URL (valid for specified duration)

**Query Parameters:**
- `expiryMinutes` (optional): URL expiry time in minutes (default: 60)

**Response:**
```json
{
  "success": true,
  "message": "Presigned URL generated successfully",
  "data": "http://localhost:9000/knowledgehub-documents/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
}
```

**Use Case:** Share temporary download links without authentication

---

#### 4. Get File Metadata

**Endpoint:** `GET /api/files/{fileId}`  
**Access:** Authenticated users  
**Description:** Retrieves file metadata without downloading the file

**Response:**
```json
{
  "success": true,
  "message": "File retrieved successfully",
  "data": {
    "id": 1,
    "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "isPublished": true,
    "fileSize": 1048576,
    "storagePath": "knowledgehub-documents/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "uploadDate": "2026-02-12T10:30:00",
    "uploadedByUserId": 1,
    "uploadedByUserName": "teacher@example.com",
    "pageCount": 25,
    "extractedText": "Sample text content from the PDF...",
    "keywords": ["tutorial", "java", "spring-boot"],
    "isScanned": false
  }
}
```

**Response Structure:**
```java
ApiResponse<File> {
  boolean success
  String message
  File data
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
  String extractedText       // For search functionality
  List<String> keywords
  Boolean isScanned
}
```

---

#### 5. Get User's Files

**Endpoint:** `GET /api/files/user/{userId}`  
**Access:** Authenticated users  
**Description:** Retrieves all files uploaded by a specific user

**Response:**
```json
{
  "success": true,
  "message": "Files retrieved successfully",
  "data": [
    {
      "id": 1,
      "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
      "originalFileName": "document.pdf",
      "isPublished": true,
      "fileSize": 1048576,
      "storagePath": "knowledgehub-documents/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
      "uploadDate": "2026-02-12T10:30:00",
      "uploadedByUserId": 1,
      "uploadedByUserName": "teacher@example.com",
      "pageCount": 25
    },
    {
      "id": 2,
      "fileName": "b2c3d4e5-f6g7-8901-bcde-fg2345678901.docx",
      "originalFileName": "lecture_notes.docx",
      "isPublished": false,
      "fileSize": 524288,
      "storagePath": "knowledgehub-documents/b2c3d4e5-f6g7-8901-bcde-fg2345678901.docx",
      "uploadDate": "2026-02-12T11:00:00",
      "uploadedByUserId": 1,
      "uploadedByUserName": "teacher@example.com"
    }
  ]
}
```

---

#### 6. Delete File

**Endpoint:** `DELETE /api/files/{fileId}`  
**Access:** Admin and Teacher only  
**Headers Required:** `X-User-Role: ADMIN` or `X-User-Role: TEACHER`  
**Description:** Deletes a file from both MinIO storage and database

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "data": null
}
```

**Error Response (Forbidden):**
```json
{
  "success": false,
  "message": "Only admins and teachers can delete files",
  "error": "Forbidden"
}
```

---

#### 7. Update Publication Status

**Endpoint:** `PUT /api/files/{fileId}/publication?isPublished=true`  
**Access:** Admin and Teacher only  
**Headers Required:** `X-User-Role: ADMIN` or `X-User-Role: TEACHER`  
**Description:** Publish or unpublish a file (makes it visible in library)

**Query Parameters:**
- `isPublished`: Boolean (true/false)

**Response:**
```json
{
  "success": true,
  "message": "Publication status updated successfully",
  "data": {
    "id": 1,
    "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "isPublished": true,
    "fileSize": 1048576,
    "storagePath": "knowledgehub-documents/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "uploadDate": "2026-02-12T10:30:00",
    "uploadedByUserId": 1,
    "uploadedByUserName": "teacher@example.com",
    "pageCount": 25
  }
}
```

---

#### 8. Add Keywords

**Endpoint:** `PUT /api/files/{fileId}/keywords`  
**Access:** Authenticated users (file owner)  
**Content-Type:** `application/json`  
**Description:** Add keywords to a file for better searchability

**Request:**
```json
["spring-boot", "tutorial", "java", "rest-api"]
```

**Response:**
```json
{
  "success": true,
  "message": "Keywords added successfully",
  "data": {
    "id": 1,
    "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "keywords": ["spring-boot", "tutorial", "java", "rest-api"],
    "isPublished": true,
    "fileSize": 1048576
  }
}
```

---

### File Type Enum Values

```java
FileType {
  REPORT,                  // Main project/internship report
  JOURNAL,                 // Daily internship log
  ATTESTATION,             // Company certificate
  EDUCATIONAL_MATERIAL,    // Teacher documents
  SUPPLEMENTARY           // Additional files
}
```

---

### Bucket Types

| Bucket Type | MinIO Bucket Name | Purpose |
|-------------|-------------------|---------|
| `documents` | knowledgehub-documents | Teacher educational materials |
| `pfa` | knowledgehub-pfa | PFA (Projet de Fin d'Année) submissions |
| `pfe` | knowledgehub-pfe | PFE (Projet de Fin d'Études) submissions |
| `stage` | knowledgehub-stage | Internship reports |
| `journal` | knowledgehub-journal | Internship daily journals |
| `attestation` | knowledgehub-attestation | Company attestations/certificates |

---

### File Features

1. **Automatic Text Extraction**: Uses Apache Tika to extract text from PDFs, Word docs, etc. for search functionality
2. **PDF Page Counting**: Automatically counts pages in PDF files
3. **Content Type Detection**: Automatically detects MIME types
4. **Unique File Names**: Generates UUID-based filenames to prevent conflicts
5. **Metadata Storage**: Stores file metadata in MySQL for fast queries
6. **Presigned URLs**: Generates temporary download links (useful for sharing)

---

### Placeholder Controllers (Not Yet Implemented)

The following controllers exist with TODO comments. Endpoints need to be implemented:

#### Student PFE Controller
- **Base Path:** `/api/student/pfe`
- **Purpose:** Student PFE (Projet de Fin d'Études) submission management
- **Planned Endpoints:** Submit PFE, view own PFE, update PFE, etc.

#### Student Stage Controller
- **Base Path:** `/api/student/stage`
- **Purpose:** Student internship (Stage) submission management
- **Planned Endpoints:** Submit Stage report, view own reports, update reports, etc.

#### Teacher Document Controller
- **Base Path:** `/api/teacher/documents`
- **Purpose:** Teacher document upload and management
- **Planned Endpoints:** Upload documents, view own documents, update/delete documents, etc.

#### Teacher Review Controller
- **Base Path:** `/api/teacher/review`
- **Purpose:** Teacher review of student submissions
- **Planned Endpoints:** View assigned submissions, provide feedback, approve/reject, etc.

#### Admin Document Controller
- **Base Path:** `/api/admin/documents`
- **Purpose:** Admin document management
- **Access:** Admin and authenticated users with appropriate permissions

##### 1. Upload File

**Endpoint:** `POST /api/admin/documents/upload`  
**Access:** Authenticated users  
**Content-Type:** `multipart/form-data`  
**Headers Required:** `X-Auth-Id`, `X-User-Name`  
**Description:** Uploads a file to MinIO storage and saves metadata

**Request Parameters:**
- `file`: File to upload (multipart)
- `fileType`: File type enum (REPORT, EDUCATIONAL_MATERIAL, etc.)
- `bucketType`: Bucket type (`documents`, `pfa`, `pfe`, `stage`)
- `title`: (optional) File title
- `description`: (optional) File description

**Request Example (cURL):**
```bash
curl -X POST http://localhost:8083/api/admin/documents/upload \
  -H "X-Auth-Id: 1" \
  -H "X-User-Name: teacher@example.com" \
  -F "file=@/path/to/document.pdf" \
  -F "fileType=EDUCATIONAL_MATERIAL" \
  -F "bucketType=documents" \
  -F "title=Introduction to Spring Boot" \
  -F "description=A comprehensive guide covering Spring Boot fundamentals"
```

**Response:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileId": 1,
    "fileName": "document.pdfa1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "title": "Introduction to Spring Boot",
    "description": "A comprehensive guide covering Spring Boot fundamentals",
    "fileSize": 1048576,
    "contentType": "application/pdf",
    "storagePath": "knowledgehub-documents/document.pdfa1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "fileType": "EDUCATIONAL_MATERIAL",
    "uploadDate": "2026-02-12T10:30:00",
    "uploadedByUserId": 1,
    "uploadedByUserName": "teacher@example.com",
    "pageCount": 25,
    "isPublished": false,
    "keywords": [],
    "message": "File uploaded successfully"
  }
}
```

##### 2. Get Secure Preview Link

**Endpoint:** `GET /api/admin/documents/view/{fileId}`  
**Access:** Admin, file owner, or public (if published)  
**Headers Required:** `X-User-Role`, `X-User-Id`  
**Description:** Generates a 2-minute secure presigned URL for file preview

**Response:**
```json
{
  "success": true,
  "message": "Secure link generated",
  "data": "http://localhost:9000/knowledgehub-documents/file.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
}
```

**Response (Forbidden):**
```json
{
  "success": false,
  "error": "Forbidden",
  "message": "This file is not yet published."
}
```

##### 3. Get File Metadata

**Endpoint:** `GET /api/admin/documents/{fileId}`  
**Access:** Authenticated users  
**Description:** Retrieves file metadata by ID

**Response:**
```json
{
  "success": true,
  "message": "File retrieved successfully",
  "data": {
    "id": 1,
    "fileName": "document.pdfa1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "originalFileName": "document.pdf",
    "title": "Introduction to Spring Boot",
    "description": "A comprehensive guide covering Spring Boot fundamentals",
    "isPublished": false,
    "fileSize": 1048576,
    "storagePath": "knowledgehub-documents/document.pdfa1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
    "uploadDate": "2026-02-12T10:30:00",
    "uploadedByUserId": 1,
    "uploadedByUserName": "teacher@example.com",
    "pageCount": 25,
    "extractedText": "Sample text content...",
    "keywords": ["spring", "boot", "tutorial"],
    "isScanned": false
  }
}
```

##### 4. Delete File

**Endpoint:** `DELETE /api/admin/documents/{fileId}`  
**Access:** Admin or file owner  
**Headers Required:** `X-User-Role`, `X-User-Id`  
**Description:** Deletes a file from storage and database

**Response (Success):**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "data": null
}
```

**Response (Forbidden):**
```json
{
  "success": false,
  "error": "Forbidden",
  "message": "Only admins and teachers can delete files"
}
```

##### 5. Update Publication Status

**Endpoint:** `PUT /api/admin/documents/{fileId}/publication`  
**Access:** Admin or Teacher  
**Headers Required:** `X-User-Role`  
**Description:** Publish or unpublish a file

**Request Parameters:**
- `isPublished`: Boolean (true/false)

**Request Example:**
```bash
curl -X PUT "http://localhost:8083/api/admin/documents/1/publication?isPublished=true" \
  -H "X-User-Role: ADMIN"
```

**Response:**
```json
{
  "success": true,
  "message": "Publication status updated successfully",
  "data": {
    "id": 1,
    "fileName": "document.pdf",
    "isPublished": true,
    "..."
  }
}
```

##### 6. Update File Metadata

**Endpoint:** `PUT /api/admin/documents/{fileId}/metadata`  
**Access:** Admin, Teacher, or file owner  
**Headers Required:** `X-User-Role`, `X-Auth-Id`  
**Description:** Updates file title and description

**Request Parameters:**
- `title`: (optional) New file title
- `description`: (optional) New file description

**Request Example:**
```bash
curl -X PUT "http://localhost:8083/api/admin/documents/1/metadata" \
  -H "X-User-Role: STUDENT" \
  -H "X-Auth-Id: 5" \
  -d "title=Updated Title&description=Updated description"
```

**Response:**
```json
{
  "success": true,
  "message": "File metadata updated successfully",
  "data": {
    "id": 1,
    "fileName": "document.pdf",
    "title": "Updated Title",
    "description": "Updated description",
    "..."
  }
}
```

**Response (Forbidden):**
```json
{
  "success": false,
  "error": "Forbidden",
  "message": "You don't have permission to update this file"
}
```

---

#### Library Controller
- **Base Path:** `/api/public/library`
- **Purpose:** Public library of approved documents
- **Access:** Public (no authentication required)

##### 1. Get All Documents

**Endpoint:** `GET /api/public/library/getDocuments`  
**Access:** Public  
**Description:** Retrieves all documents

**Response:**
```json
{
  "success": true,
  "message": "Documents retrieved successfully",
  "data": [
    {
      "id": 1,
      "fileName": "document.pdf",
      "originalFileName": "Spring Boot Guide.pdf",
      "title": "Introduction to Spring Boot",
      "description": "A comprehensive guide",
      "isPublished": true,
      "fileSize": 1048576,
      "uploadDate": "2026-02-12T10:30:00",
      "pageCount": 25
    }
  ]
}
```

##### 2. Get Documents by Type

**Endpoint:** `GET /api/public/library/getDocuments/{type}`  
**Access:** Public  
**Description:** Retrieves documents filtered by file type

**Path Parameters:**
- `type`: FileType enum (REPORT, EDUCATIONAL_MATERIAL, JOURNAL, ATTESTATION, SUPPLEMENTARY)

**Response:**
```json
{
  "success": true,
  "message": "Documents retrieved successfully",
  "data": [
    {
      "id": 1,
      "fileName": "document.pdf",
      "originalFileName": "Spring Boot Guide.pdf",
      "fileType": "EDUCATIONAL_MATERIAL",
      "title": "Introduction to Spring Boot",
      "..."
    }
  ]
}
```

##### 3. AI-Powered Semantic Search

**Endpoint:** `POST /api/public/library/ai-search`  
**Access:** Public  
**Description:** Performs semantic search using AI embeddings

**Request:**
```json
{
  "query": "spring boot microservices tutorial"
}
```

**Response:**
```json
{
  "success": true,
  "message": "result : 3",
  "data": [
    {
      "id": 1,
      "fileName": "microservices-guide.pdf",
      "title": "Microservices Architecture",
      "relevanceScore": 0.89
    },
    {
      "id": 5,
      "fileName": "spring-boot-intro.pdf",
      "title": "Introduction to Spring Boot",
      "relevanceScore": 0.85
    }
  ]
}
```

---

#### Student PFA Controller
- **Base Path:** `/api/student/pfa`
- **Purpose:** Student PFA (Projet de Fin d'Année) management
- **Access:** Admin and Teachers

##### 1. Create PFA

**Endpoint:** `POST /api/student/pfa/create`  
**Access:** Admin or Teacher  
**Headers Required:** `X-User-Role`  
**Description:** Creates a new PFA assignment for a student

**Request:**
```json
{
  "title": "E-commerce Platform Development",
  "year": 2026,
  "studentId": 10,
  "studentName": "John Doe",
  "studentEmail": "john.doe@example.com",
  "supervisorId": 5,
  "supervisorName": "Prof. Smith"
}
```

**Response:**
```json
{
  "success": true,
  "message": "PFA created successfully",
  "data": {
    "id": 1,
    "title": "E-commerce Platform Development",
    "year": 2026,
    "status": "UNDER_REVIEW",
    "studentId": 10,
    "studentName": "John Doe",
    "studentEmail": "john.doe@example.com",
    "supervisorId": 5,
    "supervisorName": "Prof. Smith",
    "submissionDate": "2026-02-12T10:30:00"
  }
}
```

**Note:** This endpoint also sends an email notification to the student.

---

---

### Expected Entity Structures (For Future Implementation)

**Document Entity:**
```java
Document {
  Long id
  String title
  String description
  DocumentCategory category    // TUTORIAL, LECTURE_NOTES, GUIDE, etc.
  String subject
  Long teacherId
  String teacherName
  String teacherEmail
  File file
  List<String> keywords
  Boolean isPublished
  Date createdAt
  Date updatedAt
}
```

**PFA Entity:**
```java
PFA {
  Long id
  String title
  Integer year
  SubmissionStatus status      // SUBMITTED, UNDER_REVIEW, REJECTED, APPROVED
  Long studentId
  String studentName
  String studentEmail
  Long supervisorId
  String supervisorName
  File reportFile
  Date submissionDate
  String supervisorFeedback
  Date createdAt
  Date updatedAt
}
```

**File Entity:**
```java
File {
  Long id
  String fileName              // Generated unique filename
  String originalFileName      // Original uploaded filename
  String title                 // File title (optional)
  String description           // File description (optional)
  Boolean isPublished
  Long fileSize
  String storagePath
  Date uploadDate
  Long uploadedByUserId
  String uploadedByUserName
  Integer pageCount
  String extractedText         // For search functionality
  List<String> keywords
  FileType fileType           // REPORT, JOURNAL, ATTESTATION, etc.
  Boolean isScanned
}
```

---

## Notification Service Endpoints

**Base URL:** `http://localhost:8080/notification-service` (via Gateway) or `http://localhost:8084` (direct)

### 1. Send Notification

**Endpoint:** `POST /api/notifications/send`  
**Access:** System/Service-to-Service  
**Description:** Sends a notification (email or push)

**Request (Email):**
```json
{
  "userId": "5",
  "content": "Your account has been created successfully",
  "type": "EMAIL",
  "subject": "Welcome to KnowledgeHub",
  "recipientEmail": "student@example.com"
}
```

**Request (Push):**
```json
{
  "userId": "5",
  "content": "New assignment posted",
  "type": "PUSH",
  "deviceToken": "firebase_device_token_here"
}
```

**Request Structure:**
```java
NotificationRequest {
  @NotBlank String userId
  @NotBlank String content
  @NotNull NotificationType type    // EMAIL | PUSH
  String subject                     // For email
  @NotBlank String recipientEmail    // For email
  String deviceToken                 // For push
}
```

**Response:**
```json
{
  "id": "65c8f2a3b4e1f8c9d0e12345",
  "userId": "5",
  "content": "Your account has been created successfully",
  "type": "EMAIL",
  "subject": "Welcome to KnowledgeHub",
  "delivered": true,
  "read": false,
  "createdAt": "2026-02-12T10:30:00",
  "deliveredAt": "2026-02-12T10:30:05",
  "readAt": null
}
```

**Response Structure:**
```java
NotificationResponse {
  String id                      // MongoDB ObjectId
  String userId
  String content
  NotificationType type
  boolean delivered
  boolean read
  LocalDateTime createdAt
  LocalDateTime deliveredAt
  LocalDateTime readAt
  String subject
}
```

---

### 2. Mark Notification as Read

**Endpoint:** `PUT /api/notifications/{id}/read`  
**Access:** User (own notifications)  
**Description:** Marks a notification as read

**Response:**
```json
{
  "id": "65c8f2a3b4e1f8c9d0e12345",
  "userId": "5",
  "content": "Your account has been created successfully",
  "type": "EMAIL",
  "subject": "Welcome to KnowledgeHub",
  "delivered": true,
  "read": true,
  "createdAt": "2026-02-12T10:30:00",
  "deliveredAt": "2026-02-12T10:30:05",
  "readAt": "2026-02-12T11:00:00"
}
```

---

### 3. Get User Notifications

**Endpoint:** `GET /api/notifications/user/{userId}`  
**Access:** User (own notifications)  
**Description:** Retrieves all notifications for a user

**Response:**
```json
[
  {
    "id": "65c8f2a3b4e1f8c9d0e12345",
    "userId": "5",
    "content": "Your account has been created successfully",
    "type": "EMAIL",
    "subject": "Welcome to KnowledgeHub",
    "delivered": true,
    "read": true,
    "createdAt": "2026-02-12T10:30:00",
    "deliveredAt": "2026-02-12T10:30:05",
    "readAt": "2026-02-12T11:00:00"
  },
  {
    "id": "65c8f2a3b4e1f8c9d0e12346",
    "userId": "5",
    "content": "New assignment posted in 3A-INFO",
    "type": "PUSH",
    "delivered": true,
    "read": false,
    "createdAt": "2026-02-12T14:00:00",
    "deliveredAt": "2026-02-12T14:00:02",
    "readAt": null
  }
]
```

**Response Structure:**
```java
List<NotificationResponse>
```

---

### 4. Get Unread Notifications

**Endpoint:** `GET /api/notifications/user/{userId}/unread`  
**Access:** User (own notifications)  
**Description:** Retrieves only unread notifications for a user

**Response:**
```json
[
  {
    "id": "65c8f2a3b4e1f8c9d0e12346",
    "userId": "5",
    "content": "New assignment posted in 3A-INFO",
    "type": "PUSH",
    "delivered": true,
    "read": false,
    "createdAt": "2026-02-12T14:00:00",
    "deliveredAt": "2026-02-12T14:00:02",
    "readAt": null
  }
]
```

**Response Structure:**
```java
List<NotificationResponse>
```

---

## AI & Search Service Endpoints

**Base URL:** `http://localhost:8080/ai-service` (via Gateway) or `http://localhost:8000` (direct)

### 1. Service Info

**Endpoint:** `GET /`  
**Access:** Public  
**Description:** Returns service information

**Response:**
```json
{
  "service": "AI and Search Service",
  "status": "running",
  "version": "1.0.0"
}
```

---

### 2. Health Check

**Endpoint:** `GET /health`  
**Access:** Public  
**Description:** Health check endpoint

**Response:**
```json
{
  "status": "healthy"
}
```

---

### Placeholder Routes (Not Yet Implemented)

#### AI Router
- **Base Path:** `/ai`
- **Purpose:** AI-powered features
- **Planned Endpoints:** 
  - Document summarization
  - Content recommendations
  - Smart tagging

#### Search Router
- **Base Path:** `/search`
- **Purpose:** Advanced search functionality
- **Planned Endpoints:**
  - Semantic search across documents
  - Search by keywords
  - Filter by category/type
  - Full-text search

---

## Common Response Patterns

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message"
}
```

### Validation Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## Authentication Headers

All authenticated endpoints (except `/api/public/**`) require JWT authentication via API Gateway.

**Required Headers:**
```
Authorization: Bearer <jwt_token>
```

**Gateway-Added Headers (for downstream services):**
```
X-Auth-Id: <user_auth_id>
X-User-Role: <STUDENT|TEACHER|ADMIN>
X-User-Email: <user_email>
```

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | Validation errors, malformed request |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (e.g., email exists) |
| 500 | Internal Server Error | Server-side error |

---

## Notes

1. **Date/Time Format:** All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
2. **Pagination:** Not yet implemented (will be added for list endpoints)
3. **File Uploads:** Use `multipart/form-data` content type
4. **CSV Format:** For bulk uploads, first row is header
5. **MongoDB IDs:** Notification IDs are MongoDB ObjectId strings (24 hex characters)

---

## Testing Endpoints

### Using cURL:

**Login:**
```bash
curl -X POST http://localhost:8080/auth-service/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student@example.com","password":"password123"}'
```

**Authenticated Request:**
```bash
TOKEN="your_jwt_token_here"
curl -X GET http://localhost:8080/user-service/api/students/all \
  -H "Authorization: Bearer $TOKEN"
```

### Using Swagger UI:
Each service has Swagger UI available at:
- Auth: http://localhost:8081/swagger-ui.html
- User: http://localhost:8082/swagger-ui.html
- Resource: http://localhost:8083/swagger-ui.html
- Notification: http://localhost:8084/swagger-ui.html
- AI: http://localhost:8000/docs

---

## Changelog

- **v1.0.0** (2026-02-12): Initial API documentation
  - Auth Service: 8 endpoints
  - User Service: 15 endpoints
  - Resource Service: 3 test endpoints (+ 6 placeholder controllers)
  - Notification Service: 4 endpoints
  - AI Service: 2 info endpoints (+ 2 placeholder routers)
