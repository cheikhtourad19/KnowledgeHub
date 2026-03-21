import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { PFE } from '../../../../shared/models/resource-service/entities/pfe.model';
import { SubmissionStatus } from '../../../../shared/models/resource-service/enums/submission-status.enum';

@Component({
  selector: 'app-pfe.gestion',
  templateUrl: './pfe.gestion.component.html',
  styleUrl: './pfe.gestion.component.scss',
  standalone: false,
})
export class PfeGestionComponent implements OnInit {
  allPfes: PFE[] = [];
  pfes: PFE[] = [];
  SubmissionStatus = SubmissionStatus;

  searchQuery: string = '';
  selectedYear: number | null = new Date().getFullYear();
  selectedStatus: SubmissionStatus | null = null;

  yearOptions: { label: string; value: number | null }[] = [];
  statusOptions: { label: string; value: SubmissionStatus | null }[] = [
    { label: 'Tous les statuts', value: null },
    { label: 'En Attente', value: SubmissionStatus.DRAFT },
    { label: 'Soumis', value: SubmissionStatus.SUBMITTED },
    { label: 'Validé', value: SubmissionStatus.APPROVED },
    { label: 'A revoir', value: SubmissionStatus.REVISION_REQUIRED },
  ];

  constructor(
    private ressourceApiService: ResourceApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadPFEs();
  }

  loadPFEs(): void {
    this.ressourceApiService.getAllPFE().subscribe({
      next: (response) => {
        this.allPfes = response.data ?? [];
        this.buildYearOptions();
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading PFEs:', error);
      },
    });
  }

  buildYearOptions(): void {
    const years = [
      ...new Set(
        this.allPfes.map(
          (p) => p.year ?? new Date(p.createdDate).getFullYear(),
        ),
      ),
    ].sort((a, b) => b - a);

    this.yearOptions = [
      { label: 'Toutes les années', value: null },
      ...years.map((y) => ({ label: y.toString(), value: y })),
    ];

    const currentYear = new Date().getFullYear();
    if (!years.includes(currentYear)) {
      this.yearOptions.splice(1, 0, {
        label: currentYear.toString(),
        value: currentYear,
      });
    }
  }

  applyFilters(): void {
    const query = this.searchQuery.trim().toLowerCase();

    this.pfes = this.allPfes.filter((p) => {
      const pYear = p.year ?? new Date(p.createdDate).getFullYear();
      const matchYear =
        this.selectedYear === null || pYear === this.selectedYear;
      const matchStatus =
        this.selectedStatus === null || p.status === this.selectedStatus;
      const matchSearch =
        !query ||
        p.title?.toLowerCase().includes(query) ||
        p.studentName?.toLowerCase().includes(query) ||
        p.studentEmail?.toLowerCase().includes(query) ||
        p.supervisorName?.toLowerCase().includes(query) ||
        p.entreprise_name?.toLowerCase().includes(query) ||
        p.id?.toString().includes(query);

      return matchYear && matchStatus && matchSearch;
    });
  }

  onSearchChange(value: string): void {
    this.searchQuery = value;
    this.applyFilters();
  }

  onYearChange(year: number | null): void {
    this.selectedYear = year;
    this.applyFilters();
  }

  onStatusChange(status: SubmissionStatus | null): void {
    this.selectedStatus = status;
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedYear = new Date().getFullYear();
    this.selectedStatus = null;
    this.applyFilters();
  }

  get activeFilterCount(): number {
    let count = 0;
    if (this.searchQuery.trim()) count++;
    if (this.selectedYear !== new Date().getFullYear()) count++;
    if (this.selectedStatus !== null) count++;
    return count;
  }

  viewDetails(pfe: PFE): void {
    this.router.navigate(['/admin/pfe-details', pfe.id]);
  }

  getStatusClass(status: SubmissionStatus): string {
    switch (status) {
      case SubmissionStatus.DRAFT:
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case SubmissionStatus.SUBMITTED:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case SubmissionStatus.APPROVED:
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case SubmissionStatus.REJECTED:
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400';
    }
  }

  getStatusLabel(status: SubmissionStatus): string {
    switch (status) {
      case SubmissionStatus.DRAFT:
        return 'En Attente';
      case SubmissionStatus.SUBMITTED:
        return 'Soumis';
      case SubmissionStatus.APPROVED:
        return 'Validé';
      case SubmissionStatus.REJECTED:
        return 'A revoir';
      default:
        return status;
    }
  }
}
