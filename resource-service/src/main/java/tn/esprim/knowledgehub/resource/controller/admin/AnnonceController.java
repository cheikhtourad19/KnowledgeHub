package tn.esprim.knowledgehub.resource.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.AnnonceStage;
import tn.esprim.knowledgehub.resource.enums.ModaliteTravail;
import tn.esprim.knowledgehub.resource.enums.TypeAnnonceStage;
import tn.esprim.knowledgehub.resource.service.AnnonceService;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@Slf4j
public class AnnonceController {

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    // ── lecture (public) ─────────────────────────────────────────

    @Operation(summary = "Lister toutes les annonces")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> findAll() {
        log.info("Fetching all annonces");
        return ResponseEntity.ok(ApiResponse.success(annonceService.findAll(),null));
    }

    @Operation(summary = "Lister les annonces actives")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> findAllActive() {
        log.info("Fetching active annonces");
        return ResponseEntity.ok(ApiResponse.success(annonceService.findAllActive(),null));
    }

    @Operation(summary = "Détail d'une annonce")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnonceStage>> findById(@PathVariable Long id) {
        try {
            log.info("Fetching annonce {}", id);
            return ResponseEntity.ok(ApiResponse.success(annonceService.findById(id),null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage(), "Annonce introuvable"));
        }
    }

    @Operation(summary = "Filtrer par type de stage")
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> findByType(
            @PathVariable TypeAnnonceStage type) {
        log.info("Fetching annonces by type {}", type);
        return ResponseEntity.ok(ApiResponse.success(annonceService.findByType(type),null));
    }

    @Operation(summary = "Filtrer par modalité")
    @GetMapping("/modalite/{modalite}")
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> findByModalite(
            @PathVariable ModaliteTravail modalite) {
        log.info("Fetching annonces by modalite {}", modalite);
        return ResponseEntity.ok(ApiResponse.success(annonceService.findByModalite(modalite),null));
    }

    @Operation(summary = "Filtrer par ville")
    @GetMapping("/ville/{ville}")
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> findByVille(
            @PathVariable String ville) {
        log.info("Fetching annonces by ville {}", ville);
        return ResponseEntity.ok(ApiResponse.success(annonceService.findByVille(ville),null));
    }

    @Operation(summary = "Recherche libre")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AnnonceStage>>> search(
            @RequestParam String keyword) {
        log.info("Searching annonces keyword='{}'", keyword);
        return ResponseEntity.ok(ApiResponse.success(annonceService.search(keyword),null));
    }

    // ── écriture (admin uniquement) ──────────────────────────────

    @Operation(summary = "Créer une annonce avec image optionnelle")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AnnonceStage>> save(
            @RequestPart("annonce") AnnonceStage annonce,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can create annonces"));
        }

        try {
            log.info("Admin creating annonce, image={}", image != null ? image.getOriginalFilename() : "none");
            return ResponseEntity.status(201).body(
                    ApiResponse.success(annonceService.save(annonce, image),null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "Données invalides"));
        } catch (Exception e) {
            log.error("Error creating annonce: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "Failed to create annonce"));
        }
    }

    @Operation(summary = "Modifier une annonce avec image optionnelle")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AnnonceStage>> edit(
            @PathVariable Long id,
            @RequestPart("annonce") AnnonceStage annonce,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can edit annonces"));
        }

        try {
            log.info("Admin editing annonce {}, image={}", id, image != null ? image.getOriginalFilename() : "none");
            return ResponseEntity.ok(ApiResponse.success(annonceService.edit(id, annonce, image),null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage(), "Annonce introuvable"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "Données invalides"));
        } catch (Exception e) {
            log.error("Error editing annonce {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "Failed to edit annonce"));
        }
    }

    @Operation(summary = "Activer / désactiver une annonce")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<AnnonceStage>> toggleActive(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can toggle annonce status"));
        }

        try {
            log.info("Admin toggling annonce {}", id);
            return ResponseEntity.ok(ApiResponse.success(annonceService.toggleActive(id),null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage(), "Annonce introuvable"));
        }
    }

    @Operation(summary = "Supprimer une annonce")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can delete annonces"));
        }

        try {
            log.info("Admin deleting annonce {}", id);
            annonceService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Annonce supprimée"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage(), "Annonce introuvable"));
        } catch (Exception e) {
            log.error("Error deleting annonce {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "Failed to delete annonce"));
        }
    }
}