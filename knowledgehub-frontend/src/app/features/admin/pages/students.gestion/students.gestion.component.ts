import { Component, OnInit } from '@angular/core';
import { UserApiService } from '../../../../shared/services/user-api.service';
import { Student } from '../../../../shared/models/user-service/entities/student.model';
import { Classe } from '../../../../shared/models/user-service/entities/classe.model';
import { CreateStudentRequest } from '../../../../shared/models/user-service/requests/create-student-request.model';
import { BulkUserCreationResponse } from '../../../../shared/models/user-service/responses/bulk-user-creation-response.model';
import { BulkUserCreationResult } from '../../../../shared/models/user-service/responses/bulk-user-creation-result.model';
import { ConfirmationService, MessageService } from 'primeng/api';
import { RegisterRequest } from '../../../../shared/models/auth-service/requests/register-request.model';
import { AuthApiService } from '../../../auth/services/auth-api.service';

@Component({
  selector: 'app-students.gestion',
  templateUrl: './students.gestion.component.html',
  styleUrl: './students.gestion.component.scss',
  providers: [MessageService],
  standalone: false,
})
export class StudentsGestionComponent implements OnInit {
  students: Student[] = [];
  classes: Classe[] = [];
  loading = false;
  actionLoading = false;

  currentFilter: 'all' | 'no-class' | 'class' = 'all';
  selectedClassFilter: number | null = null;
  filteredStudents: Student[] = [];
  displayCreateModal: boolean = false;
  displayBulkCreateModal: boolean = false;
  displayEditModal: boolean = false;
  displayAssignClassModal: boolean = false;

  studentForm: RegisterRequest = {
    email: '',
    nom: '',
    prenom: '',
    password: '',
  };

  selectedStudent: Student | null = null;
  selectedClass: number | null = null;
  selectedFile: File | null = null;
  createdUsers: BulkUserCreationResult[] = [];
  notCreatedUsers: BulkUserCreationResult[] = [];
  displayBulkResultsModal: boolean = false;
  activeResultTab: 'created' | 'notCreated' = 'created';

  constructor(
    private userApiService: UserApiService,
    private messageService: MessageService,
    private authApiService: AuthApiService,
    private confirmationService: ConfirmationService,
    private userService: UserApiService,
  ) {}
  selectedAuthId: number | null = null;
  ngOnInit(): void {
    this.loadStudents();
    this.loadClasses();
  }

