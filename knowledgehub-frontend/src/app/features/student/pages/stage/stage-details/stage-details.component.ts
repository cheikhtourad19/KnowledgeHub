import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { Stage } from '../../../../../shared/models/resource-service/entities/stage.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-student-stage-details',
  standalone: false,
  templateUrl: './stage-details.component.html',
  styleUrl: './stage-details.component.scss',
})
export class StudentStageDetailsComponent implements OnInit {
  stage: Stage | null = null;
  stageId: number = 0;
  currentYear = new Date().getFullYear();

  reportFileUrl: string = '';
  journalFileUrl: string = '';
  attestationFileUrl: string = '';

  // Upload modals
  showUploadReportModal = false;
  showUploadJournalModal = false;
  showUploadAttestationModal = false;
  selectedFile: File | null = null;
  uploadLoading = false;

  // PDF Viewer Modal
  showPdfModal = false;
  currentPdfUrl: string = '';
  currentPdfTitle: string = '';

  constructor(
    private resourceApiService: ResourceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.stageId = +params['id'];
      if (this.stageId) {
        this.loadStageDetails(this.stageId);
      }
    });
  }

  loadStageDetails(stageId: number): void {
    this.resourceApiService.getStageById(stageId).subscribe({
      next: (response) => {
        this.stage = response.data ?? null;

        // Load presigned URLs for files
        if (this.stage?.reportFileId) {
          this.loadPresignedUrl(this.stage.reportFileId, 'report');
        }
        if (this.stage?.journalFileId) {
          this.loadPresignedUrl(this.stage.journalFileId, 'journal');
        }
        if (this.stage?.attestationFileId) {
          this.loadPresignedUrl(this.stage.attestationFileId, 'attestation');
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les détails du stage',
          life: 5000,
        });
      },
    });
  }

  loadPresignedUrl(fileId: number, type: 'report' | 'journal' | 'attestation'): void {
    this.resourceApiService.viewpdf(fileId).subscribe({
      next: (response) => {
        const url = response.data ?? '';
        if (type === 'report') {
          this.reportFileUrl = url;
        } else if (type === 'journal') {
          this.journalFileUrl = url;
        } else if (type === 'attestation') {
          this.attestationFileUrl = url;
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: `Erreur lors du chargement du fichier ${type}`,
          life: 5000,
        });
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/student/stage']);
  }

  canEditStage(): boolean {
    return this.stage?.year === this.currentYear;
  }

  getTypeStageLabel(typeStage?: string): string {
    if (!typeStage) return '—';
    return typeStage === 'TECHNICIEN' ? 'Technicien' : 'Ouvrier';
  }

  getTypeStageClass(typeStage?: string): string {
    if (!typeStage)
      return 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400';
    return typeStage === 'TECHNICIEN'
      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
      : 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400';
  }

  getFileCount(): string {
    let count = 0;
    if (this.stage?.reportFile) count++;
    if (this.stage?.journalFile) count++;
    if (this.stage?.attestationFile) count++;
    return `${count}/3`;
  }

  // ── Upload Modals ──
  openUploadReportModal(): void {
    this.showUploadReportModal = true;
  }

  openUploadJournalModal(): void {
    this.showUploadJournalModal = true;
  }

  openUploadAttestationModal(): void {
    this.showUploadAttestationModal = true;
  }

  closeUploadModal(): void {
    this.showUploadReportModal = false;
    this.showUploadJournalModal = false;
    this.showUploadAttestationModal = false;
    this.selectedFile = null;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Veuillez sélectionner un fichier PDF',
        life: 3000,
      });
      this.selectedFile = null;
    }
  }

  uploadReport(): void {
    if (!this.selectedFile || !this.stageId) return;

    this.uploadLoading = true;
    this.resourceApiService.uploadStageReport(this.stageId, this.selectedFile, 'Rapport de stage').subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Rapport uploadé avec succès',
          life: 3000,
        });
        this.closeUploadModal();
        this.loadStageDetails(this.stageId);
        this.uploadLoading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Erreur lors de l'upload du rapport",
          life: 5000,
        });
        this.uploadLoading = false;
      },
    });
  }

  uploadJournal(): void {
    if (!this.selectedFile || !this.stageId) return;

    this.uploadLoading = true;
    this.resourceApiService.uploadStageJournal(this.stageId, this.selectedFile, 'Journal de stage').subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Journal uploadé avec succès',
          life: 3000,
        });
        this.closeUploadModal();
        this.loadStageDetails(this.stageId);
        this.uploadLoading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Erreur lors de l'upload du journal",
          life: 5000,
        });
        this.uploadLoading = false;
      },
    });
  }

  uploadAttestation(): void {
    if (!this.selectedFile || !this.stageId) return;

    this.uploadLoading = true;
    this.resourceApiService.uploadStageAttestation(this.stageId, this.selectedFile, 'Attestation de stage').subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Attestation uploadée avec succès',
          life: 3000,
        });
        this.closeUploadModal();
        this.loadStageDetails(this.stageId);
        this.uploadLoading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Erreur lors de l'upload de l'attestation",
          life: 5000,
        });
        this.uploadLoading = false;
      },
    });
  }

  // ── PDF Modal ──
  openPdfModal(type: 'report' | 'journal' | 'attestation'): void {
    let url = '';
    let title = '';

    switch (type) {
      case 'report':
        url = this.reportFileUrl;
        title = 'Rapport de Stage';
        break;
      case 'journal':
        url = this.journalFileUrl;
        title = 'Journal de Stage';
        break;
      case 'attestation':
        url = this.attestationFileUrl;
        title = 'Attestation';
        break;
    }

    if (!url || url.trim().length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: "Le fichier n'est pas encore chargé",
        life: 3000,
      });
      return;
    }

    this.currentPdfUrl = url;
    this.currentPdfTitle = title;
    this.showPdfModal = true;
  }

  closePdfModal(): void {
    this.showPdfModal = false;
    this.currentPdfUrl = '';
    this.currentPdfTitle = '';
  }
}
