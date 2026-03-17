import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Pipes
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';

// Non-standalone components (declared here)
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { ProfileComponent } from './components/profile/profile.component';
import { PdfViewerModalComponent } from './components/pdf-viewer-modal/pdf-viewer-modal.component';
import { LibraryComponent } from './components/library/library.component';
import { DocumentComponent } from './components/library/document/document.component';

// Standalone components (imported + re-exported)
import { LoadingOverlayComponent } from './components/loading-overlay/loading-overlay.component';
import { TopbarComponent } from './components/topbar/topbar.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { LogoutModalComponent } from './components/logout-modal/logout-modal.component';

// PrimeNG only what SharedModule's own declared components need
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DialogModule } from 'primeng/dialog';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { Badge } from 'primeng/badge';
import { TooltipModule } from 'primeng/tooltip';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { CardModule } from 'primeng/card';
import { MarkdownModule } from 'ngx-markdown';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    ProfileComponent,

    LibraryComponent,
    DocumentComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    PdfViewerModalComponent,
    FormsModule,
    MarkdownModule.forChild(),

    // Standalone components used inside declared components' templates
    LoadingOverlayComponent,
    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,

    // PrimeNG needed by declared components above
    ProgressSpinnerModule,
    DialogModule,
    PdfViewerModule,
    TableModule,
    ButtonModule,
    TagModule,
    InputTextModule,
    SelectModule,
    ToastModule,
    FileUploadModule,
    Badge,
    TooltipModule,
    ToggleSwitchModule,
    CardModule,
  ],
  exports: [
    // Angular essentials
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    // Pipes
    FileSizePipe,
    DateFormatPipe,
    // Components
    UnauthorizedComponent,
    NotFoundComponent,
    LoadingOverlayComponent,
    ProfileComponent,
    PdfViewerModalComponent,
    LibraryComponent,
    DocumentComponent,

    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
  ],
  // No PrimeNG in exports — each feature module imports what it needs directly
})
export class SharedModule {}
