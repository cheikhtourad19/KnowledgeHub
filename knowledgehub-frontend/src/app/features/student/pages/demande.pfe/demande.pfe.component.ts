import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MessageService } from 'primeng/api';
import { DemandStatus } from '../../../../shared/models/resource-service/enums/demand-status.enum';
import { DemandePFE } from '../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';

export function pfeDateValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const dateDebut = group.get('dateDebut')?.value;
    const dateFin = group.get('dateFin')?.value;

    if (!dateDebut || !dateFin) return null;

    // p-datePicker binds Date objects directly
    const start = dateDebut instanceof Date ? dateDebut : new Date(dateDebut);
    const end = dateFin instanceof Date ? dateFin : new Date(dateFin);
    const currentYear = new Date().getFullYear();
    const errors: ValidationErrors = {};

    if (start.getFullYear() !== currentYear)
      errors['startNotCurrentYear'] = true;
    if (end.getFullYear() !== currentYear) errors['endNotCurrentYear'] = true;
    if (end <= start) errors['endBeforeStart'] = true;

    const diffMonths =
      (end.getFullYear() - start.getFullYear()) * 12 +
      (end.getMonth() - start.getMonth());
    if (diffMonths > 7) errors['exceedsMaxDuration'] = true;

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
  selectedFile: globalThis.File | null = null;
  demandeForm: FormGroup;
  DemandStatus = DemandStatus;
  currentYear = new Date().getFullYear();

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
        motCles: [''],
        entrepriseDomaine: ['', Validators.required],
        entrepriseName: ['', Validators.required],
        entrepriseEncadrantName: ['', Validators.required],
        entrepriseEncadrantEmail: ['', [Validators.required, Validators.email]],
        entrepriseEncadrantPhone: ['', Validators.required],
        dateDebut: [null, Validators.required],
        dateFin: [null, Validators.required],
      },
      { validators: pfeDateValidator() },
    );
  }

  ngOnInit(): void {
    this.loadMyDemande();
  }

  loadMyDemande(): void {
    this.ressourceService.getMyDemandes().subscribe({
      next: (res) => {
        this.demande = res.data?.[0] ?? null;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger votre demande',
          life: 5000,
        });
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

    const formValue = this.demandeForm.value;

    this.ressourceService
      .createDemandePFE(
        {
          ...formValue,
          // Values are already Date objects from p-datePicker
          dateDebut: formValue.dateDebut,
          dateFin: formValue.dateFin,
        },
        this.selectedFile,
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande envoyée avec succès',
            life: 3000,
          });
          this.showModal = false;
          this.loadMyDemande();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Échec de l'envoi de la demande",
            life: 5000,
          });
        },
      });
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
