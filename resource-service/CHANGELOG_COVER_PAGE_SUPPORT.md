# Cover Page Support - Frontend Integration Notes

Date: 2026-05-20
Service: `resource-service`

## What changed

A cover-page feature was added for library documents.

- A document can now have a cover image (`coverImagePath`, `coverOriginalFileName`).
- A dedicated MinIO bucket is used for covers: `knowledgehub-cover-pages`.
- Cover image is always based on page 1 when auto-generated from PDF.

## Business rules implemented

Cover pages are created only for library publication flows:

1. Upload flow (`POST /api/admin/documents/upload`)
   - `TEACHER` upload is published by default.
   - `ADMIN` upload can be published immediately with `isPublished=true`.
   - If the file is published during upload:
     - If `coverPage` image is sent, it is used as cover.
     - If `coverPage` is not sent and uploaded file is PDF, page 1 is auto-extracted and stored as cover.

2. Publication update flow (`PUT /api/admin/documents/{fileId}/publication`)
   - When `ADMIN` publishes (`isPublished=true`) and file type is `PFE_REPORT` or `STAGE_REPORT`:
     - If file has no cover yet, page 1 is auto-extracted from PDF and stored as cover.

## API contract updates

### 1) Upload endpoint

`POST /api/admin/documents/upload` (`multipart/form-data`)

New optional request parts/params:

- `coverPage` (file): optional image (`image/*`).
- `isPublished` (boolean): optional; for `ADMIN` to publish directly during upload.

Existing fields remain unchanged (`file`, `fileType`, `bucketType`, `title`, `description`, headers).

### 2) Upload response

`FileUploadResponse` now includes:

- `coverImagePath` (string | null)
- `coverOriginalFileName` (string | null)

## Frontend actions required

1. Update upload form:
   - Add optional image input `coverPage`.
   - For admin UI, add optional `isPublished` checkbox/toggle.

2. Update upload API call payload:
   - Send `coverPage` only when selected.
   - Send `isPublished=true` only when admin explicitly publishes on upload.

3. Update frontend models:
   - Add `coverImagePath?: string`
   - Add `coverOriginalFileName?: string`
   in upload response and file entity model if used.

4. Display logic:
   - If `coverImagePath` exists, display cover thumbnail/card image.
   - If not, use default placeholder image.

## Notes

- If `coverPage` is provided but is not an image, backend returns an error.
- Auto-generation from non-PDF files is skipped (no cover generated).
- Existing files remain valid; cover fields can be null.

