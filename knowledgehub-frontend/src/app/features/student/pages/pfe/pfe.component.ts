import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
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
  demandes: DemandePFE[] = [];
  pendingDemande: DemandePFE | null = null;
  rejectedDemande: DemandePFE | null = null;

  // Store presigned URLs separately
  reportFileUrl: string | null = null;
  attestationFileUrl: string | null = null;

  // Upload modal
  showUploadModal = false;
  selectedFile: File | null = null;
  uploadLoading = false;

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
    // Load student's PFE
    this.resourceApi.getStudentPFE().subscribe({
      next: (response) => {
        console.log('PFE Response:', response);
        // Backend returns a list
        const pfeList = response.data ?? [];
        this.pfe = pfeList.length > 0 ? pfeList[0] : null;

        console.log('Parsed PFE:', this.pfe);

        // If student has a PFE, load the presigned URLs for files
        if (this.pfe?.reportFile?.id) {
          this.loadFileUrl(this.pfe.reportFile.id, 'report');
        }
        if (this.pfe?.attestationFile?.id) {
          this.loadFileUrl(this.pfe.attestationFile.id, 'attestation');
        }
      },
      error: (err) => {
        console.error('Error loading PFE:', err);
      },
    });

    // Load student's demandes
    this.resourceApi.getMyDemandes().subscribe({
      next: (response) => {
        this.demandes = response.data ?? [];

        // Find pending or rejected demand
        this.pendingDemande = this.demandes.find(
          (d) => d.status === DemandStatus.UPLOADED
        ) ?? null;

        this.rejectedDemande = this.demandes.find(
          (d) => d.status === DemandStatus.REJECTED
        ) ?? null;
      },
      error: () => {},
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
      case SubmissionStatus.SUBMITTED:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
      case SubmissionStatus.UNDER_REVIEW:
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300';
      case SubmissionStatus.APPROVED:
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
      case SubmissionStatus.REJECTED:
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  }

  getStatusLabel(status: SubmissionStatus): string {
    switch (status) {
      case SubmissionStatus.SUBMITTED:
        return 'Soumis';
      case SubmissionStatus.UNDER_REVIEW:
        return 'En révision';
      case SubmissionStatus.APPROVED:
        return 'Approuvé';
      case SubmissionStatus.REJECTED:
        return 'Rejeté';
      default:
        return status;
    }
  }

  canUploadReport(): boolean {
    // Can't upload if there's already a report file
    if (this.pfe?.reportFile) {
      return false;
    }
    // Can't upload if PFE is approved (validated)
    return this.pfe?.status !== SubmissionStatus.APPROVED;
  }

  navigateToDemandes(): void {
    this.router.navigate(['/student/pfe/demandes']);
  }

  // Upload modal methods
  openUploadModal(): void {
    this.showUploadModal = true;
    this.selectedFile = null;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.selectedFile = null;
    this.uploadLoading = false;
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
    if (!this.selectedFile || !this.pfe?.id) return;

    this.uploadLoading = true;
    this.resourceApi
      .uploadPFEReport(this.pfe.id, this.selectedFile, 'Rapport PFE')
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
            detail: err.error?.message || 'Erreur lors de l\'upload du rapport',
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
