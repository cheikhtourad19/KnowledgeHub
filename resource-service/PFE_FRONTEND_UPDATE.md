# PFE Module Update - Frontend Handoff

Ce document resume les changements backend du module PFE pour mise a jour frontend.

## 1) Nouveaux champs PFE

Les champs suivants existent maintenant dans la reponse PFE:

- reporteurFeedback: String (nullable)
- cloture: Boolean (default false)

Contexte:

- cloture=true: aucun upload de document n'est autorise.
- reporteurFeedback renseigne: permet de re-autoriser l'upload apres validation encadrant.

## 2) Workflow metier d'upload (nouvelle logique)

Workflow cible:

1. Publier / depot initial
2. Validation par encadrant => blocage upload
3. Feedback par rapporteur => deblocage upload (sans changer le statut)
4. Cloture par admin => blocage upload definitif

Regles backend appliquees sur upload rapport + attestation:

- Upload bloque si status == UNDER_REVIEW
- Upload bloque si cloture == true
- Upload bloque si status == APPROVED ET reporteurFeedback vide/null
- Upload autorise si status == APPROVED ET reporteurFeedback existe
- Upload autorise pour les autres statuts

## 3) Routes etudiant (module PFE)

Base: /api/student/pfe

### 3.1 Upload rapport

- Method: POST
- Path: /upload-report/{idPFE}
- Headers:
  - X-User-Id: Long
- FormData:
  - file: MultipartFile (required)
  - title: String (optional)
  - description: String (optional)

### 3.2 Upload attestation

- Method: POST
- Path: /upload-attestation/{idPFE}
- Headers:
  - X-User-Id: Long
- FormData:
  - file: MultipartFile (required)
  - title: String (optional)
  - description: String (optional)

## 4) Nouvelle route rapporteur

Base: /api/teacher/pfe

### 4.1 Ajouter feedback rapporteur

- Method: PUT
- Path: /pfe/{pfeId}/rapporteur-feedback
- Headers:
  - X-User-Id: Long (doit etre le rapporteur assigne)
- Body JSON:

```json
{
  "feedback": "Commentaires du rapporteur"
}
```

Effet:

- Met a jour pfe.reporteurFeedback
- Ne modifie pas le status du PFE
- Permet ensuite l'upload etudiant si status == APPROVED

## 5) Nouvelles routes admin

Base: /api/admin/pfe

### 5.1 Cloturer un PFE

- Method: PUT
- Path: /pfe/{id}/cloture
- Headers:
  - X-User-Role: ADMIN

Effet:

- pfe.cloture passe a true
- Uploads etudiant bloques definitivement

### 5.2 Supprimer un PFE

- Method: DELETE
- Path: /pfe/{id}
- Headers:
  - X-User-Role: ADMIN

Effet:

- Le PFE est supprime de la base

## 6) Adaptations frontend conseillees

### 6.1 Affichage etat PFE

Afficher dans la fiche PFE:

- Badge statut (CREATED, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED)
- Badge cloture: Ouvert/Ferme selon cloture
- Bloc feedback rapporteur (si present)

### 6.2 Permissions UI upload (important)

Desactiver les boutons upload rapport/attestation selon cette logique:

- disabled si pfe.cloture === true
- disabled si pfe.status === "UNDER_REVIEW"
- disabled si pfe.status === "APPROVED" ET reporteurFeedback absent
- enabled sinon

Equivalent pseudo-code:

```ts
const hasRapporteurFeedback = !!pfe.reporteurFeedback?.trim();

const uploadBlocked =
  pfe.cloture === true ||
  pfe.status === "UNDER_REVIEW" ||
  (pfe.status === "APPROVED" && !hasRapporteurFeedback);
```

### 6.3 Action rapporteur

Dans l'espace rapporteur:

- Ajouter formulaire textarea + bouton "Envoyer feedback"
- Appeler PUT /api/teacher/pfe/pfe/{pfeId}/rapporteur-feedback
- Apres succes: refresh fiche PFE pour re-evaluer uploadBlocked

### 6.4 Action admin

Dans l'espace admin:

- Bouton "Cloturer PFE" => PUT /api/admin/pfe/pfe/{id}/cloture
- Bouton "Supprimer PFE" => DELETE /api/admin/pfe/pfe/{id}
- Sur suppression: retirer l'element de la liste et afficher confirmation

## 7) Exemple minimal PFEResponse (partiel)

```json
{
  "id": 15,
  "title": "PFE Vision",
  "status": "APPROVED",
  "reporteurFeedback": "Corriger la partie evaluation experimentale.",
  "cloture": false,
  "studentId": 1001,
  "supervisorId": 2001,
  "rapporteurId": 2002
}
```


