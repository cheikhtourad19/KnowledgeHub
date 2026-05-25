import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../../shared/services/resource-api.service';
import { Stage } from '../../../../../../shared/models/resource-service/entities/stage.model';
import { Teacher } from '../../../../../../shared/models/user-service/entities/teacher.model';
import { UserApiService } from '../../../../../../shared/services/user-api.service';
import { MessageService } from 'primeng/api';

interface TeacherOption extends Teacher {
  id: number;
  fullName: string;
}

@Component({
  selector: 'app-stage-details',
  standalone: false,
  templateUrl: './stage-details.component.html',
  styleUrl: './stage-details.component.scss',
})
export class StageDetailsComponent implements OnInit {
  stage_details: Stage | null = null;
  stage_id: number = 0;
  list_teachers: TeacherOption[] = [];
  loading = false;
  actionLoading = false;

  // Jury modal
  showSupervisorModal = false;
  selectedSupervisorAuthId: number | null = null;

  // Status confirmation modal
  showStatusConfirmModal = false;

  reportFileUrl: string = '';
  journalFileUrl: string = '';
  attestationFileUrl: string = '';
  conventionFileUrl: string = '';

  // PDF Viewer Modal
  showPdfModal = false;
  currentPdfUrl: string = '';
  currentPdfTitle: string = '';

  constructor(
    private resourceApiService: ResourceApiService,
    private userService: UserApiService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.stage_id = +params['id'];
      if (this.stage_id) {
        this.loadStageDetails(this.stage_id);
      }
    });
  }

  loadStageDetails(stageId: number): void {
    this.loading = true;
    this.resourceApiService.getStageById(stageId).subscribe({
      next: (response) => {
        this.loading = false;
        this.stage_details = response.data ?? null;

        // Load presigned URLs for files
        if (this.stage_details?.reportFileId) {
          this.loadPresignedUrl(this.stage_details.reportFileId, 'report');
        }
        if (this.stage_details?.journalFileId) {
          this.loadPresignedUrl(this.stage_details.journalFileId, 'journal');
        }
        if (this.stage_details?.attestationFileId) {
          this.loadPresignedUrl(
            this.stage_details.attestationFileId,
            'attestation',
          );
        }
        if (this.stage_details?.conventionFileId) {
          this.loadPresignedUrl(
            this.stage_details.conventionFileId,
            'convention',
          );
        }
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les détails du stage',
          life: 5000,
        });
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
        if (type === 'report') {
          this.reportFileUrl = url;
        } else if (type === 'journal') {
          this.journalFileUrl = url;
        } else if (type === 'attestation') {
          this.attestationFileUrl = url;
        } else if (type === 'convention') {
          this.conventionFileUrl = url;
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
    this.router.navigate(['/teacher/stage/jury']);
  }

  openStatusConfirmModal(): void {
    this.showStatusConfirmModal = true;
  }

  closeStatusConfirmModal(): void {
    this.showStatusConfirmModal = false;
  }

  confirmStageStatus(): void {
    if (!this.stage_id) return;

    const request = this.stage_details?.cloture
      ? this.resourceApiService.openStageByTeacher(this.stage_id)
      : this.resourceApiService.closeStageByTeacher(this.stage_id);

    this.actionLoading = true;
    request.subscribe({
      next: () => {
        this.actionLoading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: this.stage_details?.cloture
            ? 'Le stage a été rouvert'
            : 'Le stage a été clôturé',
          life: 3000,
        });
        this.closeStatusConfirmModal();
        this.loadStageDetails(this.stage_id);
      },
      error: () => {
        this.actionLoading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de clôturer le stage',
          life: 5000,
        });
      },
    });
  }

  // ── Jury Modal ──

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
    if (this.stage_details?.reportFile || this.stage_details?.reportFileId)
      count++;
    if (this.stage_details?.journalFile || this.stage_details?.journalFileId)
      count++;
    if (
      this.stage_details?.attestationFile ||
      this.stage_details?.attestationFileId
    )
      count++;
    if (
      this.stage_details?.conventionFile ||
      this.stage_details?.conventionFileId
    )
      count++;
    return `${count}/4`;
  }

  // ── PDF Modal ──
  openPdfModal(
    type: 'report' | 'journal' | 'attestation' | 'convention',
  ): void {
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
      case 'convention':
        url = this.conventionFileUrl;
        title = 'Convention de Stage';
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
