import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { PdfViewerModule } from 'ng2-pdf-viewer';

@Component({
  selector: 'app-pdf-viewer-modal',
  templateUrl: './pdf-viewer-modal.component.html',
  styleUrls: ['./pdf-viewer-modal.component.scss'],
  standalone: true,
  imports: [CommonModule, DialogModule, PdfViewerModule],
})
export class PdfViewerModalComponent {
  @Input() visible: boolean = false;
  @Input() pdfUrl: string | null = null;
  @Input() title: string = 'Document PDF';
  @Output() visibleChange = new EventEmitter<boolean>();

  closeModal() {
    this.visible = false;
    this.visibleChange.emit(false);
  }
}
