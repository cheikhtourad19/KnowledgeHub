# MinIO Integration for Resource Service

## Overview

The Resource Service uses MinIO for object storage to handle file uploads and downloads. MinIO is an open-source, S3-compatible object storage system that runs locally or in the cloud.

## MinIO Setup

### Installation (Using Docker)

The easiest way to run MinIO is using Docker:

```bash
# Run MinIO container
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -v ~/minio/data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

### Installation (Standalone)

**macOS:**
```bash
brew install minio/stable/minio
minio server ~/minio/data --console-address ":9001"
```

**Linux:**
```bash
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
./minio server ~/minio/data --console-address ":9001"
```

**Windows:**
Download from: https://dl.min.io/server/minio/release/windows-amd64/minio.exe

## Access MinIO

- **API Endpoint:** http://localhost:9000
- **Console UI:** http://localhost:9001
- **Default Credentials:**
  - Username: `minioadmin`
  - Password: `minioadmin`

## Configuration

The resource service is configured in `application.yml`:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: knowledgehub
  buckets:
    documents: knowledgehub-documents  # Teacher documents
    pfa: knowledgehub-pfa             # PFA submissions
    pfe: knowledgehub-pfe             # PFE submissions
    stage: knowledgehub-stage         # Stage/Internship reports
```

## Buckets

The service automatically creates the following buckets on startup:

