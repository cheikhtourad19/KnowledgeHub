import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { DemandePFE } from '../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { DemandStatus } from '../../../../shared/models/resource-service/enums/demand-status.enum';

@Component({
  selector: 'app-pfe.demande',
  templateUrl: './pfe.demande.component.html',
  styleUrl: './pfe.demande.component.scss',
  standalone: false,
  providers: [MessageService],
})
export class PfeDemandeComponent implements OnInit {
  allDemandes: DemandePFE[] = [];
  demandes: DemandePFE[] = [];
  DemandStatus = DemandStatus;

  // Filters
  selectedYear: number = new Date().getFullYear();
  selectedStatus: DemandStatus | null = null;

  // Dropdown options
  yearOptions: { label: string; value: number | null }[] = [];
  statusOptions: { label: string; value: DemandStatus | null }[] = [
    { label: 'Tous les statuts', value: null },
    { label: 'Téléchargée', value: DemandStatus.UPLOADED },
    { label: 'Acceptée', value: DemandStatus.ACCEPTED },
    { label: 'Rejetée', value: DemandStatus.REJECTED },
  ];

  constructor(
    private resourceApiService: ResourceApiService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadDemandes();
  }

  loadDemandes(): void {
    this.resourceApiService.getAllDemandes().subscribe({
      next: (response) => {
        this.allDemandes = response.data || [];
        this.buildYearOptions();
        this.applyFilters();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors du chargement des demandes',
        });
      },
    });
  }

  buildYearOptions(): void {
    const years = [
      ...new Set(
        this.allDemandes.map((d) => new Date(d.createdAt).getFullYear()),
      ),
    ].sort((a, b) => b - a); // descending

    this.yearOptions = [
      { label: 'Toutes les années', value: null },
      ...years.map((y) => ({ label: y.toString(), value: y })),
    ];

    // Ensure current year is available even if no data yet
    const currentYear = new Date().getFullYear();
    if (!years.includes(currentYear)) {
      this.yearOptions.splice(1, 0, {
        label: currentYear.toString(),
        value: currentYear,
      });
    }
  }

  applyFilters(): void {
    this.demandes = this.allDemandes.filter((d) => {
      const matchYear =
        this.selectedYear === null ||
        new Date(d.createdAt).getFullYear() === this.selectedYear;
      const matchStatus =
        this.selectedStatus === null || d.status === this.selectedStatus;
      return matchYear && matchStatus;
    });
  }

  onYearChange(year: number | null): void {
    this.selectedYear = year!;
    this.applyFilters();
  }

  onStatusChange(status: DemandStatus | null): void {
    this.selectedStatus = status;
    this.applyFilters();
  }

  clearFilters(): void {
    this.selectedYear = new Date().getFullYear();
    this.selectedStatus = null;
    this.applyFilters();
  }

  get activeFilterCount(): number {
    let count = 0;
    if (this.selectedYear !== new Date().getFullYear()) count++;
    if (this.selectedStatus !== null) count++;
    return count;
  }

  viewDetails(demande: DemandePFE): void {
    this.router.navigate(['/admin/pfe/demandes', demande.id]);
  }

  getStatusClass(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.UPLOADED:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case DemandStatus.ACCEPTED:
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case DemandStatus.REJECTED:
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400';
    }
  }

  getStatusLabel(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.UPLOADED:
        return 'Téléchargée';
      case DemandStatus.ACCEPTED:
        return 'Acceptée';
      case DemandStatus.REJECTED:
        return 'Rejetée';
      default:
        return status;
    }
  }

  // ══════════════════════════════════════════════════════════
  // ADD these new properties to the component class
  // (alongside the existing filter properties)
  // ══════════════════════════════════════════════════════════

  // Delete-all modal state
  showDeleteAllModal = false;
  deleteConfirmPassword = '';
  deletePasswordError = '';
  deletingAll = false;

  // ══════════════════════════════════════════════════════════
  // ADD these new methods to the component class
  // ══════════════════════════════════════════════════════════

  openDeleteAllModal(): void {
    this.deleteConfirmPassword = '';
    this.deletePasswordError = '';
    this.showDeleteAllModal = true;
  }

  closeDeleteAllModal(): void {
    this.showDeleteAllModal = false;
    this.deleteConfirmPassword = '';
    this.deletePasswordError = '';
  }

  confirmDeleteAll(): void {
    if (!this.deleteConfirmPassword.trim()) {
      this.deletePasswordError = 'Veuillez saisir votre mot de passe.';
      return;
    }

    this.deletingAll = true;
    this.deletePasswordError = '';

    // Step 1 — verify password with the backend
    this.resourceApiService
      .verifyPassword(this.deleteConfirmPassword)
      .subscribe({
        next: () => {
          // Step 2 — password OK → delete all demandes
          this.resourceApiService.deleteAllDemandes().subscribe({
            next: () => {
              this.deletingAll = false;
              this.closeDeleteAllModal();
              this.allDemandes = [];
              this.demandes = [];
              this.messageService.add({
                severity: 'success',
                summary: 'Suppression réussie',
                detail: 'Toutes les demandes de PFE ont été supprimées.',
              });
            },
            error: () => {
              this.deletingAll = false;
              this.messageService.add({
                severity: 'error',
                summary: 'Erreur',
                detail: 'La suppression a échoué. Veuillez réessayer.',
              });
            },
          });
        },
        error: () => {
          this.deletingAll = false;
          this.deletePasswordError =
            'Mot de passe incorrect. Veuillez réessayer.';
        },
      });
  }

  // ══════════════════════════════════════════════════════════
  // ADD these two methods to ResourceApiService
  // (resource-api.service.ts)
  // ══════════════════════════════════════════════════════════

  // Verify the admin password before a destructive action
}
