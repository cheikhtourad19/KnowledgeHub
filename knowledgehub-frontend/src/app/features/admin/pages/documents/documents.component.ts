import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ResourceApiService } from '../../../../shared/services/resource-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { File } from '../../../../shared/models/resource-service/entities/file.model';
import { FileType } from '../../../../shared/models/resource-service/enums/file-type.enum';

@Component({
  selector: 'app-documents',
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.scss',
  standalone: false,
})
export class DocumentsComponent implements OnInit {
  documents: File[] = [];
  filteredDocuments: File[] = [];
  coverUrls: Record<number, string> = {};
  publicationLoading: Record<number, boolean> = {};
  loading = false;

  // Upload Modal
  showUploadModal = false;
  uploadedFile: globalThis.File | null = null;
  coverFile: globalThis.File | null = null;
  uploadTitle = '';
  uploadDescription = '';
  isPublishedOnUpload: boolean = false;
  selectedFileType: FileType = FileType.EDUCATIONAL_MATERIAL;
  uploading = false;

  // Delete Confirmation Modal
  showDeleteModal = false;
  pendingDeleteDocumentId: number | null = null;
  deleting = false;

  // Password Confirmation Modal
  showPasswordModal = false;
  passwordConfirmation = '';
  pendingPublicationDocument: File | null = null;
  verifyingPassword = false;

  // File type options - Admin only uses EDUCATIONAL_MATERIAL
  fileTypeOptions = [
    { label: 'Matériel Éducatif', value: FileType.EDUCATIONAL_MATERIAL },
  ];

  // Search and filter
  searchQuery = '';
  selectedFilterType: FileType | 'ALL' = 'ALL';

  get allFileTypeOptions() {
    return [{ label: 'Tous les types', value: 'ALL' }, ...this.fileTypeOptions];
  }

