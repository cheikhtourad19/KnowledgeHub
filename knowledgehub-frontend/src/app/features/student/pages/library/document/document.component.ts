import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../../../shared/services/resource-api.service';
import { File } from '../../../../../shared/models/resource-service/entities/file.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-document',
  standalone: false,
  templateUrl: './document.component.html',
  styleUrl: './document.component.scss',
})
export class DocumentComponent implements OnInit {
  document: File | null = null;
  documentId: number = 0;
  pdfUrl: string = '';
  showPdfModal = false;

  typeLabels: Record<string, string> = {
    PFA_REPORT: 'Rapport PFA',
    PFE_REPORT: 'Rapport PFE',
    STAGE_REPORT: 'Rapport de Stage',
    JOURNAL: 'Journal',
    ATTESTATION: 'Attestation',
    DEMANDE: 'Demande',
    EDUCATIONAL_MATERIAL: 'Matériel Pédagogique',
    SUPPLEMENTARY: 'Supplémentaire',
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceApiService: ResourceApiService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.documentId = +params['id'];
      if (this.documentId) {
        this.loadDocument();
      }
    });
  }

  loadDocument(): void {
    this.resourceApiService.getFileById(this.documentId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.document = response.data;
          this.loadPresignedUrl();
        } else {
          this.showError('Document non trouvé');
          this.goBack();
        }
      },
      error: (error) => {
        console.error('Error fetching document:', error);
        this.showError('Erreur lors du chargement du document');
        this.goBack();
      },
    });
  }

  loadPresignedUrl(): void {
    if (this.document) {
      this.resourceApiService.getPresignedUrl(this.document.id).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.pdfUrl = response.data;
          }
        },
        error: (error) => {
          console.error('Error fetching presigned URL:', error);
          this.showError('Erreur lors du chargement du document');
        },
      });
    }
  }

  openPdfModal(): void {
    if (!this.pdfUrl) {
      this.showError('Le document n\'est pas encore chargé');
      return;
    }
    this.showPdfModal = true;
  }

  downloadDocument(): void {
    if (this.pdfUrl) {
      window.open(this.pdfUrl, '_blank');
    }
  }

  goBack(): void {
    this.router.navigate(['/student/library']);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }

  private showError(message: string): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erreur',
      detail: message,
      life: 3000,
    });
  }
}
