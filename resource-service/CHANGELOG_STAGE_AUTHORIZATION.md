# Changement - Gestion des autorisations de stage (multi-type)

## Résumé

Un étudiant peut désormais avoir plusieurs autorisations de stage (une par type : TECHNICIEN / OUVRIER) dans une même année. L'étudiant choisit le type de stage lors de la création.

---

## 1. Admin - Création d'autorisation

**POST** `/api/admin/stage/authorize/student/{studentId}?typeStage=TECHNICIEN`

### Nouveau comportement
- Si l'étudiant a **déjà une autorisation du même type** pour l'année en cours → rien ne change (pas de doublon)
- Si l'étudiant a **une autorisation d'un type différent** → une **nouvelle** autorisation est créée (l'ancienne est conservée)
- L'étudiant peut donc avoir 2 autorisations simultanément (une TECHNICIEN + une OUVRIER)

### Changement côté frontend
- Aucun changement sur cet endpoint (même signature)

---

## 2. Student - Création de stage

**POST** `/api/student/stage` (multipart/form-data)

### Changement dans la request
Nouveau champ obligatoire ajouté :

| Champ | Type | Obligatoire | Description |
|-------|------|-------------|-------------|
| `typeStage` | `string` (TECHNICIEN / OUVRIER) | Oui | Type de stage choisi par l'étudiant |

### Nouveau comportement
- L'étudiant **choisit** le type de stage dans la request
- Le service vérifie que l'étudiant a bien une **autorisation pour CE type** (plus une autorisation générique)
- Si l'autorisation de ce type n'existe pas → erreur
- Une fois le stage créé, l'autorisation de ce **type spécifique** est supprimée (l'autre type reste disponible)

### Exemple de body multipart
```
title: "Stage en développement"
companyName: "Tech Corp"
durationMonths: 3
typeStage: TECHNICIEN
conventionFile: (file)
```

---

## 3. Student - Vérification des autorisations

**GET** `/api/student/stage/check-authorization`

### Changement dans la réponse
Avant : retournait un **objet unique** (`StageCreationAutorisation`) ou `null`

Après : retourne une **liste** d'autorisations (`StageCreationAutorisation[]`) — peut être vide `[]`

### Structure de la réponse (data)
```json
[
  {
    "id": 1,
    "studentId": 123,
    "typeStage": "TECHNICIEN",
    "createdAt": "2026-05-20T10:00:00.000+00:00"
  },
  {
    "id": 2,
    "studentId": 123,
    "typeStage": "OUVRIER",
    "createdAt": "2026-05-20T10:30:00.000+00:00"
  }
]
```

### Adaptation frontend
- Remplacer `if (data !== null)` par `if (data.length > 0)`
- Afficher les types disponibles à l'étudiant pour qu'il choisisse
- Passer le `typeStage` choisi dans la requête de création

---

## 4. Admin - Autorisation de classe

**POST** `/api/admin/stage/authorize/class/{classId}?typeStage=TECHNICIEN`

Aucun changement de signature. La logique interne suit les mêmes règles que l'autorisation individuelle (multi-type).

---

## 5. Admin - Suppression d'un stage

**DELETE** `/api/admin/stage/{stageId}`

Aucun changement de signature. L'autorisation supprimée est désormais celle correspondant au **type du stage supprimé** (et non plus n'importe quelle autorisation de l'étudiant).

---

## Résumé pour le frontend

| Endpoint | Changement |
|----------|-----------|
| `POST /api/student/stage` | Ajout du champ `typeStage` (obligatoire) |
| `GET /api/student/stage/check-authorization` | Retourne une **liste** au lieu d'un objet unique |
| `POST /api/admin/stage/authorize/student/{studentId}` | Aucun changement d'interface |
| `POST /api/admin/stage/authorize/class/{classId}` | Aucun changement d'interface |
