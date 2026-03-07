import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { LoadingOverlayComponent } from './components/loading-overlay/loading-overlay.component';
import { ProfileComponent } from './components/profile/profile.component';
import { PdfViewerModalComponent } from './components/pdf-viewer-modal/pdf-viewer-modal.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ReactiveFormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { PdfViewerModule } from 'ng2-pdf-viewer';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    ProfileComponent,
    PdfViewerModalComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ProgressSpinnerModule,
    LoadingOverlayComponent,
    ReactiveFormsModule,
    DialogModule,
    PdfViewerModule,
  ],
  exports: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    LoadingOverlayComponent,
    ProfileComponent,
    PdfViewerModalComponent,
  ],
})
export class SharedModule {}
