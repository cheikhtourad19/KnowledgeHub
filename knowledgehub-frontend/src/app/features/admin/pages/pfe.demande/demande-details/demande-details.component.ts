import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { DemandePFE } from '../../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { DemandStatus } from '../../../../../shared/models/resource-service/enums/demand-status.enum';
import { UpdateDemandStatusRequest } from '../../../../../shared/models/resource-service/requests/update-demand-status-request.model';
import { Teacher } from '../../../../../shared/models/user-service/entities/teacher.model';
import { UserApiService } from '../../../../../shared/services/user-api.service';
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
  pdf_presigned_url: string = '';
  displayApproveDialog: boolean = false;
  displayRejectDialog: boolean = false;
  adminFeedback: string = '';
  teachers: Teacher[] = [];
  selectedTeacherId: number | null = null;
  showPdfModal: boolean = false;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceApiService: ResourceApiService,
    private messageService: MessageService,
    private userService: UserApiService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.demandeId = +params['id'];
      if (this.demandeId) {
        this.loadDemandeDetails();
      }
    });
    this.fetchteachers();
  }
  displayDeleteDialog: boolean = false;

  showDeleteDialog(): void {
    this.displayDeleteDialog = true;
  }

  hideDeleteDialog(): void {
    this.displayDeleteDialog = false;
  }

  deleteDemande(): void {
    if (!this.demandeId) return;

    this.resourceApiService.deleteDemande(this.demandeId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Demande supprimée avec succès',
        });
        this.hideDeleteDialog();
        setTimeout(() => {
          this.router.navigate(['/admin/pfe/demandes']);
        }, 1000);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors de la suppression de la demande',
        });
      },
    });
  }

  fetchteachers() {
    this.userService.getAllTeachers().subscribe({
      next: (response) => {
        this.teachers = (response.data ?? []).map((t: Teacher) => ({
          ...t,
          fullName:
            `${t.prenom ?? ''} ${t.nom ?? ''}`.trim() || t.email || 'Sans nom',
        }));
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les enseignants.',
        });
      },
    });
  }

  loadDemandePdf(): void {
    if (!this.demande || !this.demande.attestationFile) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Avertissement',
        detail: "Le fichier d'attestation n'est pas disponible",
      });
      return;
    }
    this.resourceApiService.viewpdf(this.demande.attestationFile.id).subscribe({
      next: (response) => {
        this.pdf_presigned_url = response.data ?? '';
        console.log('PDF URL:', this.pdf_presigned_url);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Erreur lors du chargement du PDF de l'attestation",
        });
      },
    });
  }

  loadDemandeDetails(): void {
    if (!this.demandeId) return;

    this.resourceApiService.getDemandePFEById(this.demandeId).subscribe({
      next: (response) => {
        this.demande = response.data || null;
        this.loadDemandePdf();
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
      status: DemandStatus.ACCEPTED,
      adminFeedback: this.adminFeedback || undefined,
    };

    this.resourceApiService
      .updateDemandStatus(
        this.demandeId,
        request,
        this.selectedTeacherId || undefined,
      )
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
      case DemandStatus.ACCEPTED:
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
      case DemandStatus.ACCEPTED:
        return 'Acceptée';
      case DemandStatus.REJECTED:
        return 'Rejetée';
      default:
        return status;
    }
  }

  openPdfModal(): void {
    this.showPdfModal = true;
  }

  closePdfModal(): void {
    this.showPdfModal = false;
  }
}
