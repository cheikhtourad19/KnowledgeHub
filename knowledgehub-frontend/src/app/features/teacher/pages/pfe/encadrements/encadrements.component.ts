import { Component } from '@angular/core';
import { PFE } from '../../../../../shared/models/resource-service/entities/pfe.model';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { Router } from '@angular/router';
import { OnInit } from '@angular/core';
@Component({
  selector: 'app-encadrements',
  standalone: false,
  templateUrl: './encadrements.component.html',
})
export class EncadrementsComponent implements OnInit {
  allPfes: PFE[] = [];
  loading = false;

  constructor(
    private resourceApi: ResourceApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.resourceApi.getTeacherSupervisedPFE().subscribe({
      next: (res) => {
        this.allPfes = res.data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      },
    });
  }

  viewDetails(pfe: PFE): void {
    this.router.navigate(['/teacher/pfe/encadrements', pfe.id]);
  }
}
