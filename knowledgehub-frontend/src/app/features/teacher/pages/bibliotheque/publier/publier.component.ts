import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { File } from '../../../../../shared/models/resource-service/entities/file.model';
import { FileType } from '../../../../../shared/models/resource-service/enums/file-type.enum';

@Component({
  selector: 'app-publier',
  standalone: false,
  templateUrl: './publier.component.html',
  styleUrl: './publier.component.scss',
})
export class PublierComponent implements OnInit {
  documents: File[] = [];
  filteredDocuments: File[] = [];

  searchQuery = '';
  selectedFilterType: FileType | 'ALL' = 'ALL';

  showUploadModal = false;
  uploadedFile: globalThis.File | null = null;
  coverFile: globalThis.File | null = null;
  uploadTitle = '';
  uploadDescription = '';
  uploading = false;
  loading = false;
  deleting = false;

  showDeleteModal = false;
  pendingDeleteDocument: File | null = null;

  fileTypeOptions = [
    { label: 'Tous les types', value: 'ALL' as const },
    { label: 'Matériel Éducatif', value: FileType.EDUCATIONAL_MATERIAL },
    { label: 'Rapport PFA', value: FileType.PFA_REPORT },
    { label: 'Rapport PFE', value: FileType.PFE_REPORT },
    { label: 'Rapport Stage', value: FileType.STAGE_REPORT },
    { label: 'Journal', value: FileType.JOURNAL },
    { label: 'Attestation', value: FileType.ATTESTATION },
    { label: 'Supplémentaire', value: FileType.SUPPLEMENTARY },
  ];

  constructor(
    private resourceApi: ResourceApiService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.resourceApi.getMyDocuments().subscribe({
      next: (res) => {
        this.loading = false;
        this.documents = res.data ?? [];
        this.applyFilters();
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger vos documents.',
        });
      },
    });
  }

  applyFilters(): void {
    let result = [...this.documents];
    if (this.selectedFilterType !== 'ALL') {
      result = result.filter((d) => d.fileType === this.selectedFilterType);
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(
        (d) =>
          d.title.toLowerCase().includes(q) ||
          d.description?.toLowerCase().includes(q) ||
          d.originalFileName.toLowerCase().includes(q),
      );
    }
    this.filteredDocuments = result;
  }

  onSearchChange(): void {
    this.applyFilters();
  }
  onFilterTypeChange(): void {
    this.applyFilters();
  }

  openUploadModal(): void {
    this.uploadedFile = null;
    this.uploadTitle = '';
    this.uploadDescription = '';
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
  }

  onFileSelect(event: any): void {
    if (event.files?.length) this.uploadedFile = event.files[0];
  }

  onCoverSelect(event: any): void {
    if (event.files?.length) this.coverFile = event.files[0];
  }

  uploadDocument(): void {
    if (!this.uploadedFile || !this.uploadTitle.trim()) return;
    this.uploading = true;

    this.resourceApi
      .uploadFile(
        this.uploadedFile!,
        FileType.EDUCATIONAL_MATERIAL,
        'documents',
        this.uploadTitle,
        this.uploadDescription || undefined,
        this.coverFile || undefined,
        undefined,
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Document téléchargé avec succès.',
          });
          this.uploading = false;
          this.closeUploadModal();
          this.loadDocuments();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Échec du téléchargement.',
          });
          this.uploading = false;
        },
      });
  }

  confirmDelete(doc: File): void {
    this.pendingDeleteDocument = doc;
    this.showDeleteModal = true;
  }

  cancelDelete(): void {
    this.pendingDeleteDocument = null;
    this.showDeleteModal = false;
  }

  executeDelete(): void {
    if (!this.pendingDeleteDocument) return;
    const id = this.pendingDeleteDocument.id;
    this.showDeleteModal = false;
    this.deleting = true;

    this.resourceApi.deleteFile(id).subscribe({
      next: () => {
        this.deleting = false;
        this.documents = this.documents.filter((d) => d.id !== id);
        this.applyFilters();
        this.messageService.add({
          severity: 'success',
          summary: 'Supprimé',
          detail: 'Document supprimé avec succès.',
        });
        this.pendingDeleteDocument = null;
      },
      error: () => {
        this.deleting = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de supprimer le document.',
        });
      },
    });
  }

  viewDocument(id: number): void {
    this.resourceApi.viewpdf(id).subscribe({
      next: (res) => {
        if (res.data) window.open(res.data, '_blank');
      },
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Impossible d'afficher le document.",
        }),
    });
  }

  getFileTypeLabel(fileType: FileType): string {
    const labels: Partial<Record<FileType, string>> = {
      [FileType.EDUCATIONAL_MATERIAL]: 'Matériel Éducatif',
      [FileType.PFA_REPORT]: 'Rapport PFA',
      [FileType.PFE_REPORT]: 'Rapport PFE',
      [FileType.STAGE_REPORT]: 'Rapport Stage',
      [FileType.JOURNAL]: 'Journal',
      [FileType.ATTESTATION]: 'Attestation',
      [FileType.DEMANDE]: 'Demande',
      [FileType.SUPPLEMENTARY]: 'Supplémentaire',
    };
    return labels[fileType] ?? fileType;
  }

  getFileTypeClass(fileType: FileType): string {
    const classes: Partial<Record<FileType, string>> = {
      [FileType.EDUCATIONAL_MATERIAL]:
        'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
      [FileType.PFA_REPORT]:
        'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
      [FileType.PFE_REPORT]:
        'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-400',
      [FileType.STAGE_REPORT]:
        'bg-cyan-100 text-cyan-700 dark:bg-cyan-900/30 dark:text-cyan-400',
      [FileType.JOURNAL]:
        'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
      [FileType.ATTESTATION]:
        'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400',
      [FileType.DEMANDE]:
        'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
      [FileType.SUPPLEMENTARY]:
        'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400',
    };
    return (
      classes[fileType] ??
      'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400'
    );
  }

  formatFileSize(bytes: number): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }
}
