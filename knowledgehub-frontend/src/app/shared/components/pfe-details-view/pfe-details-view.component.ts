import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PFE } from '../../models/resource-service/entities/pfe.model';
import { ResourceApiService } from '../../services/resource-api.service';
import { SharedModule } from '../../shared.module';
@Component({
  selector: 'app-pfe-details-view',
  standalone: true,
  imports: [CommonModule, SharedModule],
  templateUrl: './pfe-details-view.component.html',
})
export class PfeDetailsViewComponent implements OnChanges {
  @Input() pfe!: PFE;

  reportFileUrl: string = '';
  showPdfModal = false;

  constructor(private resourceApi: ResourceApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pfe'] && this.pfe?.reportFile?.id) {
      this.loadReportUrl(this.pfe.reportFile.id);
    }
  }

  private loadReportUrl(fileId: number): void {
    this.resourceApi.viewpdf(fileId).subscribe({
      next: (response) => {
        this.reportFileUrl = response.data ?? '';
      },
      error: () => {
        this.reportFileUrl = '';
      },
    });
  }

  openPdfModal(): void {
    this.showPdfModal = true;
  }
  closePdfModal(): void {
    this.showPdfModal = false;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'SUBMITTED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'VALIDATED':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'En Attente';
      case 'SUBMITTED':
        return 'Soumis';
      case 'VALIDATED':
        return 'Validé';
      case 'REJECTED':
        return 'Rejeté';
      default:
        return status;
    }
  }
}
