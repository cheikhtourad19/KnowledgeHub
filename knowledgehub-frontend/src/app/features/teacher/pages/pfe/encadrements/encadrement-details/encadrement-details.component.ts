import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../../shared/services/resource-api.service';
import { PFE } from '../../../../../../shared/models/resource-service/entities/pfe.model';
import { MessageService } from 'primeng/api';
import { UpdatePFEStatusRequest } from '../../../../../../shared/models/resource-service/requests/update-pfe-status-request.model';
import { SubmissionStatus } from '../../../../../../shared/models/resource-service/enums/submission-status.enum';

@Component({
  selector: 'app-encadrement-details',
  standalone: false,
  templateUrl: './encadrement-details.component.html',
})
export class EncadrementDetailsComponent implements OnInit {
  pfe: PFE | null = null;
  submitting = false;
  markingUnderReview = false;

  selectedStatus: SubmissionStatus | null = null;
  supervisorFeedback: string = '';

  SubmissionStatus = SubmissionStatus;

  constructor(
    private resourceApi: ResourceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    if (id) {
      this.resourceApi.getPFEById(id).subscribe({
        next: (res) => {
          this.pfe = res.data ?? null;
          const status = this.pfe?.status;
          if (
            status === SubmissionStatus.APPROVED ||
            status === SubmissionStatus.REJECTED
          ) {
            this.selectedStatus = status;
          }
          this.supervisorFeedback = this.pfe?.supervisorFeedback ?? '';
        },
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de charger le PFE.',
          }),
      });
    }
  }

  get isRejecting(): boolean {
    return this.selectedStatus === SubmissionStatus.REJECTED;
  }

  get isAlreadyUnderReview(): boolean {
    return this.pfe?.status === SubmissionStatus.UNDER_REVIEW;
  }

  get canSubmit(): boolean {
    if (!this.selectedStatus) return false;
    if (this.isRejecting && !this.supervisorFeedback.trim()) return false;
    return true;
  }

  markAsUnderReview(): void {
    if (!this.pfe) return;
    this.markingUnderReview = true;

    const request: UpdatePFEStatusRequest = {
      status: SubmissionStatus.UNDER_REVIEW,
    };

    this.resourceApi.updatePFESubmissionStatus(this.pfe.id, request).subscribe({
      next: (res) => {
        this.pfe = res.data ?? this.pfe;
        this.messageService.add({
          severity: 'info',
          summary: "En cours d'examen",
          detail: "Le PFE est maintenant bloqué pour l'étudiant.",
        });
        this.markingUnderReview = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Échec de la mise à jour du statut.',
        });
        this.markingUnderReview = false;
      },
    });
  }

  submitStatus(): void {
    if (!this.pfe || !this.selectedStatus) return;
    this.submitting = true;

    const request: UpdatePFEStatusRequest = {
      status: this.selectedStatus!,
      supervisorFeedback: this.supervisorFeedback.trim() || undefined,
    };

    this.resourceApi.updatePFESubmissionStatus(this.pfe.id, request).subscribe({
      next: (res) => {
        this.pfe = res.data ?? this.pfe;
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Décision enregistrée avec succès.',
        });
        this.submitting = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Échec de la mise à jour du statut.',
        });
        this.submitting = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['../'], { relativeTo: this.route });
  }
}
