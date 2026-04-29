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
import { finalize } from 'rxjs';
import { InternshipConventionExtractionResponse } from '../../../../shared/models/resource-service/responses/internship-convention-extraction-response.model';

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
  isLoading = true;
  showModal = false;
  selectedFile: globalThis.File | null = null;
  extractionLoading = false;
  keywordQuery = '';
  keywordSuggestions: string[] = [];
  selectedKeywords: string[] = [];
  readonly availableKeywords: string[] = [
    'Angular',
    'Spring Boot',
    'Intelligence Artificielle',
    'Machine Learning',
    'Deep Learning',
    'Data Science',
    'Big Data',
    'Cloud',
    'DevOps',
    'Docker',
    'Kubernetes',
    'Microservices',
    'REST API',
    'Cybersécurité',
    'Blockchain',
    'IoT',
    'Mobile',
    'Flutter',
    'Réseaux',
    'Vision par Ordinateur',
  ];
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
    this.isLoading = true;
    this.ressourceService
      .getMyDemandes()
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (res) => {
          this.demande = res.data ?? null;
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
    const file = event.files?.[0] ?? null;

    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
      return;
    }

    this.selectedFile = null;
    this.messageService.add({
      severity: 'warn',
      summary: 'Attention',
      detail: 'Veuillez sélectionner une convention au format PDF',
    });
  }

  openModal(): void {
    this.demandeForm.reset();
    this.selectedFile = null;
    this.extractionLoading = false;
    this.keywordQuery = '';
    this.keywordSuggestions = [];
    this.selectedKeywords = [];
    this.updateMotClesControl();
    this.showModal = true;
  }

  extractConventionData(): void {
    if (!this.selectedFile) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: "Veuillez d'abord sélectionner une convention PDF",
      });
      return;
    }

    this.extractionLoading = true;
    this.ressourceService
      .extractConventionData(this.selectedFile)
      .pipe(finalize(() => (this.extractionLoading = false)))
      .subscribe({
        next: (response) => {
          this.applyConventionExtraction(response);
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Les champs disponibles ont été préremplis',
          });
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail:
              err.error?.message ||
              "Impossible d'extraire les données de la convention",
          });
        },
      });
  }

  private applyConventionExtraction(
    response: InternshipConventionExtractionResponse,
  ): void {
    const patch: Partial<{
      studentCin: string;
      studentPhone: string;
      entrepriseName: string;
      entrepriseDomaine: string;
      entrepriseEncadrantName: string;
      entrepriseEncadrantEmail: string;
      pfeTitle: string;
      dateDebut: Date;
      dateFin: Date;
    }> = {};

    if (response.studentCin) patch.studentCin = response.studentCin;
    if (response.studentPhone) patch.studentPhone = response.studentPhone;
    if (response.entrepriseName) patch.entrepriseName = response.entrepriseName;
    if (response.entrepriseDomaine)
      patch.entrepriseDomaine = response.entrepriseDomaine;
    if (response.entrepriseEncadrantName)
      patch.entrepriseEncadrantName = response.entrepriseEncadrantName;
    if (response.entrepriseEncadrantEmail)
      patch.entrepriseEncadrantEmail = response.entrepriseEncadrantEmail;
    if (response.pfeTitle) patch.pfeTitle = response.pfeTitle;
    if (response.dateDebut)
      patch.dateDebut = this.parseIsoDate(response.dateDebut);
    if (response.dateFin) patch.dateFin = this.parseIsoDate(response.dateFin);

    this.demandeForm.patchValue(patch);
  }

  private parseIsoDate(value: string): Date {
    const [year, month, day] = value.split('-').map((item) => Number(item));
    return new Date(year, month - 1, day);
  }

  searchKeywords(event: { query: string }): void {
    const query = (event.query ?? '').trim().toLowerCase();

    const matches = this.availableKeywords.filter(
      (keyword) =>
        keyword.toLowerCase().includes(query) &&
        !this.selectedKeywords.includes(keyword),
    );

    if (!query) {
      this.keywordSuggestions = matches;
      return;
    }

    const hasExactMatch = this.availableKeywords.some(
      (keyword) => keyword.toLowerCase() === query,
    );

    this.keywordSuggestions = hasExactMatch
      ? matches
      : [event.query.trim(), ...matches];
  }

  onKeywordSelect(event: { value: string }): void {
    this.addKeyword(event.value);
  }

  addTypedKeyword(): void {
    this.addKeyword(this.keywordQuery);
  }

  removeKeyword(keywordToRemove: string): void {
    this.selectedKeywords = this.selectedKeywords.filter(
      (keyword) => keyword !== keywordToRemove,
    );
    this.updateMotClesControl();
  }

  private addKeyword(rawKeyword: string): void {
    const keyword = rawKeyword?.trim();

    if (!keyword) {
      return;
    }

    const exists = this.selectedKeywords.some(
      (selected) => selected.toLowerCase() === keyword.toLowerCase(),
    );

    if (!exists) {
      this.selectedKeywords = [...this.selectedKeywords, keyword];
      const knownKeyword = this.availableKeywords.some(
        (item) => item.toLowerCase() === keyword.toLowerCase(),
      );
      if (!knownKeyword) {
        this.availableKeywords.push(keyword);
      }
    }

    this.keywordQuery = '';
    this.keywordSuggestions = [];
    this.updateMotClesControl();
  }

  private updateMotClesControl(): void {
    this.demandeForm.get('motCles')?.setValue(this.selectedKeywords.join(', '));
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
