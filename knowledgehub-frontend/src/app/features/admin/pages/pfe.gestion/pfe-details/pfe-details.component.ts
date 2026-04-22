import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { Teacher } from '../../../../../shared/models/user-service/entities/teacher.model';
import { UserApiService } from '../../../../../shared/services/user-api.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-pfe-details',
  standalone: false,
  templateUrl: './pfe-details.component.html',
  styleUrl: './pfe-details.component.scss',
})
export class PfeDetailsComponent implements OnInit {
  pfe_details: PFE | null = null;
  pfe_id: number = 0;
  list_teachers: TeacherOption[] = [];

  // Jury modal
  showJuryModal = false;
  selectedJuryId: number | null = null;

  // Rapporteur modal
  showRapporteurModal = false;
  selectedRapporteurId: number | null = null;

  // Confirmation modals
  showClotureConfirmModal = false;
  showDeleteConfirmModal = false;

  reportFileUrl: string = '';
  showPdfModal: boolean = false;

  constructor(
    private ressourceApiService: ResourceApiService,
    private userService: UserApiService,
    private ressourceApi: ResourceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.pfe_id = +params['id'];
      if (this.pfe_id) {
        this.loadPfeDetails(this.pfe_id);
      }
    });
    this.loadTeachers();
  }

  signedUrl(fileId: number) {
    this.ressourceApiService.viewpdf(fileId).subscribe({
      next: (response) => {
        this.reportFileUrl = response.data ?? '';
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Erreur lors du chargement du PDF de l'attestation",
        });
      },
    });
  }

  loadTeachers(): void {
    this.userService.getAllTeachers().subscribe({
      next: (response) => {
        this.list_teachers = (response.data ?? []).map((t: Teacher) => ({
          ...t,
          id: Number(t.teacherId),
          fullName:
            `${t.prenom ?? ''} ${t.nom ?? ''}`.trim() || t.email || 'Sans nom',
        }));
      },
      error: () => {},
    });
  }

  loadPfeDetails(pfeId: number): void {
    this.ressourceApiService.getPFEById(pfeId).subscribe({
      next: (response) => {
        this.pfe_details = response.data ?? null;
        if (this.pfe_details?.reportFile?.id) {
          this.signedUrl(this.pfe_details.reportFile.id);
        } else {
          this.reportFileUrl = '';
        }
      },
      error: () => {},
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/pfe/gestion']);
  }

  openClotureConfirmModal(): void {
    if (!this.pfe_details) return;
    this.showClotureConfirmModal = true;
  }

  closeClotureConfirmModal(): void {
    this.showClotureConfirmModal = false;
  }

  confirmCloturePFE(): void {
    if (!this.pfe_id) return;

    const request = this.pfe_details?.cloture
      ? this.ressourceApiService.cancelPFEClotureByAdmin(this.pfe_id)
      : this.ressourceApiService.closePFEByAdmin(this.pfe_id);

    request.subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: this.pfe_details?.cloture
            ? 'La clôture du PFE a été annulée.'
            : 'Le PFE a été clôturé.',
        });
        this.closeClotureConfirmModal();
        this.loadPfeDetails(this.pfe_id);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: this.pfe_details?.cloture
            ? 'Impossible de rouvrir ce PFE.'
            : 'Impossible de clôturer ce PFE.',
        });
      },
    });
  }

  openDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
  }

  confirmDeletePFE(): void {
    if (!this.pfe_id) return;

    this.ressourceApiService.deletePFEByAdmin(this.pfe_id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'PFE supprimé avec succès.',
        });
        this.closeDeleteConfirmModal();
        this.router.navigate(['/admin/pfe/gestion']);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de supprimer ce PFE.',
        });
      },
    });
  }

  // ── Jury Modal ──
  openJuryModal(): void {
    this.selectedJuryId = this.pfe_details?.chefDeJuryId ?? null;
    this.showJuryModal = true;
  }
  closeJuryModal(): void {
    this.showJuryModal = false;
    this.selectedJuryId = null;
  }
  confirmJury(): void {
    if (!this.selectedJuryId) return;

    this.ressourceApi
      .assignJury(this.pfe_id, this.selectedJuryId, undefined)
      .subscribe({
        next: () => {
          // Patch locally — don't rely on API to return resolved names
          if (this.pfe_details) {
            this.pfe_details = {
              ...this.pfe_details,
              chefDeJuryId: this.selectedJuryId!,
              chefDeJuryName:
                this.list_teachers.find(
                  (t) => t.teacherId === this.selectedJuryId,
                )?.fullName ?? '',
            };
          }
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Jury assigné avec succès.',
          });
          this.closeJuryModal();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'assignation du jury.",
          });
        },
      });
  }

  // ── Rapporteur Modal ──
  openRapporteurModal(): void {
    this.selectedRapporteurId = this.pfe_details?.rapporteurId ?? null;
    this.showRapporteurModal = true;
  }
  closeRapporteurModal(): void {
    this.showRapporteurModal = false;
    this.selectedRapporteurId = null;
  }
  confirmRapporteur(): void {
    if (!this.selectedRapporteurId) return;

    this.ressourceApi
      .assignJury(this.pfe_id, undefined, this.selectedRapporteurId)
      .subscribe({
        next: () => {
          if (this.pfe_details) {
            this.pfe_details = {
              ...this.pfe_details,
              rapporteurId: this.selectedRapporteurId!,
              rapporteurName:
                this.list_teachers.find(
                  (t) => t.teacherId === this.selectedRapporteurId,
                )?.fullName ?? '',
            };
          }
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Rapporteur assigné avec succès.',
          });
          this.closeRapporteurModal();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'assignation du rapporteur.",
          });
        },
      });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'DRAFT':
      case 'CREATED':
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'SUBMITTED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'UNDER_REVIEW':
        return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/30 dark:text-indigo-300';
      case 'APPROVED':
      case 'VALIDATED':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      case 'REVISION_REQUIRED':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'DRAFT':
      case 'CREATED':
      case 'PENDING':
        return 'Créé';
      case 'SUBMITTED':
        return 'Soumis';
      case 'UNDER_REVIEW':
        return 'En révision';
      case 'APPROVED':
      case 'VALIDATED':
        return 'Validé';
      case 'REJECTED':
        return 'Rejeté';
      case 'REVISION_REQUIRED':
        return 'Révision requise';
      default:
        return status;
    }
  }

  openPdfModal(): void {
    this.showPdfModal = true;
  }

  closePdfModal(): void {
    this.showPdfModal = false;
  }
}

interface TeacherOption extends Teacher {
  fullName: string;
}