  constructor(
    private resourceApiService: ResourceApiService,
    private authService: AuthService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.resourceApiService.getAllDocuments().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.documents = response.data;
          this.preloadCovers(this.documents);
          this.applyFilters();
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: `${this.documents.length} document(s) chargé(s)`,
            life: 3000,
          });
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les documents',
          life: 5000,
        });
        this.loading = false;
      },
    });
  }

  applyFilters(): void {
    let filtered = [...this.documents];

    // Filter by type
    if (this.selectedFilterType !== 'ALL') {
      filtered = filtered.filter(
        (doc) => doc.fileType === this.selectedFilterType,
      );
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (doc) =>
          doc.title.toLowerCase().includes(query) ||
          doc.description?.toLowerCase().includes(query) ||
          doc.originalFileName.toLowerCase().includes(query),
      );
    }

    this.filteredDocuments = filtered;
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onFilterTypeChange(): void {
    this.applyFilters();
  }

  openUploadModal(): void {
    this.showUploadModal = true;
    this.uploadedFile = null;
    this.uploadTitle = '';
    this.uploadDescription = '';
    this.selectedFileType = FileType.EDUCATIONAL_MATERIAL;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.uploadedFile = null;
    this.uploadTitle = '';
    this.uploadDescription = '';
  }

  onFileSelect(event: any): void {
    if (event.files && event.files.length > 0) {
      this.uploadedFile = event.files[0];
    }
  }

  onCoverSelect(event: any): void {
    if (event.files && event.files.length > 0) {
      this.coverFile = event.files[0];
    }
  }

  uploadDocument(): void {
    if (!this.uploadedFile || !this.uploadTitle.trim()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez renseigner le fichier et le titre',
        life: 3000,
      });
      return;
    }

    this.uploading = true;
    this.resourceApiService
      .uploadFile(
        this.uploadedFile,
        this.selectedFileType,
        'documents',
        this.uploadTitle,
        this.uploadDescription,
        this.coverFile || undefined,
        this.isPublishedOnUpload,
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: 'Document téléchargé avec succès',
              life: 3000,
            });
            this.closeUploadModal();
            this.loadDocuments();
          }
          this.uploading = false;
        },
        error: (error) => {
          console.error('Error uploading document:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de télécharger le document',
            life: 5000,
          });
          this.uploading = false;
        },
      });
  }

  togglePublicationStatus(document: File): void {
    // Si on dépublie, pas besoin de confirmation
    if (document.isPublished) {
      this.unpublishDocument(document);
    } else {
      // Si on publie, demander confirmation par mot de passe
      this.openPasswordConfirmationModal(document);
    }
  }

  onPublicationSwitchChange(document: File, nextValue: boolean): void {
    if (this.publicationLoading[document.id]) {
      return;
    }

    // Keep previous behavior: publishing requires password confirmation.
    if (nextValue) {
      this.openPasswordConfirmationModal(document);
      return;
    }

    this.togglePublicationStatus(document);
  }

  openPasswordConfirmationModal(document: File): void {
    this.pendingPublicationDocument = document;
    this.passwordConfirmation = '';
    this.showPasswordModal = true;
  }

  closePasswordModal(revertSwitch: boolean = true): void {
    if (revertSwitch) {
      this.revertPendingPublicationSwitch();
    }
    this.showPasswordModal = false;
    this.passwordConfirmation = '';
    this.pendingPublicationDocument = null;
    this.verifyingPassword = false;
  }

  private revertPendingPublicationSwitch(): void {
    if (this.pendingPublicationDocument) {
      this.pendingPublicationDocument.isPublished = false;
    }
  }

  confirmPasswordAndPublish(): void {
    if (!this.passwordConfirmation || !this.pendingPublicationDocument) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Attention',
        detail: 'Veuillez entrer votre mot de passe',
        life: 3000,
      });
      this.revertPendingPublicationSwitch();
      return;
    }

    this.verifyingPassword = true;

    // Vérifier le mot de passe
    this.authService.verifyPassword(this.passwordConfirmation).subscribe({
      next: (response) => {
        if (response.success) {
          // Mot de passe correct, procéder à la publication
          this.publishDocument(this.pendingPublicationDocument!);
          this.closePasswordModal(false);
        } else {
          this.revertPendingPublicationSwitch();
          this.messageService.add({
            severity: 'error',
            summary: 'Mot de passe incorrect',
            detail: 'Le mot de passe que vous avez entré est incorrect',
            life: 5000,
          });
        }
        this.verifyingPassword = false;
      },
      error: (error) => {
        console.error('Error verifying password:', error);
        this.revertPendingPublicationSwitch();
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de vérifier le mot de passe',
          life: 5000,
        });
        this.verifyingPassword = false;
      },
    });
  }

  publishDocument(document: File): void {
    this.publicationLoading[document.id] = true;
    this.resourceApiService
      .updatePublicationStatus(document.id, true)
      .subscribe({
        next: (response) => {
          if (response.success) {
            document.isPublished = true;
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: 'Document publié avec succès',
              life: 3000,
            });
          }
          this.publicationLoading[document.id] = false;
        },
        error: (error) => {
          console.error('Error publishing document:', error);
          document.isPublished = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de publier le document',
            life: 5000,
          });
          this.publicationLoading[document.id] = false;
        },
      });
  }

  unpublishDocument(document: File): void {
    this.publicationLoading[document.id] = true;
    this.resourceApiService
      .updatePublicationStatus(document.id, false)
      .subscribe({
        next: (response) => {
          if (response.success) {
            document.isPublished = false;
            this.messageService.add({
              severity: 'success',
              summary: 'Succès',
              detail: 'Document dépublié avec succès',
              life: 3000,
            });
          }
          this.publicationLoading[document.id] = false;
        },
        error: (error) => {
          console.error('Error unpublishing document:', error);
          document.isPublished = true;
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de dépublier le document',
            life: 5000,
          });
          this.publicationLoading[document.id] = false;
        },
      });
  }

  private preloadCovers(files: File[]): void {
    files
      .filter((file) => file.coverImagePath && !this.coverUrls[file.id])
      .forEach((file) => {
        this.resourceApiService.getCoverPresignedUrl(file.id).subscribe({
          next: (response) => {
            if (response.success && response.data) {
              this.coverUrls[file.id] = response.data;
            }
          },
          error: () => {
            // Keep placeholder when cover fetch fails.
          },
        });
      });
  }

  openDeleteModal(documentId: number): void {
    this.pendingDeleteDocumentId = documentId;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.pendingDeleteDocumentId = null;
    this.deleting = false;
  }

  confirmDelete(): void {
    if (!this.pendingDeleteDocumentId) return;

    this.deleting = true;
    this.resourceApiService.deleteFile(this.pendingDeleteDocumentId).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Document supprimé avec succès',
            life: 3000,
          });
          this.closeDeleteModal();
          this.loadDocuments();
        }
        this.deleting = false;
      },
      error: (error) => {
        console.error('Error deleting document:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de supprimer le document',
          life: 5000,
        });
        this.deleting = false;
      },
    });
  }

  viewDocument(documentId: number): void {
    this.resourceApiService.viewpdf(documentId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          window.open(response.data, '_blank');
        }
      },
      error: (error) => {
        console.error('Error viewing document:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: "Impossible d'afficher le document",
          life: 5000,
        });
      },
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
    return labels[fileType] || fileType;
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
      classes[fileType] ||
      'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400'
    );
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
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
