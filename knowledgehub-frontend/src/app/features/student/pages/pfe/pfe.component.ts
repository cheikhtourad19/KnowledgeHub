import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { PFE } from '../../../../shared/models/resource-service/entities/pfe.model';
import { DemandePFE } from '../../../../shared/models/resource-service/entities/demande-pfe.model';
import { DemandStatus } from '../../../../shared/models/resource-service/enums/demand-status.enum';
import { SubmissionStatus } from '../../../../shared/models/resource-service/enums/submission-status.enum';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-pfe',
  standalone: false,
  templateUrl: './pfe.component.html',
  styleUrl: './pfe.component.scss',
})
export class PfeComponent implements OnInit {
  pfe: PFE | null = null;
  demandes: DemandePFE | null = null;
  pendingDemande: DemandePFE | null = null;
  rejectedDemande: DemandePFE | null = null;
  loading = false;

  // Store presigned URLs separately
  reportFileUrl: string | null = null;
  attestationFileUrl: string | null = null;

  // Upload modal
  showUploadModal = false;
  selectedFile: File | null = null;
  reportTitle: string = '';
  reportDescription: string = '';
  uploadLoading = false;

  showUploadAttestationModal = false;
  selectedAttestationFile: File | null = null;
  attestationTitle: string = '';
  attestationDescription: string = '';
  uploadAttestationLoading = false;

  // PDF viewer modal
  showPdfModal = false;
  pdfUrl = '';
  pdfTitle = '';

  constructor(
    private resourceApi: ResourceApiService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadStudentData();
  }

  loadStudentData(): void {
    this.loading = true;

    forkJoin({
      pfe: this.resourceApi.getStudentPFE(),
      demandes: this.resourceApi.getMyDemandes(),
    }).subscribe({
      next: (results) => {
        const pfeList = results.pfe.data ?? [];
        this.pfe = pfeList.length > 0 ? pfeList[0] : null;

        if (this.pfe?.reportFile?.id) {
          this.loadFileUrl(this.pfe.reportFile.id, 'report');
        }
        if (this.pfe?.attestationFile?.id) {
          this.loadFileUrl(this.pfe.attestationFile.id, 'attestation');
        }

        this.demandes = results.demandes.data ?? null;
        this.pendingDemande =
          this.demandes && this.demandes.status === DemandStatus.UPLOADED ? this.demandes : null;
        this.rejectedDemande =
          this.demandes && this.demandes.status === DemandStatus.REJECTED ? this.demandes : null;

        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading data:', err);
        this.loading = false;
      },
    });
  }

  loadFileUrl(fileId: number, type: 'report' | 'attestation'): void {
    this.resourceApi.viewpdf(fileId).subscribe({
      next: (response) => {
        const url = response.data ?? '';
        if (type === 'report') {
          this.reportFileUrl = url;
        } else if (type === 'attestation') {
          this.attestationFileUrl = url;
        }
      },
      error: () => {},
    });
  }

