import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { StudentsGestionComponent } from './pages/students.gestion/students.gestion.component';
import { StudentsClasseComponent } from './pages/students.classe/students.classe.component';
import { PfeGestionComponent } from './pages/pfe.gestion/pfe.gestion.component';
import { PfeDemandeComponent } from './pages/pfe.demande/pfe.demande.component';
import { DemandeDetailsComponent } from './pages/pfe.demande/demande-details/demande-details.component';
import { StageComponent } from './pages/stage/stage.component';
import { StageDetailsComponent } from './pages/stage/stage-details/stage-details.component';
import { EnseignantComponent } from './pages/enseignant/enseignant.component';
import { DocumentsComponent } from './pages/documents/documents.component';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { SharedModule } from '../../shared/shared.module';
import { PasswordModule } from 'primeng/password';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { Dialog, DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { Badge } from 'primeng/badge';
import { TooltipModule } from 'primeng/tooltip';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { PfeDetailsComponent } from './pages/pfe.gestion/pfe-details/pfe-details.component';
import { TopbarComponent } from '../../shared/components/topbar/topbar.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { LogoutModalComponent } from '../../shared/components/logout-modal/logout-modal.component';
@NgModule({
  declarations: [
    AdminLayoutComponent,
    StudentsGestionComponent,
    StudentsClasseComponent,
    PfeGestionComponent,
    PfeDemandeComponent,
    DemandeDetailsComponent,
    StageComponent,
    StageDetailsComponent,
    EnseignantComponent,
    DocumentsComponent,
    PfeDetailsComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
    FormsModule,
    PasswordModule,
    AdminRoutingModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,

    ToastModule,
    Badge,
    FileUploadModule,
    ConfirmDialogModule,
    ReactiveFormsModule,
    TooltipModule,
    ToggleSwitchModule,
  ],
})
export class AdminModule {}
