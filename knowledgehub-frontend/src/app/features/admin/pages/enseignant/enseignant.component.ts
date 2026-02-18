import { Component, OnInit } from '@angular/core';
import { UserApiService } from '../../../../shared/services/user-api.service';
import { Teacher } from '../../../../shared/models/user-service/entities/teacher.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { PrimeNG } from 'primeng/config';
import { BulkUserCreationResult } from '../../../../shared/models/user-service/responses/bulk-user-creation-result.model';

import { UserRole } from '../../../../shared/models/auth-service/entities/auth-user.model';
@Component({
  selector: 'app-enseignant',
  templateUrl: './enseignant.component.html',
  styleUrl: './enseignant.component.scss',
  standalone: false,
})
export class EnseignantComponent implements OnInit {
  teachers: Teacher[] = [];
  createVisible: boolean = false;
  teacherForm!: FormGroup;
  displayBulkCreateModal: boolean = false;
  selectedCsvFile: File | null = null;
  createdUsers: BulkUserCreationResult[] = [];
  notCreatedUsers: BulkUserCreationResult[] = [];
  displayBulkResultsModal: boolean = false;
  activeResultTab: 'created' | 'notCreated' = 'created';
  selectedTeacherId: number | null = null;
  constructor(
    private userService: UserApiService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private config: PrimeNG,
    private confirmationService: ConfirmationService,
  ) {}

  fetchteachers() {
    this.userService.getAllTeachers().subscribe({
      next: (response) => {
        this.teachers = response.data ?? [];
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les enseignants.',
        });
      },
    });
  }

  initForm(): void {
    this.teacherForm = this.fb.group({
      prenom: ['', Validators.required],
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit(): void {
    this.fetchteachers();
    this.initForm();
  }

  openCreateTeacherDialog() {
    this.createVisible = true;
  }

  openBulkCreateModal(): void {
    this.selectedCsvFile = null;
    this.displayBulkCreateModal = true;
  }

  onCsvSelected(event: any): void {
    this.selectedCsvFile = event.currentFiles[0] ?? null;
  }

  onCsvUpload(event: any): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Succès',
      detail: 'Fichier CSV importé avec succès',
      life: 3000,
    });
    this.displayBulkCreateModal = false;
    this.selectedCsvFile = null;
  }

  submitCsv(): void {
    if (!this.selectedCsvFile) return;

    this.userService.bulkCreateTeachers(this.selectedCsvFile).subscribe({
      next: (response) => {
        const data = response.data;
        if (data && data.results) {
          this.createdUsers = data.results.cree ?? [];
          this.notCreatedUsers = data.results.non_cree ?? [];
          this.displayBulkCreateModal = false;
          this.displayBulkResultsModal = true;
          this.fetchteachers();
          const totalCreated = this.createdUsers.length;
          const totalNotCreated = this.notCreatedUsers.length;
          if (totalCreated > 0) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: `${totalCreated} enseignant(s) créé(s) avec succès${totalNotCreated > 0 ? `, ${totalNotCreated} non créé(s)` : ''}`,
            });
          } else if (totalNotCreated > 0) {
            this.messageService.add({
              severity: 'warn',
              summary: 'Attention',
              detail: `Aucun enseignant créé. ${totalNotCreated} utilisateur(s) déjà existant(s) ou invalide(s)`,
            });
          }
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de créer les enseignants en masse',
        });
      },
    });
  }

  deleteTeacher(event: Event, id: number): void {
    event.stopPropagation();

    this.selectedTeacherId = id;
    console.log('Selected teacher ID for deletion:', this.selectedTeacherId); // Now this should show correctly

    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Êtes-vous sûr de vouloir supprimer cet enseignant ?',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Non, annuler',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        const teacherIdToDelete = this.selectedTeacherId;
        console.log('Accept - Deleting teacher ID:', teacherIdToDelete);

        if (teacherIdToDelete) {
          this.userService.deleteUser(teacherIdToDelete).subscribe({
            next: (response) => {
              this.messageService.add({
                severity: 'success',
                summary: 'Succès',
                detail: 'Enseignant supprimé avec succès',
              });
              this.fetchteachers();
              this.selectedTeacherId = null;
              console.log('Delete successful for teacher ID:', response);
            },
            error: (err) => {
              console.error('Delete error:', err);
              this.messageService.add({
                severity: 'error',
                summary: 'Erreur',
                detail: "Impossible de supprimer l'enseignant.",
              });
              this.selectedTeacherId = null;
            },
          });
        } else {
          console.error('No teacher ID found for deletion');
        }
      },
      reject: () => {
        console.log('Delete rejected for teacher ID:', this.selectedTeacherId); // Debug log
        this.selectedTeacherId = null;
      },
    });
  }
  closeBulkResultsModal(): void {
    this.displayBulkResultsModal = false;
    this.createdUsers = [];
    this.notCreatedUsers = [];
    this.fetchteachers();
  }
  formatSize(bytes: number): string {
    const k = 1024;
    const sizes = this.config.translation.fileSizeTypes ?? ['B', 'KB', 'MB'];
    if (bytes === 0) return `0 ${sizes[0]}`;
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  }

  onSubmit() {
    if (!this.teacherForm.valid) {
      return;
    }
    const teacher: any = {
      email: this.teacherForm.value.email,
      nom: this.teacherForm.value.nom,
      prenom: this.teacherForm.value.prenom,
      password: this.teacherForm.value.password,
      role: UserRole.TEACHER,
    };
    this.userService.createTeacher(teacher).subscribe({
      next: (response) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Enseignant créé avec succès',
        });
        this.createVisible = false;
        this.teacherForm.reset();
        this.fetchteachers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Impossible de créer l'enseignant.",
        });
      },
    });
  }
}
