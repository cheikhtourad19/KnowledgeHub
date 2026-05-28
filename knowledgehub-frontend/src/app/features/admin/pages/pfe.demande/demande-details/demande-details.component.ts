import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { DemandePFE } from '../../../../../shared/models/resource-service/entities/demande-pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { DemandStatus } from '../../../../../shared/models/resource-service/enums/demand-status.enum';
import { UpdateDemandStatusRequest } from '../../../../../shared/models/resource-service/requests/update-demand-status-request.model';
import { Teacher } from '../../../../../shared/models/user-service/entities/teacher.model';
import { UserApiService } from '../../../../../shared/services/user-api.service';

interface TeacherWithMatch {
  authId: string;
  teacherId?: number;
  fullName: string;
  email: string;
  pfeCount: number;
  matchScore?: number;
}

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
  loading = false;
  actionLoading = false;
  pdf_presigned_url: string = '';
  displayApproveDialog: boolean = false;
  displayRejectDialog: boolean = false;
  adminFeedback: string = '';
  showPdfModal: boolean = false;
  displayDeleteDialog: boolean = false;

  teachersWithMatch: TeacherWithMatch[] = [];
  selectedTeacherAuthId: string | null = null;
  isAiSearching = false;
  aiSearchDone = false;

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
    this.fetchTeachersWithCount();
  }

  fetchTeachersWithCount() {
    this.loading = true;
    this.userService.getAllTeachers().subscribe({
      next: (teachersRes) => {
        this.resourceApiService.getTeachersPfeCount().subscribe({
          next: (countRes) => {
            this.loading = false;
            const countMap: Record<string, number> = countRes.data ?? {};
            this.teachersWithMatch = (teachersRes.data ?? []).map((t: Teacher) => ({
              authId: t.authId,
              teacherId: t.teacherId,
              fullName: `${t.prenom ?? ''} ${t.nom ?? ''}`.trim() || t.email || 'Sans nom',
              email: t.email,
              pfeCount: countMap[t.authId] ?? 0,
            }));
          },
          error: () => {
            this.loading = false;
            this.teachersWithMatch = (teachersRes.data ?? []).map((t: Teacher) => ({
              authId: t.authId,
              teacherId: t.teacherId,
              fullName: `${t.prenom ?? ''} ${t.nom ?? ''}`.trim() || t.email || 'Sans nom',
              email: t.email,
              pfeCount: 0,
            }));
          },
        });
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les enseignants.',
        });
      },
    });
  }

  searchMatchingTeachers() {
    if (!this.demande) return;

    this.isAiSearching = true;
    this.aiSearchDone = false;
    this.teachersWithMatch = this.teachersWithMatch.map((t) => ({ ...t, matchScore: undefined }));

    const pfePayload = [{
      pfe_title: this.demande.pfeTitle,
      pfe_description: this.demande.pfeDescription,
      mot_cles: this.demande.motCles,
      entreprise_domaine: this.demande.entrepriseDomaine,
      entreprise_name: this.demande.entrepriseName,
    }];

    this.resourceApiService.searchTeachersByCv(pfePayload).subscribe({
      next: (response) => {
        this.isAiSearching = false;
        this.aiSearchDone = true;

        const teacherIdToAuthId = new Map<number, string>();
        for (const t of this.teachersWithMatch) {
          if (t.teacherId != null) {
            teacherIdToAuthId.set(t.teacherId, t.authId);
          }
        }

        const scoreByAuthId = new Map<string, number>();
        for (const m of response.matches) {
          const authId = teacherIdToAuthId.get(m.teacher_id);
          if (authId) {
            scoreByAuthId.set(authId, m.score);
          }
        }

        this.teachersWithMatch = this.teachersWithMatch.map((t) => ({
          ...t,
          matchScore: scoreByAuthId.get(t.authId),
        }));

        this.teachersWithMatch.sort((a, b) => {
          const scoreA = a.matchScore ?? 0;
          const scoreB = b.matchScore ?? 0;
          return scoreB - scoreA;
        });

        this.messageService.add({
          severity: 'success',
          summary: 'Suggestion IA',
          detail: `${response.matches.length} enseignant(s) trouvé(s) par similarité de CV`,
        });
      },
      error: () => {
        this.isAiSearching = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur IA',
          detail: 'Échec de la recherche par CV embeddings.',
        });
      },
    });
  }

  selectTeacher(authId: string): void {
    this.selectedTeacherAuthId = authId;
  }

  get selectedTeacher(): TeacherWithMatch | undefined {
    return this.teachersWithMatch.find((t) => t.authId === this.selectedTeacherAuthId);
  }

  get sortedTeachers(): TeacherWithMatch[] {
    if (this.aiSearchDone) {
      return this.teachersWithMatch;
    }
    return [...this.teachersWithMatch].sort((a, b) => a.fullName.localeCompare(b.fullName));
  }

  getScoreColor(score: number | undefined): string {
    if (score == null) return '';
    if (score >= 0.7) return 'green';
    if (score >= 0.5) return 'yellow';
    if (score >= 0.3) return 'orange';
    return 'red';
  }

  showDeleteDialog(): void {
    this.displayDeleteDialog = true;
  }

  hideDeleteDialog(): void {
    this.displayDeleteDialog = false;
  }

  deleteDemande(): void {
    if (!this.demandeId) return;

    this.actionLoading = true;
    this.resourceApiService.deleteDemande(this.demandeId).subscribe({
      next: () => {
        this.actionLoading = false;
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
        this.actionLoading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Erreur lors de la suppression de la demande',
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

    this.loading = true;
    this.resourceApiService.getDemandePFEById(this.demandeId).subscribe({
      next: (response) => {
        this.loading = false;
        this.demande = response.data || null;
        this.loadDemandePdf();
      },
      error: (error) => {
        this.loading = false;
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

    this.actionLoading = true;
    const request: UpdateDemandStatusRequest = {
      status: DemandStatus.ACCEPTED,
      adminFeedback: this.adminFeedback || undefined,
    };

    this.resourceApiService
      .updateDemandStatus(
        this.demandeId,
        request,
        this.selectedTeacherAuthId ? Number(this.selectedTeacherAuthId) : undefined,
      )
      .subscribe({
        next: (response) => {
          this.actionLoading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande approuvée et PFE créé avec succès',
          });
          this.hideApproveDialog();
          this.router.navigate(['/admin/pfe/demandes']);
        },
        error: (error) => {
          this.actionLoading = false;
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

    this.actionLoading = true;
    const request: UpdateDemandStatusRequest = {
      status: DemandStatus.REJECTED,
      adminFeedback: this.adminFeedback,
    };

    this.resourceApiService
      .updateDemandStatus(this.demandeId, request)
      .subscribe({
        next: (response) => {
          this.actionLoading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande rejetée avec succès',
          });
          this.hideRejectDialog();
          this.router.navigate(['/admin/pfe/demandes']);
        },
        error: (error) => {
          this.actionLoading = false;
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
