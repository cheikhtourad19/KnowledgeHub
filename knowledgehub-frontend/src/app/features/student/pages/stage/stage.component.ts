import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { Stage } from '../../../../shared/models/resource-service/entities/stage.model';
import { TypeStage } from '../../../../shared/models/resource-service/enums/type-stage.enum';
import { CreateStageRequest } from '../../../../shared/models/resource-service/requests/create-stage-request.model';

@Component({
  selector: 'app-stage',
  standalone: false,
  templateUrl: './stage.component.html',
  styleUrl: './stage.component.scss',
})
export class StageComponent implements OnInit {
  stages: Stage[] = [];
  filteredStages: Stage[] = [];
  hasAuthorization = false;
  authorizationType: TypeStage | null = null;

  // Year filter
  selectedYear: number | null = null;
  availableYears: number[] = [];
  currentYear: number = new Date().getFullYear();

  // Create stage modal
  showCreateModal = false;
  stageForm = {
    title: '',
    companyName: '',
    durationMonths: null as number | null,
  };

  constructor(
    private resourceApi: ResourceApiService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadStudentStages();
    this.checkAuthorization();
  }

  loadStudentStages(): void {
    this.resourceApi.getMyStage().subscribe({
      next: (response) => {
        console.log('Stages Response:', response);
        this.stages = response.data ?? [];
        this.extractYears();
        this.applyFilter();
      },
      error: (err) => {
        console.error('Error loading stages:', err);
      },
    });
  }

  checkAuthorization(): void {
    this.resourceApi.checkStageAuthorization().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.hasAuthorization = true;
          this.authorizationType = response.data.typeStage;
        } else {
          this.hasAuthorization = false;
        }
      },
      error: () => {
        this.hasAuthorization = false;
      },
    });
  }

  // Year filter methods
  extractYears(): void {
    const years = new Set<number>();
    this.stages.forEach((stage) => {
      if (stage.year) {
        years.add(stage.year);
      }
    });
    this.availableYears = Array.from(years).sort((a, b) => b - a); // Desc order

    // Default to current year if available
    if (this.availableYears.includes(this.currentYear)) {
      this.selectedYear = this.currentYear;
    }
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

  canEditStage(stage: Stage): boolean {
    // Can only edit stages from the current year
    return stage.year === this.currentYear;
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

  // Create stage modal methods
  openCreateModal(): void {
    this.showCreateModal = true;
    this.stageForm = {
      title: '',
      companyName: '',
      durationMonths: null,
    };
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.stageForm = {
      title: '',
      companyName: '',
      durationMonths: null,
    };
  }

  createStage(): void {
    if (
      !this.stageForm.title ||
      !this.stageForm.companyName ||
      !this.stageForm.durationMonths
    ) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez remplir tous les champs',
      });
      return;
    }

    const request: CreateStageRequest = {
      title: this.stageForm.title,
      companyName: this.stageForm.companyName,
      durationMonths: this.stageForm.durationMonths,
    };

    this.resourceApi.createStage(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Stage créé avec succès',
        });
        this.closeCreateModal();
        this.loadStudentStages();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.message || 'Erreur lors de la création du stage',
        });
      },
    });
  }

  // Navigation to stage details
  navigateToStageDetails(stageId: number): void {
    this.router.navigate(['/student/stage', stageId]);
  }

  getFileCount(stage: Stage): string {
    let count = 0;
    if (stage.reportFileId) count++;
    if (stage.journalFileId) count++;
    if (stage.attestationFileId) count++;
    return `${count}/3`;
  }

  hasCurrentYearStage(): boolean {
    return this.stages.some((stage) => stage.year === this.currentYear);
  }

  getYearOptions(): { label: string; value: number }[] {
    return this.availableYears.map((year) => ({
      label: year.toString(),
      value: year,
    }));
  }
}
