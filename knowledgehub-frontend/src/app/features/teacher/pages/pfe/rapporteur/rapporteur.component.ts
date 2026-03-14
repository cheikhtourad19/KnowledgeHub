import { Component } from '@angular/core';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { Router } from '@angular/router';
import { OnInit } from '@angular/core';

@Component({
  selector: 'app-rapporteur',
  standalone: false,
  templateUrl: './rapporteur.component.html',
  styleUrl: './rapporteur.component.scss',
})
export class RapporteurComponent implements OnInit {
  allPfes: PFE[] = [];

  constructor(
    private resourceApi: ResourceApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.resourceApi.getTeacherRepporteurPFE().subscribe({
      next: (res) => (this.allPfes = res.data ?? []),
      error: (err) => console.error(err),
    });
  }

  viewDetails(pfe: PFE): void {
    this.router.navigate(['/teacher/pfe/rapporteur', pfe.id]);
  }
}
