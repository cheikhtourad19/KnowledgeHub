import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Stage } from '../../models/resource-service/entities/stage.model';
import { TypeStage } from '../../models/resource-service/enums/type-stage.enum';
import { PdfViewerModalComponent } from '../pdf-viewer-modal/pdf-viewer-modal.component';
import { SharedModule } from '../../shared.module';
import { CommonModule } from '@angular/common';
import { Dial } from 'flowbite';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';

export interface StageSupervisorOption {
  id: number;
  fullName: string;
}

@Component({
  selector: 'app-stage-details-card',
  imports: [
    CommonModule,
    SharedModule,
    DialogModule,
    SelectModule,
    FormsModule,
  ],
  templateUrl: './stage-details-card.component.html',
  styleUrls: ['./stage-details-card.component.scss'],
})
export class StageDetailsCardComponent {
  @Input() stage: Stage | null = null;
  @Input() reportFileUrl = '';
  @Input() journalFileUrl = '';
  @Input() attestationFileUrl = '';
  @Input() supervisors: StageSupervisorOption[] = [];

  @Output() back = new EventEmitter<void>();
  @Output() assignSupervisor = new EventEmitter<number>();

  // Modals
  showSupervisorModal = false;
  selectedSupervisorId: number | null = null;

  showPdfModal = false;
  currentPdfUrl = '';
  currentPdfTitle = '';

  openSupervisorModal(): void {
    this.selectedSupervisorId = this.stage?.supervisorId ?? null;
    this.showSupervisorModal = true;
  }

  closeSupervisorModal(): void {
    this.showSupervisorModal = false;
    this.selectedSupervisorId = null;
  }

  confirmSupervisor(): void {
    if (!this.selectedSupervisorId) {
      return;
    }

    this.assignSupervisor.emit(this.selectedSupervisorId);
    this.closeSupervisorModal();
  }

  getTypeStageLabel(typeStage?: TypeStage | string): string {
    if (!typeStage) return '—';
    const value = typeof typeStage === 'string' ? typeStage : typeStage;
    return value === 'TECHNICIEN' || value === TypeStage.TECHNICIEN
      ? 'Technicien'
      : 'Ouvrier';
  }

  getTypeStageClass(typeStage?: TypeStage | string): string {
    if (!typeStage)
      return 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400';
    const value = typeof typeStage === 'string' ? typeStage : typeStage;
    return value === 'TECHNICIEN' || value === TypeStage.TECHNICIEN
      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
      : 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400';
  }

  getFileCount(): string {
    if (!this.stage) return '0/3';
    let count = 0;
    if (this.stage.reportFile) count++;
    if (this.stage.journalFile) count++;
    if (this.stage.attestationFile) count++;
    return `${count}/3`;
  }

  openPdfModal(type: 'report' | 'journal' | 'attestation'): void {
    let url = '';
    let title = '';

    switch (type) {
      case 'report':
        url = this.reportFileUrl;
        title = 'Rapport de Stage';
        break;
      case 'journal':
        url = this.journalFileUrl;
        title = 'Journal de Stage';
        break;
      case 'attestation':
        url = this.attestationFileUrl;
        title = 'Attestation';
        break;
    }

    if (!url || url.trim().length === 0) {
      return;
    }

    this.currentPdfUrl = url;
    this.currentPdfTitle = title;
    this.showPdfModal = true;
  }

  closePdfModal(): void {
    this.showPdfModal = false;
    this.currentPdfUrl = '';
    this.currentPdfTitle = '';
  }
}
