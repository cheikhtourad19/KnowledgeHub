import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { AnnonceStage } from '../../../../shared/models/resource-service/entities/annonce.model';
import {
  ModaliteTravailIcons,
  ModaliteTravailLabels,
} from '../../../../shared/models/resource-service/enums/modalite-travail.enum';
import { TypeAnnonceStageLabels } from '../../../../shared/models/resource-service/enums/type-annonce-stage.enum';
import {
  TypeUrlIcons,
  TypeUrlLabels,
} from '../../../../shared/models/resource-service/enums/type-url.enum';

@Component({
  selector: 'app-student-annonce-details',
  templateUrl: './annonce-details.component.html',
  styleUrls: ['./annonce-details.component.scss'],
  standalone: false,
})
export class StudentAnnonceDetailsComponent implements OnInit {
  annonce: AnnonceStage | null = null;
  loading = false;
  notFound = false;

  TypeAnnonceStageLabels = TypeAnnonceStageLabels;
  ModaliteTravailLabels = ModaliteTravailLabels;
  ModaliteTravailIcons = ModaliteTravailIcons;
  TypeUrlLabels = TypeUrlLabels;
  TypeUrlIcons = TypeUrlIcons;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceApi: ResourceApiService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.notFound = true;
      return;
    }

    this.loading = true;
    this.resourceApi.findAnnonceById(id).subscribe({
      next: (response) => {
        const annonce = response.data ?? null;
        if (!annonce || !annonce.estActive) {
          this.notFound = true;
          this.annonce = null;
        } else {
          this.annonce = annonce;
        }
        this.loading = false;
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/student/annonces']);
  }

  openUrl(url: string): void {
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'https://' + url;
    }

    window.open(url, '_blank');
  }
}
