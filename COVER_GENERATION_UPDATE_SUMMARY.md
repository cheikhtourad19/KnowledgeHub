# Cover Generation on Publication Status Update - Implementation Summary

## Overview
Enhanced the file publication workflow to automatically generate cover pages for files when they are published if no cover exists. This mirrors the cover generation logic available during file upload.

## Flow Implementation

When an admin or teacher updates a file to **published status**, the following process occurs:

```
1. Admin/Teacher sends request to update file publication status
                ↓
2. Controller validates permissions (only admin or file owner can publish)
                ↓
3. Service checks publication status change:
   - Is the file transitioning from unpublished to published?
                ↓
4. Check if cover should be generated:
   - Is the user an admin or teacher?
   - Does the file currently have a cover?
                ↓
5. If cover is missing:
   a. Retrieve the source file from MinIO
   b. Extract first page from PDF
   c. Render page as PNG image at 150 DPI
   d. Save cover to MinIO (cover-pages bucket)
   e. Attach cover metadata to file entity
                ↓
6. Index file in AI/Search Service (if published)
                ↓
7. Save all changes to database
```

## Changes Made

### 1. **FileService.java** - Enhanced Cover Generation Logic

#### Method: `shouldGenerateCoverOnPublish()` (Updated)
**Location:** Lines 607-611

**Changes:**
- **Before:** Only generated covers for PFE_REPORT and STAGE_REPORT file types by ADMIN role
- **After:** Generates covers for ALL file types when ADMIN or TEACHER publishes a file

```java
private boolean shouldGenerateCoverOnPublish(File file, String role) {
    // Generate cover for admin or teacher publishing if no cover exists
    boolean isStaff = "ADMIN".equals(role) || "TEACHER".equals(role);
    boolean noCoverExists = file.getCoverImagePath() == null || file.getCoverImagePath().isBlank();
    return isStaff && noCoverExists;
}
```

#### Method: `updatePublicationStatus()` (Enhanced with Logging)
**Location:** Lines 354-385

**Changes:**
- Added detailed logging to track the cover generation process
- Logs include:
  - File ID and current cover status
  - Cover generation completion status
  - AI/Search Service indexing status
  - Publication status updates

```java
if (publishingNow && shouldGenerateCoverOnPublish(file, role)) {
    log.info("Publishing file {} - checking cover generation. Current cover: {}", fileId, file.getCoverImagePath());
    ensureCoverPageForPublishedFile(file);
    log.info("Cover generation completed for file {}. New cover: {}", fileId, file.getCoverImagePath());
}
```

#### Method: `ensureCoverPageForPublishedFile()` (Enhanced with Logging)
**Location:** Lines 580-605

**Changes:**
- Enhanced logging to show:
  - When a file already has a cover (skip case)
  - Source file retrieval from MinIO
  - PDF extraction process
  - Cover save completion

**Key Logic:**
```java
if (file.getCoverImagePath() != null && !file.getCoverImagePath().isBlank()) {
    log.info("File {} already has a cover - skipping cover generation: {}", 
             file.getId(), file.getCoverImagePath());
    return;  // Skip if cover already exists
}
```

#### Method: `extractAndUploadPdfFirstPage()` (Enhanced with Logging)
**Location:** Lines 542-569

**Changes:**
- Detailed logging of each step:
  - PDF page count validation
  - First page rendering at 150 DPI
  - Cover upload to MinIO (cover-pages bucket)
  - Final storage path and metadata

```java
log.info("Rendering first page of PDF to image for: {} (total pages: {})", 
         sourceOriginalName, document.getNumberOfPages());
// ... render image ...
log.info("Uploading cover image to MinIO bucket: {} - object: {} (size: {} bytes)", 
         coverPagesBucket, objectName, imageBytes.length);
```

#### Method: `uploadProvidedCover()` (Enhanced with Logging)
**Location:** Lines 499-526

**Changes:**
- Enhanced logging for explicitly provided covers
- Logs include format validation and MinIO upload confirmation

## Controller Integration

The **AdminDocumentController.updatePublicationStatus()** endpoint (Lines 236-264) already:
- ✅ Validates user permissions (admin or file owner)
- ✅ Extracts role from headers
- ✅ Passes role to service method: `fileService.updatePublicationStatus(fileId, isPublished, role)`

## Features

### ✅ Cover Generation Process
1. **Check**: Verifies if file already has a cover
2. **Skip**: Returns immediately if cover exists
3. **Generate**: Extracts first page from PDF if no cover
4. **Save**: Uploads PNG cover to MinIO cover-pages bucket
5. **Attach**: Updates file entity with cover metadata
6. **Persist**: Saves all changes to database

### ✅ Supported Scenarios
- Admin publishes a new file → auto-generates cover if missing
- Teacher publishes their own file → auto-generates cover if missing
- File already has a cover → skips generation
- Non-PDF files → skips generation gracefully

### ✅ Logging
- Comprehensive logging at DEBUG and INFO levels
- Tracks each step of the process
- Helps debugging and monitoring

## Testing the Implementation

### Test Case 1: Admin Publishing a PDF File
```
Request: PUT /api/admin/documents/{fileId}/publication?isPublished=true
Headers: X-User-Role: ADMIN
         X-User-Id: 1

Result: 
- File transitions to published
- Missing cover is auto-generated from first PDF page
- Cover saved to MinIO
- File indexed in AI/Search Service
```

### Test Case 2: File Already Has Cover
```
Request: PUT /api/admin/documents/{fileId}/publication?isPublished=true
Headers: X-User-Role: TEACHER
         X-User-Id: 5
         
(Assuming file already has cover_image_path)

Result:
- File transitions to published
- Cover generation skipped (already exists)
- File indexed in AI/Search Service
```

### Test Case 3: Non-Admin/Teacher Cannot Publish
```
Request: PUT /api/admin/documents/{fileId}/publication?isPublished=true
Headers: X-User-Role: STUDENT
         X-User-Id: 10

Result:
- 403 Forbidden error
- Publication status unchanged
```

## Database Schema Notes

The `File` entity already has the following fields for cover management:
- `coverImagePath`: Path to cover in MinIO (format: "bucket/object")
- `coverOriginalFileName`: Original name of the cover image

No schema changes required.

## MinIO Buckets Used

- **documents/pfa/pfe/stage**: Source files bucket
- **cover-pages**: Dedicated bucket for cover images (auto-generated from PDFs)

## Dependencies

All required dependencies already in place:
- Apache PDFBox: PDF page rendering
- ImageIO: PNG image encoding
- MinIO Client: File upload/retrieval

## Summary

The implementation provides a seamless experience where:
1. **Admins** can publish files without providing explicit covers
2. **Teachers** can publish their own files with auto-generated covers
3. **Existing covers** are preserved (no overwrite)
4. **PDFs only** have auto-generated covers (non-PDFs skip gracefully)
5. **Full audit trail** via comprehensive logging

