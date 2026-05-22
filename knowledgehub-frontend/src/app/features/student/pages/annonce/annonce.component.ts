import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { AnnonceStage } from '../../../../shared/models/resource-service/entities/annonce.model';
import {
  ModaliteTravail,
  ModaliteTravailIcons,
  ModaliteTravailLabels,
} from '../../../../shared/models/resource-service/enums/modalite-travail.enum';
import {
  TypeAnnonceStage,
  TypeAnnonceStageLabels,
} from '../../../../shared/models/resource-service/enums/type-annonce-stage.enum';

@Component({
  selector: 'app-student-annonce',
  templateUrl: './annonce.component.html',
  styleUrls: ['./annonce.component.scss'],
  standalone: false,
})
export class StudentAnnonceComponent implements OnInit {
  annonces: AnnonceStage[] = [];
  filteredAnnonces: AnnonceStage[] = [];
  loading = false;

  searchKeyword = '';
  selectedType: TypeAnnonceStage | '' = '';
  selectedModalite: ModaliteTravail | '' = '';
  selectedVille = '';

  typeOptions = Object.values(TypeAnnonceStage);
  TypeAnnonceStageLabels = TypeAnnonceStageLabels;
  modaliteOptions = Object.values(ModaliteTravail);
  ModaliteTravailLabels = ModaliteTravailLabels;
  ModaliteTravailIcons = ModaliteTravailIcons;

  constructor(
    private resourceApi: ResourceApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.resourceApi.findAllActiveAnnonces().subscribe({
      next: (response) => {
        this.annonces = response.data ?? [];
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.annonces = [];
        this.filteredAnnonces = [];
        this.loading = false;
      },
    });
  }

  applyFilters(): void {
    const keyword = this.searchKeyword.toLowerCase().trim();
    const city = this.selectedVille.toLowerCase().trim();

    this.filteredAnnonces = this.annonces.filter((annonce) => {
      const matchesKeyword =
        !keyword ||
        [
          annonce.titre,
          annonce.nomEntreprise,
          annonce.ville,
          annonce.pays,
          annonce.description,
        ]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(keyword));

      const matchesType =
        !this.selectedType || annonce.typesStage.includes(this.selectedType);
      const matchesModalite =
        !this.selectedModalite ||
        annonce.modaliteTravail === this.selectedModalite;
      const matchesVille = !city || annonce.ville.toLowerCase().includes(city);

      return matchesKeyword && matchesType && matchesModalite && matchesVille;
    });
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedType = '';
    this.selectedModalite = '';
    this.selectedVille = '';
    this.applyFilters();
  }

  goToDetails(id: number): void {
    this.router.navigate(['/student/annonces', id]);
  }

  trackById(_: number, annonce: AnnonceStage): number | undefined {
    return annonce.id;
  }
}
