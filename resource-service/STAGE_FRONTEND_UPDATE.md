# Stage Module Update - Frontend Handoff

Ce document résume les changements backend sur le module stage pour mise à jour frontend.

## 1) Nouveaux champs Stage

Les champs suivants existent maintenant dans la réponse stage:

- cloture: Boolean (default false)
- canEdit: Boolean (default false)
- conventionFileId: Long (nullable)
- conventionFileName: String (nullable)
- conventionFile: File (nullable)

Contexte:

- cloture=true: aucun upload de fichier n'est autorisé.
- canEdit=true: l'etudiant peut modifier les metadonnees du stage.
- canEdit=false: l'etudiant ne peut pas modifier les metadonnees.

## 2) Type de fichier ajoute

Nouveau FileType:

- CONVENTION_STAGE

## 3) Routes et comportements etudiant

Base: /api/student/stage

### 3.0 Creation de stage (convention obligatoire)

- Method: POST
- Path: /
- Content-Type: multipart/form-data
- Headers:
  - X-User-Id: Long
- FormData:
  - title: String (required)
  - companyName: String (required)
  - durationMonths: Number (required)
  - conventionFile: File (required)

Regles backend:

- La convention est obligatoire a la creation.
- Le backend upload automatiquement la convention via fileService.uploadFile.
- Le fichier uploadé est rattache au champ conventionFile du Stage.
- Apres creation reussie du stage, l'autorisation StageCreationAutorisation de l'etudiant est supprimee.

### 3.1 Modifier les metadonnees du stage

- Method: PUT
- Path: /{stageId}/metadata
- Headers:
  - X-User-Id: Long
- Body JSON:

```json
{
  "title": "Nouveau titre",
  "companyName": "Nouvelle societe",
  "durationMonths": 4
}
```

Regles backend:

- Refuse si l'etudiant n'est pas proprietaire du stage.
- Refuse si cloture=true.
- Refuse si canEdit=false.

### 3.2 Upload convention de stage

- Method: POST
- Path: /upload-convention/{stageId}
- Headers:
  - X-User-Id: Long
- FormData:
  - file: MultipartFile (required)
  - title: String (optional)
  - description: String (optional)

Regles backend:

- Refuse si l'etudiant n'est pas proprietaire.
- Refuse si cloture=true.
- Refuse pour ancien stage (year < currentYear), comme les autres uploads.

### 3.3 Uploads existants (rapport/journal/attestation)

Routes existantes (inchangées):

- POST /upload-report/{stageId}
- POST /upload-journal/{stageId}
- POST /upload-attestation/{stageId}

Nouvelle regle appliquee a ces 3 routes:

- Si cloture=true => upload interdit.

## 4) Routes admin

Base: /api/admin/stage

### 4.1 Cloturer un stage (admin)

- Method: PATCH
- Path: /{stageId}/close
- Headers:
  - X-User-Role: ADMIN

Effet:

- cloture passe a true
- canEdit passe automatiquement a false

### 4.2 Activer/desactiver canEdit (admin)

- Method: PATCH
- Path: /{stageId}/can-edit
- Query param:
  - canEdit=true|false
- Headers:
  - X-User-Role: ADMIN

Regle backend:

- Si stage deja cloture, canEdit ne peut pas etre remis a true.

### 4.3 Supprimer un stage (admin)

- Method: DELETE
- Path: /{stageId}
- Headers:
  - X-User-Role: ADMIN

Effet:

- Le stage est supprime de la base.

## 5) Route jury/enseignant

Base: /api/teacher/stage

### 5.1 Cloturer un stage (jury/teacher)

- Method: PATCH
- Path: /{stageId}/close
- Headers:
  - X-User-Id: Long
  - X-User-Role: JURY ou TEACHER

Regle backend:

- L'utilisateur doit etre le superviseur assigne au stage.
- Effet: cloture=true et canEdit=false.

## 6) Adaptations frontend conseillees

## 6.1 UI etat du stage

Afficher dans la vue stage:

- Badge cloture: Ouvert/Ferme selon cloture
- Badge editable: Editable/Locked selon canEdit

## 6.2 Permissions UI upload

Desactiver les boutons upload (rapport/journal/attestation/convention) si:

- cloture=true

## 6.3 Permissions UI edition metadata

Activer le formulaire de metadata uniquement si:

- stage.canEdit === true
- ET stage.cloture === false

## 6.4 Fichier convention

Ajouter un bloc fichier "Convention de stage":

- bouton upload
- nom du fichier convention (conventionFileName)
- preview/download via conventionFile ou conventionFileId selon flux frontend existant

## 7) Exemple minimal StageResponse (partiel)

```json
{
  "id": 12,
  "title": "Stage IA",
  "companyName": "Tech Corp",
  "durationMonths": 4,
  "cloture": false,
  "canEdit": true,
  "reportFileId": 100,
  "journalFileId": 101,
  "attestationFileId": 102,
  "conventionFileId": 103,
  "conventionFileName": "Convention de stage - Stage IA.pdf"
}
```

## 8) Note backend/DB

Le backend a ajoute de nouveaux champs dans l'entite Stage. En environnement deja deploye, il faut verifier que la migration SQL (ou auto-ddl) a bien ajoute:

- convention_file_id
- cloture
- can_edit

Si vous utilisez des migrations versionnees, creer/appliquer le script correspondant.
