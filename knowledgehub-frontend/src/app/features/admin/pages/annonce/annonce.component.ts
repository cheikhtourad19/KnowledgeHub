import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { AnnonceStage, AnnonceStageRequest } from '../../../../shared/models/resource-service/entities/annonce.model';
import { TypeAnnonceStage, TypeAnnonceStageLabels } from '../../../../shared/models/resource-service/enums/type-annonce-stage.enum';
import { ModaliteTravail, ModaliteTravailLabels, ModaliteTravailIcons } from '../../../../shared/models/resource-service/enums/modalite-travail.enum';
import { TypeUrl } from '../../../../shared/models/resource-service/enums/type-url.enum';

@Component({
  selector: 'app-annonce',
  templateUrl: './annonce.component.html',
  styleUrls: ['./annonce.component.scss'],
  standalone: false,
})
export class AnnonceComponent implements OnInit {

  annonces: AnnonceStage[] = [];
  filtered: AnnonceStage[] = [];
  loading = false;
  searchKeyword = '';

  // modal
  showModal = false;
  isEditing = false;
  submitting = false;
  selectedImage: File | null = null;
  imagePreview: string | null = null;

  // delete
  showDeleteModal = false;
  deletingId: number | null = null;
  deleting = false;

  // form
  form: AnnonceStageRequest = this.emptyForm();
  editingId: number | null = null;

  // enums pour le template
  typeOptions = Object.values(TypeAnnonceStage);
  TypeAnnonceStageLabels = TypeAnnonceStageLabels;
  modaliteOptions = Object.values(ModaliteTravail);
  ModaliteTravailLabels = ModaliteTravailLabels;
  ModaliteTravailIcons = ModaliteTravailIcons;
  TypeUrl = TypeUrl;
  typeUrlOptions = Object.values(TypeUrl);

  constructor(
    private annonceService: ResourceApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.annonceService.findAllAnnonces().subscribe({
      next: res => {
        this.annonces = res.data ?? [];
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applyFilter(): void {
    const kw = this.searchKeyword.toLowerCase().trim();
    this.filtered = kw
      ? this.annonces.filter(a =>
          a.titre.toLowerCase().includes(kw) ||
          a.nomEntreprise.toLowerCase().includes(kw) ||
          a.ville.toLowerCase().includes(kw),
        )
      : [...this.annonces];
  }

  openCreate(): void {
    this.isEditing = false;
    this.editingId = null;
    this.form = this.emptyForm();
    this.selectedImage = null;
    this.imagePreview = null;
    this.showModal = true;
  }

  openEdit(a: AnnonceStage): void {
    this.isEditing = true;
    this.editingId = a.id!;
    this.form = {
      titre: a.titre,
      description: a.description,
      nomEntreprise: a.nomEntreprise,
      ville: a.ville,
      pays: a.pays,
      typesStage: [...a.typesStage],
      modaliteTravail: a.modaliteTravail,
      urls: a.urls.map(u => ({ ...u })),
    };
    this.selectedImage = null;
    this.imagePreview = a.imageUrl ?? null;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.selectedImage = file;
    const reader = new FileReader();
    reader.onload = e => { this.imagePreview = e.target?.result as string; };
    reader.readAsDataURL(file);
  }

  removeImage(): void {
    this.selectedImage = null;
    this.imagePreview = null;
  }

  toggleType(type: TypeAnnonceStage): void {
    const idx = this.form.typesStage.indexOf(type);
    if (idx === -1) this.form.typesStage.push(type);
    else this.form.typesStage.splice(idx, 1);
  }

  isTypeSelected(type: TypeAnnonceStage): boolean {
    return this.form.typesStage.includes(type);
  }

  addUrl(): void {
    this.form.urls.push({ url: '', typeUrl: TypeUrl.AUTRE });
  }

  removeUrl(index: number): void {
    this.form.urls.splice(index, 1);
  }

  submit(): void {
    if (this.submitting) return;
    this.submitting = true;

    const obs = this.isEditing
      ? this.annonceService.editAnnonce(this.editingId!, this.form, this.selectedImage ?? undefined)
      : this.annonceService.saveAnnonce(this.form, this.selectedImage ?? undefined);

    obs.subscribe({
      next: saved => {
        const annonce = saved.data!;
        if (this.isEditing) {
          const idx = this.annonces.findIndex(a => a.id === annonce.id);
          if (idx !== -1) this.annonces[idx] = annonce;
        } else {
          this.annonces.unshift(annonce);
        }
        this.applyFilter();
        this.submitting = false;
        this.showModal = false;
      },
      error: () => { this.submitting = false; },
    });
  }

  confirmDelete(id: number): void {
    this.deletingId = id;
    this.showDeleteModal = true;
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.deletingId = null;
  }

  executeDelete(): void {
    if (!this.deletingId) return;
    this.deleting = true;
    this.annonceService.deleteAnnonce(this.deletingId).subscribe({
      next: () => {
        this.annonces = this.annonces.filter(a => a.id !== this.deletingId);
        this.applyFilter();
        this.deleting = false;
        this.showDeleteModal = false;
        this.deletingId = null;
      },
      error: () => { this.deleting = false; },
    });
  }

  toggle(a: AnnonceStage): void {
    this.annonceService.toggleAnnonceActive(a.id!).subscribe({
      next: updated => {
        const annonce = updated.data!;
        const idx = this.annonces.findIndex(x => x.id === annonce.id);
        if (idx !== -1) this.annonces[idx] = annonce;
        this.applyFilter();
      },
    });
  }

  goToDetails(id: number): void {
    this.router.navigate(['admin/annonce', id]);
  }

  private emptyForm(): AnnonceStageRequest {
    return {
      titre: '',
      description: '',
      nomEntreprise: '',
      ville: '',
      pays: '',
      typesStage: [],
      modaliteTravail: ModaliteTravail.PRESENTIEL,
      urls: [],
    };
  }
}