  loadStudents(): void {
    this.loading = true;
    this.userApiService.getAllStudents().subscribe({
      next: (response) => {
        this.students = response.data ?? [];
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les étudiants',
        });
        this.loading = false;
      },
    });
  }
  setFilter(filter: 'all' | 'no-class' | 'class'): void {
    this.currentFilter = filter;
    this.applyFilter();
  }

  applyFilter(): void {
    console.log(this.selectedClassFilter);
    switch (this.currentFilter) {
      case 'all':
        this.filteredStudents = this.students;
        break;
      case 'no-class':
        this.filteredStudents = this.students.filter((s) => !s.classe);
        break;
      case 'class':
        if (this.selectedClassFilter) {
          this.filteredStudents = this.students.filter(
            (s) => s.classe?.classeId == this.selectedClassFilter,
          );
        } else {
          this.filteredStudents = this.students;
        }
        break;
      default:
        this.filteredStudents = this.students;
    }
  }
  clearFilters(): void {
    this.currentFilter = 'all';
    this.selectedClassFilter = null;
    this.applyFilter();
  }

  getStudentsWithoutClass(): Student[] {
    return this.students.filter((s) => !s.classe);
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.selectedStudents = [];
    } else {
      this.selectedStudents = this.filteredStudents.map((s) => Number(s.authId));
    }
  }

  isAllSelected(): boolean {
    return (
      this.filteredStudents.length > 0 &&
      this.selectedStudents.length === this.filteredStudents.length &&
      this.filteredStudents.every((s) =>
        this.selectedStudents.includes(Number(s.authId)),
      )
    );
  }

  loadClasses(): void {
    this.loading = true;
    this.userApiService.getAllClasses().subscribe({
      next: (response) => {
        this.classes = response.data ?? [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error  classes', error);
        this.loading = false;
      },
    });
  }

  openCreateModal(): void {
    this.studentForm = {
      email: '',
      nom: '',
      prenom: '',
      password: '',
    };
    this.displayCreateModal = true;
  }

  openBulkCreateModal(): void {
    this.selectedFile = null;
    this.displayBulkCreateModal = true;
  }

  openAssignClassModal(student: Student): void {
    console.log('Selected Student:', this.selectedStudent);
    console.log('Selected Class:', this.selectedClass);
    this.selectedStudent = student;
    this.selectedClass = student.classe?.classeId ?? null;
    this.displayAssignClassModal = true;
  }

  createStudent(): void {
    this.actionLoading = true;
    this.authApiService.register(this.studentForm).subscribe({
      next: (response) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Étudiant créé avec succès',
        });
        this.displayCreateModal = false;
        this.actionLoading = false;
        this.loadStudents();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Impossible de créer l'étudiant",
        });
        this.actionLoading = false;
      },
    });
  }

  assignToClass(): void {
    const authId = this.selectedStudent?.authId;
    const classId = this.selectedClass;

    console.log('Selected Student auth ID:', authId);
    console.log('Selected Class ID:', classId);

    if (authId && classId) {
      // Close modal first
      this.displayAssignClassModal = false;
      this.actionLoading = true;

      this.userApiService
        .assignStudentToClass({
          authId: Number(authId),
          classId: classId,
        })
        .subscribe({
          next: (response) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: 'Étudiant assigné à la classe avec succès',
            });
            this.actionLoading = false;
            this.loadStudents();
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Erreur',
              detail: "Impossible d'assigner l'étudiant à la classe",
            });
            this.actionLoading = false;
          },
        });
    }
  }
  onFileSelect(event: any): void {
    if (event.files && event.files.length > 0) {
      this.selectedFile = event.files[0];
    }
  }

  bulkCreateStudents(): void {
    if (this.selectedFile) {
      this.actionLoading = true;
      this.userApiService.bulkCreateStudents(this.selectedFile).subscribe({
        next: (response) => {
          const data = response.data;
          if (data && data.results) {
            this.createdUsers = data.results.cree ?? [];
            this.notCreatedUsers = data.results.non_cree ?? [];
            this.displayBulkCreateModal = false;
            this.displayBulkResultsModal = true;
            this.loadStudents();

            const totalCreated = this.createdUsers.length;
            const totalNotCreated = this.notCreatedUsers.length;

            if (totalCreated > 0) {
              this.messageService.add({
                severity: 'success',
                summary: 'Succès',
                detail: `${totalCreated} étudiant(s) créé(s) avec succès${totalNotCreated > 0 ? `, ${totalNotCreated} non créé(s)` : ''}`,
              });
            } else if (totalNotCreated > 0) {
              this.messageService.add({
                severity: 'warn',
                summary: 'Attention',
                detail: `Aucun étudiant créé. ${totalNotCreated} utilisateur(s) déjà existant(s) ou invalide(s)`,
              });
            }
          }
          this.actionLoading = false;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de créer les étudiants en masse',
          });
          this.actionLoading = false;
        },
      });
    }
  }
  // Add to your component class
  selectedStudents: number[] = [];
  displayBulkAssignModal: boolean = false;

  // Toggle single student selection
  toggleStudentSelection(authId: number): void {
    const index = this.selectedStudents.indexOf(authId);
    if (index > -1) {
      this.selectedStudents.splice(index, 1);
    } else {
      this.selectedStudents.push(authId);
    }
  }

  // Check if student is selected
  isStudentSelected(authId: number): boolean {
    return this.selectedStudents.includes(authId);
  }

  // Clear selection
  clearSelection(): void {
    this.selectedStudents = [];
  }

  // Select only students without a class
  selectStudentsWithoutClass(): void {
    this.selectedStudents = this.students
      .filter((s) => !s.classe)
      .map((s) => Number(s.authId));

    if (this.selectedStudents.length === 0) {
      this.messageService.add({
        severity: 'info',
        summary: 'Information',
        detail: 'Aucun étudiant sans classe trouvé',
      });
    }
  }

  // Open bulk assign modal
  openBulkAssignModal(): void {
    this.selectedClass = null;
    this.displayBulkAssignModal = true;
  }

  // Bulk assign students to class
  bulkAssignToClass(): void {
    const authIds = [...this.selectedStudents];
    const classId = this.selectedClass;

    if (authIds.length === 0 || !classId) {
      return;
    }

    // Close modal first
    this.displayBulkAssignModal = false;
    this.actionLoading = true;

    this.userApiService
      .bulkAssignStudentsToClass({
        authIds: authIds,
        classId: classId,
      })
      .subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: `${authIds.length} étudiant(s) assigné(s) à la classe avec succès`,
          });
          this.actionLoading = false;
          this.clearSelection();
          this.loadStudents();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Impossible d'assigner les étudiants à la classe",
          });
          this.actionLoading = false;
        },
      });
  }

  closeBulkResultsModal(): void {
    this.displayBulkResultsModal = false;
    this.createdUsers = [];
    this.notCreatedUsers = [];
    this.activeResultTab = 'created';
  }

  deleteTeacher(event: Event, id: string): void {
    event.stopPropagation();

    this.selectedAuthId = Number(id);
    console.log('Selected auth ID for deletion:', this.selectedAuthId);

    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Êtes-vous sûr de vouloir supprimer cet utilisateur ?',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Non, annuler',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        const authIdToDelete = this.selectedAuthId;
        console.log('Accept - Deleting auth ID:', authIdToDelete);

        if (authIdToDelete) {
          this.actionLoading = true;
          this.userService.deleteUser(authIdToDelete).subscribe({
            next: (response) => {
              this.messageService.add({
                severity: 'success',
                summary: 'Succès',
                detail: 'Utilisateur supprimé avec succès',
              });
              this.actionLoading = false;
              this.loadStudents();
              this.selectedAuthId = null;
              console.log('Delete successful for auth ID:', response);
            },
            error: (err) => {
              console.error('Delete error:', err);
              this.messageService.add({
                severity: 'error',
                summary: 'Erreur',
                detail: "Impossible de supprimer l'utilisateur.",
              });
              this.actionLoading = false;
              this.selectedAuthId = null;
            },
          });
        } else {
          console.error('No auth ID found for deletion');
        }
      },
      reject: () => {
        console.log('Delete rejected for auth ID:', this.selectedAuthId);
        this.selectedAuthId = null;
      },
    });
  }
}
