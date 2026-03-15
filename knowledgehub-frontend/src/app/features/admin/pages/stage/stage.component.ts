import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { UserApiService } from '../../../../shared/services/user-api.service';
import { Stage } from '../../../../shared/models/resource-service/entities/stage.model';
import { TypeStage } from '../../../../shared/models/resource-service/enums/type-stage.enum';
import { Classe } from '../../../../shared/models/user-service/entities/classe.model';
import { Student } from '../../../../shared/models/user-service/entities/student.model';

@Component({
  selector: 'app-stage',
  templateUrl: './stage.component.html',
  styleUrl: './stage.component.scss',
  standalone: false,
})
export class StageComponent implements OnInit {
  stages: Stage[] = [];
  filteredStages: Stage[] = [];
  loading = false;

  // Year filter
  selectedYear: number | null = null;
  availableYears: number[] = [];

  // Modal state
  showAuthorizationModal = false;
  authorizationType: 'student' | 'class' = 'student';

  // Remove authorizations modal
  showRemoveAuthorizationsModal = false;

  // Student authorization
  selectedStudentId: number | null = null;
  students: Student[] = [];

  // Class authorization
  selectedClassId: number | null = null;
  classes: Classe[] = [];

  // Type de stage
  selectedTypeStage: TypeStage | null = null;
  typeStageOptions = [
    { label: 'Technicien', value: TypeStage.TECHNICIEN },
    { label: 'Ouvrier', value: TypeStage.OUVRIER },
  ];

  constructor(
    private resourceApiService: ResourceApiService,
    private userApiService: UserApiService,
    private messageService: MessageService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadStages();
    this.loadClasses();
    this.loadStudents();
  }

  loadStages(): void {
    this.loading = true;
    this.resourceApiService.getAllStages().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.stages = response.data;
          this.extractYears();
          this.applyFilter();
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: `${this.stages.length} stage(s) chargé(s)`,
            life: 3000,
          });
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading stages:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les stages',
          life: 5000,
        });
        this.loading = false;
      },
    });
  }

  loadClasses(): void {
    this.userApiService.getAllClasses().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.classes = response.data;
        }
      },
      error: (error) => {
        console.error('Error loading classes:', error);
      },
    });
  }

  loadStudents(): void {
    this.userApiService.getAllStudents().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.students = response.data;
        }
      },
      error: (error) => {
        console.error('Error loading students:', error);
      },
    });
  }

  openAuthorizationModal(): void {
    this.showAuthorizationModal = true;
    this.authorizationType = 'student';
    this.selectedStudentId = null;
    this.selectedClassId = null;
    this.selectedTypeStage = null;
  }

  closeAuthorizationModal(): void {
    this.showAuthorizationModal = false;
    this.authorizationType = 'student';
    this.selectedStudentId = null;
    this.selectedClassId = null;
    this.selectedTypeStage = null;
  }

  confirmAuthorization(): void {
    if (!this.selectedTypeStage) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez sélectionner un type de stage',
        life: 3000,
      });
      return;
    }

    if (this.authorizationType === 'student') {
      this.authorizeStudent();
    } else {
      this.authorizeClass();
    }
  }

  authorizeStudent(): void {
    if (!this.selectedStudentId || !this.selectedTypeStage) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez sélectionner un étudiant et un type de stage',
        life: 3000,
      });
      return;
    }

    this.loading = true;
    this.resourceApiService
      .authorizeStudent(this.selectedStudentId, this.selectedTypeStage)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: response.message || 'Étudiant autorisé avec succès',
              life: 3000,
            });
            this.closeAuthorizationModal();
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error authorizing student:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Impossible d'autoriser l'étudiant",
            life: 5000,
          });
          this.loading = false;
        },
      });
  }

  authorizeClass(): void {
    if (!this.selectedClassId || !this.selectedTypeStage) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez sélectionner une classe et un type de stage',
        life: 3000,
      });
      return;
    }

    this.loading = true;
    this.resourceApiService
      .authorizeClass(this.selectedClassId, this.selectedTypeStage)
      .subscribe({
        next: (response) => {
          if (response.success && response.data !== undefined) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail:
                response.message ||
                `${response.data} étudiant(s) autorisé(s) avec succès`,
              life: 3000,
            });
            this.closeAuthorizationModal();
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error authorizing class:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Impossible d'autoriser la classe",
            life: 5000,
          });
          this.loading = false;
        },
      });
  }

  getTypeStageLabel(typeStage?: TypeStage): string {
    if (!typeStage) return '—';
    return typeStage === TypeStage.TECHNICIEN ? 'Technicien' : 'Ouvrier';
  }

  getTypeStageClass(typeStage?: TypeStage): string {
    if (!typeStage)
      return 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400';
    return typeStage === TypeStage.TECHNICIEN
      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
      : 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400';
  }

  getStudentName(studentId: number): string {
    const student = this.students.find((s) => s.studentId === studentId);
    return student ? `${student.nom} ${student.prenom}` : 'N/A';
  }

  getClassName(classId: number): string {
    const classe = this.classes.find((c) => c.classeId === classId);
    return classe?.classeName || 'N/A';
  }

  viewStageDetails(stageId: number): void {
    this.router.navigate(['/admin/stage', stageId]);
  }

  getFileCount(stage: Stage): string {
    let count = 0;
    if (stage.reportFile || stage.reportFileId) count++;
    if (stage.journalFile || stage.journalFileId) count++;
    if (stage.attestationFile || stage.attestationFileId) count++;
    return `${count}/3`;
  }

  // ── Year Filter Methods ──
  extractYears(): void {
    const years = new Set<number>();
    this.stages.forEach((stage) => {
      if (stage.year) {
        years.add(stage.year);
      }
    });
    this.availableYears = Array.from(years).sort((a, b) => b - a); // Desc order
  }

  applyFilter(): void {
    if (this.selectedYear === null) {
      this.filteredStages = [...this.stages];
    } else {
      this.filteredStages = this.stages.filter(
        (stage) => stage.year === this.selectedYear,
      );
    }
  }

  onYearChange(): void {
    this.applyFilter();
  }

  clearFilter(): void {
    this.selectedYear = null;
    this.applyFilter();
  }

  // ── Remove All Authorizations Modal ──
  openRemoveAuthorizationsModal(): void {
    this.showRemoveAuthorizationsModal = true;
  }

  closeRemoveAuthorizationsModal(): void {
    this.showRemoveAuthorizationsModal = false;
  }

  confirmRemoveAllAuthorizations(): void {
    this.loading = true;
    this.resourceApiService.removeAllStageAuthorizations().subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail:
              response.message || 'Toutes les autorisations ont été supprimées',
            life: 3000,
          });
          this.closeRemoveAuthorizationsModal();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error removing authorizations:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de supprimer les autorisations',
          life: 5000,
        });
        this.loading = false;
      },
    });
  }
}
