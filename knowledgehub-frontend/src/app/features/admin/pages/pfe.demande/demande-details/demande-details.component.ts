import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { DemandePFE } from '../../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { DemandStatus } from '../../../../../shared/models/resource-service/enums/demand-status.enum';
import { UpdateDemandStatusRequest } from '../../../../../shared/models/resource-service/requests/update-demand-status-request.model';

@Component({
  selector: 'app-demande-details',
  templateUrl: './demande-details.component.html',
  styleUrls: ['./demande-details.component.scss'],
  standalone: false,
  providers: [MessageService],
})
export class DemandeDetailsComponent implements OnInit {
  demande: DemandePFE | null = null;
  demandeId: number | null = null;
  DemandStatus = DemandStatus;

  // Dialog states
  displayApproveDialog: boolean = false;
  displayRejectDialog: boolean = false;
  adminFeedback: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceApiService: ResourceApiService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.demandeId = +params['id'];
      if (this.demandeId) {
        this.loadDemandeDetails();
      }
    });
  }

  loadDemandeDetails(): void {
    if (!this.demandeId) return;

    this.resourceApiService.getDemandePFEById(this.demandeId).subscribe({
      next: (response) => {
        this.demande = response.data || null;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors du chargement de la demande',
        });
        this.router.navigate(['/admin/pfe/demandes']);
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/pfe/demandes']);
  }

  showApproveDialog(): void {
    this.adminFeedback = '';
    this.displayApproveDialog = true;
  }

  showRejectDialog(): void {
    this.adminFeedback = '';
    this.displayRejectDialog = true;
  }

  hideApproveDialog(): void {
    this.displayApproveDialog = false;
    this.adminFeedback = '';
  }

  hideRejectDialog(): void {
    this.displayRejectDialog = false;
    this.adminFeedback = '';
  }

  approveDemand(): void {
    if (!this.demandeId) return;

    const request: UpdateDemandStatusRequest = {
      status: DemandStatus.APPROVED,
      adminFeedback: this.adminFeedback || undefined,
    };

    this.resourceApiService
      .updateDemandStatus(this.demandeId, request)
      .subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande approuvée et PFE créé avec succès',
          });
          this.hideApproveDialog();
          this.router.navigate(['/admin/pfe/demandes']);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Erreur lors de l'approbation de la demande",
          });
        },
      });
  }

  rejectDemand(): void {
    if (!this.demandeId) return;

    if (!this.adminFeedback || this.adminFeedback.trim() === '') {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Le feedback est requis pour rejeter une demande',
      });
      return;
    }

    const request: UpdateDemandStatusRequest = {
      status: DemandStatus.REJECTED,
      adminFeedback: this.adminFeedback,
    };

    this.resourceApiService
      .updateDemandStatus(this.demandeId, request)
      .subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande rejetée avec succès',
          });
          this.hideRejectDialog();
          this.router.navigate(['/admin/pfe/demandes']);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Erreur lors du rejet de la demande',
          });
        },
      });
  }

  getStatusClass(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.UPLOADED:
        return 'bg-blue-100 text-blue-800';
      case DemandStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case DemandStatus.APPROVED:
        return 'bg-green-100 text-green-800';
      case DemandStatus.REJECTED:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusLabel(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.UPLOADED:
        return 'Téléchargée';
      case DemandStatus.PENDING:
        return 'En Attente';
      case DemandStatus.APPROVED:
        return 'Approuvée';
      case DemandStatus.REJECTED:
        return 'Rejetée';
      default:
        return status;
    }
  }
}