1. **knowledgehub-documents** - Teacher-uploaded educational materials
2. **knowledgehub-pfa** - Student PFA (Projet de Fin d'Année) submissions
3. **knowledgehub-pfe** - Student PFE (Projet de Fin d'Études) submissions
4. **knowledgehub-stage** - Student internship reports and documents

## File Service Features

### 1. File Upload

Uploads files to MinIO and saves metadata to MySQL database.

**Features:**
- Unique file name generation (UUID)
- Content type detection using Apache Tika
- Text extraction for search (up to 10KB)
- PDF page counting
- Automatic bucket selection based on file type

**Usage:**
```java
FileUploadResponse response = fileService.uploadFile(
    multipartFile,
    FileType.REPORT,
    userId,
    userName,
    "pfa"  // bucket type
);
```

### 2. File Download

Downloads files from MinIO storage.

**Usage:**
```java
FileDownloadResponse response = fileService.downloadFile(fileId);
byte[] content = response.getFileContent();
```

### 3. Presigned URLs

Generates temporary download URLs (default: 60 minutes).

**Usage:**
```java
String url = fileService.generatePresignedUrl(fileId, 60);
```

### 4. File Deletion

Deletes files from both MinIO and database.

**Usage:**
```java
fileService.deleteFile(fileId);
```

### 5. Metadata Management

- Get file by ID
- Get files by uploader
- Update publication status
- Add keywords for search

## REST API Endpoints

### Upload File
```http
POST /api/files/upload
Content-Type: multipart/form-data
Headers:
  X-Auth-Id: {userId}
  X-User-Email: {userEmail}

Parameters:
  file: (file)
  fileType: REPORT | EDUCATIONAL_MATERIAL | etc.
  bucketType: documents | pfa | pfe | stage
```

### Download File
```http
GET /api/files/download/{fileId}
```

### Get Presigned URL
```http
GET /api/files/{fileId}/presigned-url?expiryMinutes=60
```

### Get File Metadata
```http
GET /api/files/{fileId}
```

### Get User's Files
```http
GET /api/files/user/{userId}
```

### Delete File
```http
DELETE /api/files/{fileId}
Headers:
  X-User-Role: ADMIN | TEACHER
```

### Update Publication Status
```http
PUT /api/files/{fileId}/publication?isPublished=true
Headers:
  X-User-Role: ADMIN | TEACHER
```

### Add Keywords
```http
PUT /api/files/{fileId}/keywords
Content-Type: application/json

Body:
["keyword1", "keyword2", "keyword3"]
```

## File Types

Defined in `FileType` enum:

- **REPORT** - Main project/internship report
- **JOURNAL** - Daily internship log
- **ATTESTATION** - Company certificate
- **EDUCATIONAL_MATERIAL** - Teacher documents
- **SUPPLEMENTARY** - Additional files

## Text Extraction

The service uses Apache Tika to extract text content from various file formats:
- PDF
- Word (DOC, DOCX)
- PowerPoint (PPT, PPTX)
- Excel (XLS, XLSX)
- Text files
- And many more

**Limitations:**
- Extracts first 50KB of content
- Stores up to 10,000 characters in database
- Used for full-text search functionality

## PDF Features

For PDF files, the service:
1. Counts the number of pages
2. Extracts text content
3. Stores page count in metadata

## Database Schema

The `files` table stores file metadata:

```sql
CREATE TABLE files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) UNIQUE NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    upload_date DATETIME NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    uploaded_by_user_name VARCHAR(255),
    page_count INT,
    extracted_text MEDIUMTEXT,
    keywords JSON,
    is_scanned BOOLEAN NOT NULL DEFAULT FALSE
);
```

## Security Considerations

1. **Access Control:**
   - File upload: Authenticated users
   - File deletion: Admin and Teacher only
   - Publication status: Admin and Teacher only

2. **File Validation:**
   - Check file size limits (configured: 100MB)
   - Validate content type
   - Scan for malware (to be implemented)

3. **MinIO Security:**
   - Change default credentials in production
   - Use HTTPS for MinIO endpoint
   - Configure bucket policies
   - Enable versioning for important documents

## Integration with Other Services

### PFA Service
```java
@Autowired
private FileService fileService;

public PFA submitPFA(MultipartFile report, PFARequest request) {
    FileUploadResponse fileResponse = fileService.uploadFile(
        report,
        FileType.REPORT,
        studentId,
        studentName,
        "pfa"
    );
    
    PFA pfa = PFA.builder()
        .reportFile(fileService.getFileById(fileResponse.getFileId()))
        // ... other fields
        .build();
    
    return pfaRepository.save(pfa);
}
```

### PFE Service
```java
FileUploadResponse fileResponse = fileService.uploadFile(
    report,
    FileType.REPORT,
    studentId,
    studentName,
    "pfe"
);
```

### Stage Service
```java
// Upload multiple files
FileUploadResponse reportFile = fileService.uploadFile(report, FileType.REPORT, userId, userName, "stage");
FileUploadResponse journalFile = fileService.uploadFile(journal, FileType.JOURNAL, userId, userName, "stage");
FileUploadResponse attestationFile = fileService.uploadFile(attestation, FileType.ATTESTATION, userId, userName, "stage");
```

### Library/Document Service
```java
FileUploadResponse fileResponse = fileService.uploadFile(
    document,
    FileType.EDUCATIONAL_MATERIAL,
    teacherId,
    teacherName,
    "documents"
);
```

## Monitoring

### MinIO Metrics

Access MinIO metrics at:
- Console: http://localhost:9001
- Prometheus endpoint: http://localhost:9000/minio/v2/metrics/cluster

### Application Logs

The FileService logs all operations:
```
INFO: File uploaded to MinIO: knowledgehub-pfa/uuid.pdf
INFO: File metadata saved to database with ID: 123
INFO: File downloaded from MinIO: knowledgehub-pfa/uuid.pdf
INFO: Generated presigned URL for file ID: 123
INFO: File deleted successfully: knowledgehub-pfa/uuid.pdf
```

## Troubleshooting

### Connection Issues

```bash
# Check if MinIO is running
curl http://localhost:9000/minio/health/live

# Check buckets
mc alias set myminio http://localhost:9000 minioadmin minioadmin
mc ls myminio
```

### Bucket Creation Failed

If buckets are not created automatically, create them manually:

```bash
mc mb myminio/knowledgehub-documents
mc mb myminio/knowledgehub-pfa
mc mb myminio/knowledgehub-pfe
mc mb myminio/knowledgehub-stage
mc mb myminio/knowledgehub-journal
mc mb myminio/knowledgehub-attestation
```

### Large File Upload Issues

Increase timeout values in `application.yml`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```

## Production Recommendations

1. **Use dedicated MinIO cluster** with multiple nodes for high availability
2. **Enable encryption** at rest and in transit
3. **Configure backup strategy** (versioning, replication)
4. **Set up monitoring** with Prometheus/Grafana
5. **Implement virus scanning** before file storage
6. **Use CDN** for public file downloads
7. **Configure lifecycle policies** for old files

## Dependencies

```xml
<!-- MinIO Client -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>

<!-- Apache Tika for file content extraction -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.1</version>
</dependency>

<!-- Apache PDFBox for PDF page counting -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

## Testing

Create a test upload:

```bash
curl -X POST http://localhost:8083/api/files/upload \
  -H "X-Auth-Id: 1" \
  -H "X-User-Email: test@example.com" \
  -F "file=@/path/to/document.pdf" \
  -F "fileType=REPORT" \
  -F "bucketType=pfa"
```

## References

- [MinIO Documentation](https://min.io/docs/minio/linux/index.html)
- [Apache Tika Documentation](https://tika.apache.org/)
- [Apache PDFBox Documentation](https://pdfbox.apache.org/)
