import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-pfe-details',
  standalone: false,
  templateUrl: './pfe-details.component.html',
})
export class PfeDetailsComponent implements OnInit {
  pfe: PFE | null = null;

  constructor(
    private resourceApi: ResourceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    if (id) {
      this.resourceApi.getPFEById(id).subscribe({
        next: (res) => (this.pfe = res.data ?? null),
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de charger le PFE.',
          }),
      });
    }
  }

  goBack(): void {
    this.router.navigate(['../'], { relativeTo: this.route });
  }
}
