import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { Stage } from '../../../../../shared/models/resource-service/entities/stage.model';
import { Teacher } from '../../../../../shared/models/user-service/entities/teacher.model';
import { UserApiService } from '../../../../../shared/services/user-api.service';
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

  // Jury modal
  showSupervisorModal = false;
  selectedSupervisorId: number | null = null;

  reportFileUrl: string = '';
  journalFileUrl: string = '';
  attestationFileUrl: string = '';

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
    this.loadTeachers();
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
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les enseignants',
          life: 5000,
        });
      },
    });
  }

  loadStageDetails(stageId: number): void {
    this.resourceApiService.getStageById(stageId).subscribe({
      next: (response) => {
        this.stage_details = response.data ?? null;

        // Load presigned URLs for files
        if (this.stage_details?.reportFileId) {
          this.loadPresignedUrl(this.stage_details.reportFileId, 'report');
        }
        if (this.stage_details?.journalFileId) {
          this.loadPresignedUrl(this.stage_details.journalFileId, 'journal');
        }
        if (this.stage_details?.attestationFileId) {
          this.loadPresignedUrl(this.stage_details.attestationFileId, 'attestation');
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
    this.router.navigate(['/admin/stage']);
  }

  // ── Jury Modal ──
  openSupervisorModal(): void {
    this.selectedSupervisorId = this.stage_details?.supervisorId ?? null;
    this.showSupervisorModal = true;
  }

  closeSupervisorModal(): void {
    this.showSupervisorModal = false;
    this.selectedSupervisorId = null;
  }

  confirmSupervisor(): void {
    if (!this.selectedSupervisorId) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez sélectionner un jury',
        life: 3000,
      });
      return;
    }

    this.resourceApiService
      .assignStageSupervisor(this.stage_id, this.selectedSupervisorId)
      .subscribe({
        next: () => {
          // Update locally
          if (this.stage_details) {
            const teacher = this.list_teachers.find(
              (t) => t.teacherId === this.selectedSupervisorId,
            );
            this.stage_details = {
              ...this.stage_details,
              supervisorId: this.selectedSupervisorId!,
              supervisorName: teacher?.fullName ?? '',
            };
          }
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Jury assigné avec succès',
            life: 3000,
          });
          this.closeSupervisorModal();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'assignation du jury",
            life: 5000,
          });
        },
      });
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
    if (this.stage_details?.reportFile) count++;
    if (this.stage_details?.journalFile) count++;
    if (this.stage_details?.attestationFile) count++;
    return `${count}/3`;
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
        detail: 'Le fichier n\'est pas encore chargé',
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
