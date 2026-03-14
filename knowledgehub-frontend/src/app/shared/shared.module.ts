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
import { LibraryComponent } from './components/library/library.component';
import { DocumentComponent } from './components/library/document/document.component';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { Badge } from 'primeng/badge';
import { TooltipModule } from 'primeng/tooltip';
import { FormsModule } from '@angular/forms';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { CardModule } from 'primeng/card';
import { TopbarComponent } from './components/topbar/topbar.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { LogoutModalComponent } from './components/logout-modal/logout-modal.component';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    ProfileComponent,
    PdfViewerModalComponent,
    LibraryComponent,
    DocumentComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ProgressSpinnerModule,
    LoadingOverlayComponent,
    ReactiveFormsModule,
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
    FormsModule,
    ToggleSwitchModule,
    CardModule,
    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
  ],
  exports: [
    FileSizePipe,
    DateFormatPipe,
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
})
export class SharedModule {}
