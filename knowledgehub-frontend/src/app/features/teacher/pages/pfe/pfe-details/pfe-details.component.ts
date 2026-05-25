import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-pfe-details',
  standalone: false,
  templateUrl: './pfe-details.component.html',
})
export class PfeDetailsComponent implements OnInit {
  pfe: PFE | null = null;
  rapporteurFeedback = '';
  savingFeedback = false;
  loading = false;

  constructor(
    private resourceApi: ResourceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    if (id) {
      this.loadPfeById(id);
    }
  }

  loadPfeById(id: number): void {
    this.loading = true;
    this.resourceApi.getPFEById(id).subscribe({
      next: (res) => {
        this.loading = false;
        this.pfe = res.data ?? null;
        this.rapporteurFeedback = this.pfe?.reporteurFeedback ?? '';
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger le PFE.',
        });
      },
    });
  }

  isAssignedRapporteur(): boolean {
    const currentAuthId = this.authService.getAuthId();
    return !!currentAuthId && this.pfe?.rapporteurAuthId === currentAuthId;
  }

  submitRapporteurFeedback(): void {
    if (!this.pfe?.id || !this.isAssignedRapporteur()) {
      return;
    }

    const feedback = this.rapporteurFeedback.trim();
    if (!feedback) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Le feedback ne peut pas être vide.',
      });
      return;
    }

    this.savingFeedback = true;
    this.resourceApi
      .submitPFERapporteurFeedback(this.pfe.id, feedback)
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Feedback rapporteur enregistré.',
          });
          this.savingFeedback = false;
          this.loadPfeById(this.pfe!.id);
        },
        error: (err) => {
          this.savingFeedback = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail:
              err?.error?.message || "Impossible d'enregistrer le feedback.",
          });
        },
      });
  }

  goBack(): void {
    this.router.navigate(['../'], { relativeTo: this.route });
  }
}
