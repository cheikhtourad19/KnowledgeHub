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
  loading = false;
  actionLoading = false;

  reportFileUrl: string = '';
  journalFileUrl: string = '';
  attestationFileUrl: string = '';
  conventionFileUrl: string = '';

  // Upload modals
  showUploadReportModal = false;
  showUploadJournalModal = false;
  showUploadAttestationModal = false;
  showUploadConventionModal = false;
  selectedFile: File | null = null;

  // Metadata edit modal
  showEditMetadataModal = false;
  metadataForm = {
    title: '',
    companyName: '',
    durationMonths: null as number | null,
  };

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
        this.loading = true;
        this.loadStageDetails(this.stageId);
      }
    });
  }

  loadStageDetails(stageId: number): void {
    this.resourceApiService.getStageById(stageId).subscribe({
      next: (response) => {
        this.stage = response.data ?? null;
        if (this.stage?.reportFileId)
          this.loadPresignedUrl(this.stage.reportFileId, 'report');
        if (this.stage?.journalFileId)
          this.loadPresignedUrl(this.stage.journalFileId, 'journal');
        if (this.stage?.attestationFileId)
          this.loadPresignedUrl(this.stage.attestationFileId, 'attestation');
        if (this.stage?.conventionFileId)
          this.loadPresignedUrl(this.stage.conventionFileId, 'convention');
        this.loading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les détails du stage',
          life: 5000,
        });
        this.loading = false;
      },
    });
  }

  loadPresignedUrl(
    fileId: number,
    type: 'report' | 'journal' | 'attestation' | 'convention',
  ): void {
    this.resourceApiService.viewpdf(fileId).subscribe({
      next: (response) => {
        const url = response.data ?? '';
        if (type === 'report') this.reportFileUrl = url;
        else if (type === 'journal') this.journalFileUrl = url;
        else if (type === 'attestation') this.attestationFileUrl = url;
        else if (type === 'convention') this.conventionFileUrl = url;
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
    return this.stage?.year === this.currentYear && !this.stage?.cloture;
  }

  canEditMetadata(): boolean {
    return this.stage?.canEdit === true && this.stage?.cloture !== true;
  }

  getTypeStageLabel(typeStage?: string): string {
    if (!typeStage) return '—';
    return typeStage === 'TECHNICIEN' ? 'Technicien' : 'Ouvrier';
  }

  // Returns PrimeNG Tag severity based on stage type
  getTypeStageTagSeverity(typeStage?: string): 'info' | 'warn' | 'secondary' {
    if (!typeStage) return 'secondary';
    return typeStage === 'TECHNICIEN' ? 'info' : 'warn';
  }

  getFileCount(): string {
    let count = 0;
    if (this.stage?.reportFile || this.stage?.reportFileId) count++;
    if (this.stage?.journalFile || this.stage?.journalFileId) count++;
    if (this.stage?.attestationFile || this.stage?.attestationFileId) count++;
    if (this.stage?.conventionFile || this.stage?.conventionFileId) count++;
    return `${count}/4`;
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
  openUploadConventionModal(): void {
    this.showUploadConventionModal = true;
  }

  closeUploadModal(): void {
    this.showUploadReportModal = false;
    this.showUploadJournalModal = false;
    this.showUploadAttestationModal = false;
    this.showUploadConventionModal = false;
    this.selectedFile = null;
  }

  openEditMetadataModal(): void {
    if (!this.stage) return;
    this.metadataForm = {
      title: this.stage.title ?? '',
      companyName: this.stage.companyName ?? '',
      durationMonths: this.stage.durationMonths ?? null,
    };
    this.showEditMetadataModal = true;
  }

  closeEditMetadataModal(): void {
    this.showEditMetadataModal = false;
    this.metadataForm = {
      title: '',
      companyName: '',
      durationMonths: null,
    };
  }

  saveMetadata(): void {
    if (!this.stageId) return;

    const { title, companyName, durationMonths } = this.metadataForm;

    if (!title || !companyName || !durationMonths) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez remplir tous les champs de métadonnées',
        life: 3000,
      });
      return;
    }

    this.actionLoading = true;
    this.resourceApiService
      .updateStageMetadata(this.stageId, {
        title,
        companyName,
        durationMonths,
      })
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Métadonnées mises à jour avec succès',
            life: 3000,
          });
          this.closeEditMetadataModal();
          this.actionLoading = false;
          this.loadStageDetails(this.stageId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de mettre à jour les métadonnées',
            life: 5000,
          });
          this.actionLoading = false;
        },
      });
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
    this.actionLoading = true;
    this.resourceApiService
      .uploadStageReport(this.stageId, this.selectedFile, 'Rapport de stage')
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Rapport uploadé avec succès',
            life: 3000,
          });
          this.closeUploadModal();
          this.actionLoading = false;
          this.loadStageDetails(this.stageId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'upload du rapport",
            life: 5000,
          });
          this.actionLoading = false;
        },
      });
  }

  uploadJournal(): void {
    if (!this.selectedFile || !this.stageId) return;
    this.actionLoading = true;
    this.resourceApiService
      .uploadStageJournal(this.stageId, this.selectedFile, 'Journal de stage')
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Journal uploadé avec succès',
            life: 3000,
          });
          this.closeUploadModal();
          this.actionLoading = false;
          this.loadStageDetails(this.stageId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'upload du journal",
            life: 5000,
          });
          this.actionLoading = false;
        },
      });
  }

  uploadAttestation(): void {
    if (!this.selectedFile || !this.stageId) return;
    this.actionLoading = true;
    this.resourceApiService
      .uploadStageAttestation(
        this.stageId,
        this.selectedFile,
        'Attestation de stage',
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Attestation uploadée avec succès',
            life: 3000,
          });
          this.closeUploadModal();
          this.actionLoading = false;
          this.loadStageDetails(this.stageId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'upload de l'attestation",
            life: 5000,
          });
          this.actionLoading = false;
        },
      });
  }

  uploadConvention(): void {
    if (!this.selectedFile || !this.stageId) return;
    this.actionLoading = true;
    this.resourceApiService
      .uploadStageConvention(
        this.stageId,
        this.selectedFile,
        'Convention de stage',
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Convention uploadée avec succès',
            life: 3000,
          });
          this.closeUploadModal();
          this.actionLoading = false;
          this.loadStageDetails(this.stageId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'upload de la convention",
            life: 5000,
          });
          this.actionLoading = false;
        },
      });
  }

  // ── PDF Modal ──
  openPdfModal(
    type: 'report' | 'journal' | 'attestation' | 'convention',
  ): void {
    const map: Record<typeof type, { url: string; title: string }> = {
      report: { url: this.reportFileUrl, title: 'Rapport de Stage' },
      journal: { url: this.journalFileUrl, title: 'Journal de Stage' },
      attestation: { url: this.attestationFileUrl, title: 'Attestation' },
      convention: { url: this.conventionFileUrl, title: 'Convention de Stage' },
    };

    const { url, title } = map[type];

    if (!url?.trim()) {
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
