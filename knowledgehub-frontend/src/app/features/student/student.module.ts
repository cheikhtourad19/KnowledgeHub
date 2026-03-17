import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { Dialog, DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { Badge } from 'primeng/badge';
import { TooltipModule } from 'primeng/tooltip';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { CardModule } from 'primeng/card';
import { StudentRoutingModule } from './student-routing.module';
import { StudentLayoutComponent } from './student-layout/student-layout.component';
import { RouterModule } from '@angular/router';
import { PfeComponent } from './pages/pfe/pfe.component';
import { StageComponent } from './pages/stage/stage.component';
import { StudentStageDetailsComponent } from './pages/stage/stage-details/stage-details.component';
import { DemandePfeComponent } from './pages/demande.pfe/demande.pfe.component';
import { SharedModule } from '../../shared/shared.module';
import { TopbarComponent } from '../../shared/components/topbar/topbar.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { LogoutModalComponent } from '../../shared/components/logout-modal/logout-modal.component';
import { MarkdownModule } from 'ngx-markdown';
@NgModule({
  declarations: [
    StudentLayoutComponent,
    PfeComponent,
    StageComponent,
    StudentStageDetailsComponent,
    DemandePfeComponent,
  ],
  imports: [
    CommonModule,
    DatePickerModule,
    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
    MarkdownModule.forChild(),
    TagModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    PdfViewerModule,
    ToastModule,
    ToggleSwitchModule,
    Badge,
    FileUploadModule,
    ConfirmDialogModule,
    ReactiveFormsModule,
    TooltipModule,
    CardModule,
    StudentRoutingModule,
    RouterModule,
    SharedModule,
  ],
})
export class StudentModule {}
