import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { AnnonceStage } from '../../../../../shared/models/resource-service/entities/annonce.model';
import { TypeAnnonceStageLabels } from '../../../../../shared/models/resource-service/enums/type-annonce-stage.enum';
import {
  ModaliteTravailLabels,
  ModaliteTravailIcons,
} from '../../../../../shared/models/resource-service/enums/modalite-travail.enum';
import {
  TypeUrlLabels,
  TypeUrlIcons,
} from '../../../../../shared/models/resource-service/enums/type-url.enum';
@Component({
  selector: 'app-annonce-details',
  templateUrl: './annonce-details.component.html',
  styleUrls: ['./annonce-details.component.scss'],
  standalone: false,
})
export class AnnonceDetailsComponent implements OnInit {
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
    private annonceService: ResourceApiService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.notFound = true;
      return;
    }
    this.loading = true;
    this.annonceService.findAnnonceById(id).subscribe({
      next: (res) => {
        this.annonce = res.data ?? null;
        this.loading = false;
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['admin/annonce']);
  }

  toggle(): void {
    if (!this.annonce) return;
    this.annonceService.toggleAnnonceActive(this.annonce.id!).subscribe({
      next: (updated) => {
        this.annonce = updated.data ?? this.annonce;
      },
    });
  }

  openUrl(url: string): void {
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'https://' + url;
    }

    window.open(url, '_blank');
  }
}
