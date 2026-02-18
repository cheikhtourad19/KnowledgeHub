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
  demandes: DemandePFE[] = [];
  DemandStatus = DemandStatus;

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
        this.demandes = response.data || [];
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors du chargement des demandes',
        });
      },
    });
  }

  viewDetails(demande: DemandePFE): void {
    this.router.navigate(['/admin/pfe/demandes', demande.id]);
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