  getStatusClass(status: SubmissionStatus): string {
    switch (status) {
      case SubmissionStatus.DRAFT:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
      case SubmissionStatus.SUBMITTED:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
      case SubmissionStatus.UNDER_REVIEW:
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300';
      case SubmissionStatus.APPROVED:
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
      case SubmissionStatus.REJECTED:
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
      case SubmissionStatus.REVISION_REQUIRED:
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  }

  getStatusLabel(status: SubmissionStatus): string {
    switch (status) {
      case SubmissionStatus.DRAFT:
        return 'Créé';
      case SubmissionStatus.SUBMITTED:
        return 'Soumis';
      case SubmissionStatus.UNDER_REVIEW:
        return 'En révision';
      case SubmissionStatus.APPROVED:
        return 'Approuvé';
      case SubmissionStatus.REJECTED:
        return 'Rejeté';
      case SubmissionStatus.REVISION_REQUIRED:
        return 'Révision requise';
      default:
        return status;
    }
  }

  canUploadReport(): boolean {
    return !this.isUploadBlocked();
  }

  canUploadAttestation(): boolean {
    return !this.isUploadBlocked();
  }

  hasRapporteurFeedback(): boolean {
    return !!this.pfe?.reporteurFeedback?.trim();
  }

  isUploadBlocked(): boolean {
    if (!this.pfe) {
      return true;
    }

    if (this.pfe.cloture === true) {
      return true;
    }

    if (this.pfe.status === SubmissionStatus.UNDER_REVIEW) {
      return true;
    }

    if (
      this.pfe.status === SubmissionStatus.APPROVED &&
      !this.hasRapporteurFeedback()
    ) {
      return true;
    }

    return false;
  }

  getUploadBlockedReason(): string {
    if (!this.pfe) {
      return '';
    }

    if (this.pfe.cloture === true) {
      return "Ce PFE est clôturé. Aucun upload de document n'est autorisé.";
    }

    if (this.pfe.status === SubmissionStatus.UNDER_REVIEW) {
      return 'Le PFE est en cours de révision. Les uploads sont temporairement bloqués.';
    }

    if (
      this.pfe.status === SubmissionStatus.APPROVED &&
      !this.hasRapporteurFeedback()
    ) {
      return 'Upload bloqué après validation encadrant. Il sera réactivé après feedback du rapporteur.';
    }

    return '';
  }

  navigateToDemandes(): void {
    this.router.navigate(['/student/pfe/demandes']);
  }

  // Upload modal methods
  openUploadModal(): void {
    this.showUploadModal = true;
    this.selectedFile = null;
    this.reportTitle = this.pfe?.reportFile?.title || this.pfe?.title || '';
    this.reportDescription = this.pfe?.reportFile?.description || '';
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.selectedFile = null;
    this.reportTitle = '';
    this.reportDescription = '';
    this.uploadLoading = false;
  }

  openUploadAttestationModal(): void {
    this.showUploadAttestationModal = true;
    this.selectedAttestationFile = null;
    this.attestationTitle =
      this.pfe?.attestationFile?.title ||
      (this.pfe?.title ? `Attestation - ${this.pfe.title}` : 'Attestation PFE');
    this.attestationDescription = this.pfe?.attestationFile?.description || '';
  }

  closeUploadAttestationModal(): void {
    this.showUploadAttestationModal = false;
    this.selectedAttestationFile = null;
    this.attestationTitle = '';
    this.attestationDescription = '';
    this.uploadAttestationLoading = false;
  }

  onFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Veuillez sélectionner un fichier PDF',
      });
    }
  }

  uploadReport(): void {
    if (!this.selectedFile || !this.pfe?.id || !this.reportTitle.trim()) return;

    this.uploadLoading = true;
    this.resourceApi
      .uploadPFEReport(
        this.pfe.id,
        this.selectedFile,
        this.reportTitle.trim(),
        this.reportDescription.trim() || undefined,
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Rapport uploadé avec succès',
          });
          this.closeUploadModal();
          this.loadStudentData(); // Reload to show the new file
        },
        error: (err) => {
          this.uploadLoading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: err.error?.message || "Erreur lors de l'upload du rapport",
          });
        },
      });
  }

  onAttestationFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file && file.type === 'application/pdf') {
      this.selectedAttestationFile = file;
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Veuillez sélectionner un fichier PDF',
      });
    }
  }

  uploadAttestation(): void {
    if (!this.selectedAttestationFile || !this.pfe?.id) return;

    this.uploadAttestationLoading = true;
    this.resourceApi
      .uploadPFEAttestation(
        this.pfe.id,
        this.selectedAttestationFile,
        this.attestationTitle.trim() || undefined,
        this.attestationDescription.trim() || undefined,
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Attestation uploadée avec succès',
          });
          this.closeUploadAttestationModal();
          this.loadStudentData();
        },
        error: (err) => {
          this.uploadAttestationLoading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail:
              err.error?.message || "Erreur lors de l'upload de l'attestation",
          });
        },
      });
  }

  // PDF viewer methods
  openPdfModal(fileUrl: string, title: string): void {
    this.pdfUrl = fileUrl;
    this.pdfTitle = title;
    this.showPdfModal = true;
  }

  closePdfModal(): void {
    this.showPdfModal = false;
    this.pdfUrl = '';
    this.pdfTitle = '';
  }

  viewReport(): void {
    if (this.reportFileUrl) {
      this.openPdfModal(this.reportFileUrl, 'Rapport PFE');
    }
  }

  viewAttestation(): void {
    if (this.attestationFileUrl) {
      this.openPdfModal(this.attestationFileUrl, 'Attestation');
    }
  }
}
