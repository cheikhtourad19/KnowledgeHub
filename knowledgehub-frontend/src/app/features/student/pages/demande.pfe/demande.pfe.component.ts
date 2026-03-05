import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { DemandStatus } from '../../../../shared/models/resource-service/enums/demand-status.enum';
import { DemandePFE } from '../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function pfeDateValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const dateDebut = group.get('dateDebut')?.value;
    const dateFin = group.get('dateFin')?.value;

    if (!dateDebut || !dateFin) return null;

    const start = new Date(dateDebut);
    const end = new Date(dateFin);
    const currentYear = new Date().getFullYear();
    const errors: ValidationErrors = {};

    // Both dates must be in the current year
    if (start.getFullYear() !== currentYear) {
      errors['startNotCurrentYear'] = true;
    }
    if (end.getFullYear() !== currentYear) {
      errors['endNotCurrentYear'] = true;
    }

    // End must be after start
    if (end <= start) {
      errors['endBeforeStart'] = true;
    }

    // Max 7 months difference
    const diffMonths =
      (end.getFullYear() - start.getFullYear()) * 12 +
      (end.getMonth() - start.getMonth());
    if (diffMonths > 7) {
      errors['exceedsMaxDuration'] = true;
    }

    return Object.keys(errors).length ? errors : null;
  };
}
@Component({
  selector: 'app-demande.pfe',
  standalone: false,
  templateUrl: './demande.pfe.component.html',
  styleUrl: './demande.pfe.component.scss',
})
export class DemandePfeComponent implements OnInit {
  demande: DemandePFE | null = null;
  showModal = false;
  isLoading = false;
  isSubmitting = false;
  selectedFile: globalThis.File | null = null;
  demandeForm: FormGroup;
  DemandStatus = DemandStatus;

  constructor(
    private fb: FormBuilder,
    private ressourceService: ResourceApiService,
    private messageService: MessageService,
  ) {
    this.demandeForm = this.fb.group(
      {
        studentCin: ['', Validators.required],
        studentPhone: ['', Validators.required],
        pfeTitle: ['', Validators.required],
        pfeDescription: ['', Validators.required],
        motCles: ['', Validators.required],
        entrepriseDomaine: ['', Validators.required],
        entrepriseName: ['', Validators.required],
        entrepriseEncadrantName: ['', Validators.required],
        entrepriseEncadrantEmail: ['', [Validators.required, Validators.email]],
        entrepriseEncadrantPhone: ['', Validators.required],
        dateDebut: ['', Validators.required],
        dateFin: ['', Validators.required],
      },
      { validators: pfeDateValidator() },
    );
  }
  currentYear = new Date().getFullYear();

  ngOnInit(): void {
    this.loadMyDemande();
  }

  loadMyDemande(): void {
    this.isLoading = true;
    this.ressourceService.getMyDemandes().subscribe({
      next: (res) => {
        this.demande = res.data?.[0] ?? null;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onFileSelect(event: any): void {
    this.selectedFile = event.files[0];
  }

  openModal(): void {
    this.demandeForm.reset();
    this.selectedFile = null;
    this.showModal = true;
  }

  submitDemande(): void {
    if (this.demandeForm.invalid || !this.selectedFile) {
      this.demandeForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.demandeForm.value;

    this.ressourceService
      .createDemandePFE(
        {
          ...formValue,
          dateDebut: new Date(formValue.dateDebut),
          dateFin: new Date(formValue.dateFin),
        },
        this.selectedFile,
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande envoyée avec succès',
          });
          this.showModal = false;
          this.loadMyDemande();
          this.isSubmitting = false;
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Échec de l'envoi de la demande",
          });
          this.isSubmitting = false;
        },
      });
  }

  getStatusSeverity(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.ACCEPTED:
        return 'success';
      case DemandStatus.REJECTED:
        return 'danger';
      case DemandStatus.UPLOADED:
        return 'warn';
      default:
        return 'info';
    }
  }

  getStatusLabel(status: DemandStatus): string {
    switch (status) {
      case DemandStatus.ACCEPTED:
        return 'Acceptée';
      case DemandStatus.REJECTED:
        return 'Rejetée';
      case DemandStatus.UPLOADED:
        return 'En attente';
      default:
        return status;
    }
  }
}
