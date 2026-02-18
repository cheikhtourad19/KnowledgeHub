import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Classe } from '../../../../shared/models/user-service/entities/classe.model';
import { UserApiService } from '../../../../shared/services/user-api.service';

@Component({
  selector: 'app-students.classe',
  templateUrl: './students.classe.component.html',
  styleUrl: './students.classe.component.scss',
  standalone: false,
  providers: [ConfirmationService, MessageService],
})
export class StudentsClasseComponent implements OnInit {
  classes: Classe[] = [];
  displayDialog: boolean = false;
  classeForm: FormGroup;
  loading: boolean = false;

  constructor(
    private userApiService: UserApiService,
    private fb: FormBuilder,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
  ) {
    this.classeForm = this.fb.group({
      className: ['', [Validators.required, Validators.minLength(2)]],
    });
  }

  ngOnInit(): void {
    this.loadClasses();
  }

  loadClasses(): void {
    this.loading = true;
    this.userApiService.getAllClasses().subscribe({
      next: (response) => {
        this.classes = response.data || [];
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors du chargement des classes',
        });
        this.loading = false;
      },
    });
  }

  showCreateDialog(): void {
    this.classeForm.reset();
    this.displayDialog = true;
  }

  hideDialog(): void {
    this.displayDialog = false;
    this.classeForm.reset();
  }

  createClass(): void {
    if (this.classeForm.valid) {
      const className = this.classeForm.get('className')?.value;
      this.userApiService.createClass(className).subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Classe créée avec succès',
          });
          this.loadClasses();
          this.hideDialog();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Erreur lors de la création de la classe',
          });
        },
      });
    }
  }

  confirmDelete(classe: Classe): void {
    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer la classe "${classe.classeName}" ?`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui',
      rejectLabel: 'Non',
      accept: () => {
        this.deleteClass(classe.classeId);
      },
    });
  }

  deleteClass(id: number): void {
    this.userApiService.deleteClass(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Classe supprimée avec succès',
        });
        this.loadClasses();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors de la suppression de la classe',
        });
      },
    });
  }
}
