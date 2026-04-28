import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceApiService } from '../../../services/resource-api.service';
import { File } from '../../../models/resource-service/entities/file.model';
import { MessageService } from 'primeng/api';
import { Location } from '@angular/common'; // ← make sure this import exists


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
  aiQuestion: string = '';
  aiResponse: string = '';
  isSummarizing: boolean = false;
  isAskingAi: boolean = false;

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
  private messageService: MessageService,
  private location: Location,   // ← full type name, not 'Lo'
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
      this.showError("Le document n'est pas encore chargé");
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
    this.location.back();
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
  summarizeDocument(): void {
    if (!this.document) return;
    this.isSummarizing = true;
    this.aiResponse = '';

    this.resourceApiService
      .askDocumentQuestion(this.document.id, 'Résumez ce document ')
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.aiResponse = response.data.data;
          }
          this.isSummarizing = false;
        },
        error: (error) => {
          console.error('Error summarizing document:', error);
          this.showError('Erreur lors de la génération du résumé');
          this.isSummarizing = false;
        },
      });
  }

  askAI(): void {
    if (!this.document || !this.aiQuestion.trim()) return;
    this.isAskingAi = true;
    this.aiResponse = '';

    this.resourceApiService
      .askDocumentQuestion(this.document.id, this.aiQuestion)
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.aiResponse = response.data.data;
          }
          this.aiQuestion = '';
          this.isAskingAi = false;
        },
        error: (error) => {
          console.error('Error asking AI:', error);
          this.showError("Erreur lors de la communication avec l'IA");
          this.isAskingAi = false;
        },
      });
  }
}
