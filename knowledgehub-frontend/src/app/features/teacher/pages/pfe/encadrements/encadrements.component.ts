import { Component } from '@angular/core';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { UserApiService } from '../../../../../shared/services/user-api.service';
import { TeacherCVResponse } from '../../../../../shared/models/user-service/responses/teacher-cv-response.model';
import { Router } from '@angular/router';
import { OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
@Component({
  selector: 'app-encadrements',
  standalone: false,
  templateUrl: './encadrements.component.html',
})
export class EncadrementsComponent implements OnInit {
  allPfes: PFE[] = [];
  loading = false;

  // CV
  cv: TeacherCVResponse | null = null;
  cvLoading = false;
  uploadLoading = false;

  // PDF viewer
  showPdfModal = false;
  currentPdfUrl: string = '';
  currentPdfTitle: string = '';

  constructor(
    private resourceApi: ResourceApiService,
    private userApi: UserApiService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.resourceApi.getTeacherSupervisedPFE().subscribe({
      next: (res) => {
        this.allPfes = res.data ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
    this.loadActiveCV();
  }

  viewDetails(pfe: PFE): void {
    this.router.navigate(['/teacher/pfe/encadrements', pfe.id]);
  }

  loadActiveCV(): void {
    this.cvLoading = true;
    this.userApi.getActiveTeacherCV().subscribe({
      next: (res) => {
        this.cv = res.data ?? null;
        this.cvLoading = false;
      },
      error: () => {
        this.cv = null;
        this.cvLoading = false;
      },
    });
  }

  viewCV(): void {
    if (!this.cv) return;
    this.userApi.getActiveCVPresignedUrl().subscribe({
      next: (res) => {
        if (res.data) {
          this.currentPdfUrl = res.data;
          this.currentPdfTitle = this.cv!.fileName;
          this.showPdfModal = true;
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger le CV.',
        });
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];

    if (file.type !== 'application/pdf') {
      this.messageService.add({
        severity: 'error',
        summary: 'Format invalide',
        detail: 'Seuls les fichiers PDF sont acceptés.',
      });
      input.value = '';
      return;
    }

    this.uploadLoading = true;
    this.userApi.uploadTeacherCV(file).subscribe({
      next: (res) => {
        this.uploadLoading = false;
        this.cv = res.data ?? null;
        this.messageService.add({
          severity: 'success',
          summary: 'CV uploadé',
          detail: 'Votre CV a été uploadé et analysé avec succès.',
        });
        input.value = '';
      },
      error: (err) => {
        this.uploadLoading = false;
        input.value = '';
        const detail =
          err.error?.message ||
          err.error?.detail ||
          "Erreur lors de l'upload du CV. Vérifiez que le fichier est un PDF valide.";
        this.messageService.add({
          severity: 'error',
          summary: "Erreur d'upload",
          detail,
        });
      },
    });
  }
}
