# Internship Convention Extraction Endpoint

## Endpoint

- **Method:** `POST`
- **Path:** `/api/internship/extract-convention`
- **Content-Type:** `multipart/form-data`
- **Form field:** `file` (PDF only)

## What It Accepts

The endpoint accepts one uploaded PDF file:

- `file`: internship convention PDF (`application/pdf`)

Validation rules:

- Rejects non-PDF files
- Rejects empty files
- Rejects oversized files (max configured by `MAX_FILE_SIZE_MB`)
- Rejects unreadable/invalid PDFs

## What It Returns

### Success

- **Status:** `200`
- **Content-Type:** `application/json`
- **Body:**

```json
{
  "studentName": "string | null",
  "studentIdentifiant": "string | null",
  "studentFiliere": "string | null",
  "studentCin": "string | null",
  "studentEmail": "string | null",
  "studentPhone": "string | null",
  "studentAddress": "string | null",
  "entrepriseName": "string | null",
  "entrepriseDomaine": "string | null",
  "entrepriseAddress": "string | null",
  "entreprisePhone": "string | null",
  "entrepriseEmail": "string | null",
  "entrepriseSiteWeb": "string | null",
  "entrepriseEncadrantName": "string | null",
  "entrepriseEncadrantFonction": "string | null",
  "entrepriseEncadrantService": "string | null",
  "entrepriseEncadrantEmail": "string | null",
  "pfeTitle": "string | null",
  "dateDebut": "YYYY-MM-DD | null",
  "dateFin": "YYYY-MM-DD | null",
  "stageType": "ouvrier | technicien | null"
}
```

Rules:

- Any missing/unclear value is `null`
- Dates are ISO strings (`YYYY-MM-DD`) when found
- `stageType` is only `"ouvrier"`, `"technicien"`, or `null`

### Error statuses

- `422`: invalid PDF or text extraction failed
- `500`: Gemini call failed or invalid JSON returned by model

## Use With Two Forms (Subset Consumption)

The endpoint always returns the full object above. Each form should only read the keys it needs.

### Shared fields (common between forms)

- `studentCin`
- `studentPhone`
- `entrepriseName`
- `entrepriseDomaine`
- `entrepriseEncadrantName`
- `entrepriseEncadrantEmail`
- `pfeTitle`
- `dateDebut`
- `dateFin`

### Example Form A fields

- `studentCin`
- `studentPhone`
- `entrepriseName`
- `entrepriseDomaine`
- `entrepriseEncadrantName`
- `entrepriseEncadrantEmail`
- `pfeTitle`
- `dateDebut`
- `dateFin`

### Example Form B fields

- `studentName`
- `studentIdentifiant`
- `studentFiliere`
- `studentEmail`
- `studentAddress`
- `entrepriseAddress`
- `entreprisePhone`
- `entrepriseEmail`
- `entrepriseSiteWeb`
- `entrepriseEncadrantFonction`
- `entrepriseEncadrantService`
- `stageType`

## Frontend integration note

Client should build a patch object from allowed keys only:

1. Define a whitelist for each form.
2. Copy only non-null values from endpoint response.
3. Ignore all other keys.
4. Keep form editable for manual correction.
