import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourceApiService } from '../../services/resource-api.service';
import { File } from '../../models/resource-service/entities/file.model';
import { FileType } from '../../models/resource-service/enums/file-type.enum';
import { Input, Output, EventEmitter } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-library',
  standalone: false,
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements OnInit {
  public documents: File[] = [];
  public coverUrls: Record<number, string> = {};
  public searchQuery: string = '';
  public activeFilter: string = 'ALL';
  public aiSearchEnabled: boolean = false;
  public isAiLoading: boolean = false;
  public isAiResults: boolean = false;
  public loading: boolean = false;
  @Input() documentRoute: string = 'student/document'; // default, can be overridden

  public displayableTypes: FileType[] = [
    FileType.PFA_REPORT,
    FileType.PFE_REPORT,
    FileType.STAGE_REPORT,
    FileType.EDUCATIONAL_MATERIAL,
    FileType.SUPPLEMENTARY,
  ];

  public typeLabels: Record<string, string> = {
    [FileType.PFA_REPORT]: 'Rapport PFA',
    [FileType.PFE_REPORT]: 'Rapport PFE',
    [FileType.STAGE_REPORT]: 'Rapport de Stage',
    [FileType.JOURNAL]: 'Journal',
    [FileType.ATTESTATION]: 'Attestation',
    [FileType.DEMANDE]: 'Demande',
    [FileType.EDUCATIONAL_MATERIAL]: 'Matériel Pédagogique',
    [FileType.SUPPLEMENTARY]: 'Supplémentaire',
  };

  private thumbnailClasses: Partial<Record<FileType, string>> = {
    [FileType.PFA_REPORT]: 'bg-gradient-to-br from-[#1a1a2e] to-[#0d3b66]',
    [FileType.PFE_REPORT]: 'bg-gradient-to-br from-[#1a1a2e] to-[#1b4332]',
    [FileType.STAGE_REPORT]: 'bg-gradient-to-br from-[#1a1a2e] to-[#3d1a00]',
    [FileType.EDUCATIONAL_MATERIAL]:
      'bg-gradient-to-br from-[#2c1654] to-[#1a1a2e]',
    [FileType.SUPPLEMENTARY]: 'bg-gradient-to-br from-[#1a1a2e] to-[#2d3436]',
  };

  private typeImages: Partial<Record<FileType, string>> = {
    [FileType.PFA_REPORT]: 'assets/images/doc-types/pfa-report.png',
    [FileType.PFE_REPORT]: 'assets/images/pfe.png',
    [FileType.STAGE_REPORT]: 'assets/images/stage.png',
    [FileType.EDUCATIONAL_MATERIAL]: 'assets/images/materiel.png',
    [FileType.SUPPLEMENTARY]: 'assets/images/supplimentaire.png',
  };

  /** Snapshot of the original full list, used to restore after AI search is cleared */
  private allDocuments: File[] = [];

  constructor(
    private ressourceService: ResourceApiService,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.ressourceService.getPublishedDocuments().subscribe({
      next: (response) => {
        this.loading = false;
        const all: File[] = response.data || [];
        this.allDocuments = all.filter((doc) =>
          this.displayableTypes.includes(doc.fileType),
        );
        this.documents = [...this.allDocuments];
        this.preloadCoverUrls(this.allDocuments);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error fetching documents:', error);
      },
    });
  }

  // ── Search ───────────────────────────────────────────────────

  /** Keystroke handler — only used for normal (non-AI) mode */
  onSearch(): void {
    if (this.aiSearchEnabled) return; // AI mode waits for Enter
    this.isAiResults = false;
  }

  /**
   * Enter key handler.
   * - AI ON  → call backend AI search endpoint
   * - AI OFF → no-op (filtering is already reactive via filteredDocuments getter)
   */
  onEnter(): void {
    if (!this.aiSearchEnabled) return;

    const q = this.searchQuery.trim();
    if (!q) {
      this.documents = [...this.allDocuments];
      this.isAiResults = false;
      return;
    }

    this.performAiSearch(q);
  }

  private performAiSearch(query: string): void {
    this.isAiLoading = true;
    this.isAiResults = false;

    this.ressourceService.searchDocuments(query).subscribe({
      next: (aiResults) => {
        const aiDocuments: File[] = (aiResults.data || []).filter((doc) =>
          this.displayableTypes.includes(doc.fileType),
        );

        const q = query.toLowerCase();
        const metadataResults = this.allDocuments.filter(
          (doc) =>
            this.displayableTypes.includes(doc.fileType) &&
            (doc.title.toLowerCase().includes(q) ||
              doc.description?.toLowerCase().includes(q) ||
              doc.originalFileName?.toLowerCase().includes(q)),
        );

        const mergedMap = new Map<number, File>();
        aiDocuments.forEach((doc) => mergedMap.set(doc.id, doc));
        metadataResults.forEach((doc) => {
          if (!mergedMap.has(doc.id)) {
            mergedMap.set(doc.id, doc);
          }
        });

        this.documents = Array.from(mergedMap.values());
        this.preloadCoverUrls(this.documents);
        this.isAiResults = true;
        this.isAiLoading = false;
      },
      error: (error) => {
        console.error('AI search error:', error);
        this.isAiLoading = false;
      },
    });
  }

  // ── AI Toggle ────────────────────────────────────────────────

  /** Switching AI OFF restores the original list and clears state */
  onAiToggle(): void {
    if (!this.aiSearchEnabled) {
      this.documents = [...this.allDocuments];
      this.isAiResults = false;
      this.searchQuery = '';
    }
  }

  // ── Filters / Getters ────────────────────────────────────────

  get filteredDocuments(): File[] {
    if (this.isAiResults) {
      return this.activeFilter === 'ALL'
        ? this.documents
        : this.documents.filter((doc) => doc.fileType === this.activeFilter);
    }

    let result = this.documents;

    if (this.activeFilter !== 'ALL') {
      result = result.filter((doc) => doc.fileType === this.activeFilter);
    }

    const q = this.searchQuery.trim().toLowerCase();
    if (q) {
      result = result.filter(
        (doc) =>
          doc.title.toLowerCase().includes(q) ||
          doc.description?.toLowerCase().includes(q) ||
          doc.originalFileName?.toLowerCase().includes(q),
      );
    }

    return result;
  }

  setFilter(type: string): void {
    this.activeFilter = type;
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.activeFilter = 'ALL';
    this.aiSearchEnabled = false;
    this.isAiResults = false;
    this.documents = [...this.allDocuments];
  }

  // ── Helpers ──────────────────────────────────────────────────

  openDocument(doc: File): void {
    const role = this.authService.currentAuthState?.role;

    if (role === 'STUDENT') {
      this.router.navigate([`student/document`, doc.id]);
    } else if (role === 'TEACHER') {
      this.router.navigate([`teacher/bibliotheque/consulter`, doc.id]);
    }
  }

  getTypeImage(fileType: FileType): string | null {
    return this.typeImages[fileType] ?? null;
  }

  private preloadCoverUrls(files: File[]): void {
    files
      .filter((file) => file.coverImagePath && !this.coverUrls[file.id])
      .forEach((file) => {
        this.ressourceService.getCoverPresignedUrl(file.id).subscribe({
          next: (response) => {
            if (response.success && response.data) {
              this.coverUrls[file.id] = response.data;
            }
          },
          error: () => {
            // Keep placeholders when no cover URL can be fetched.
          },
        });
      });
  }

  getThumbnailClass(fileType: FileType): string {
    return this.thumbnailClasses[fileType] ?? 'bg-[#1a1a2e]';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }
}
