import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PasswordModule } from 'primeng/password';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { Badge } from 'primeng/badge';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { TooltipModule } from 'primeng/tooltip';
import { TeacherRoutingModule } from './teacher-routing.module';
import { TeacherLayoutComponent } from './teacher-layout/teacher-layout.component';
import { TopbarComponent } from '../../shared/components/topbar/topbar.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { LogoutModalComponent } from '../../shared/components/logout-modal/logout-modal.component';
import { EncadrementsComponent } from './pages/pfe/encadrements/encadrements.component';
import { JuryComponent } from './pages/pfe/jury/jury.component';
import { RapporteurComponent } from './pages/pfe/rapporteur/rapporteur.component';
import { ConsulterComponent } from './pages/bibliotheque/consulter/consulter.component';
import { PublierComponent } from './pages/bibliotheque/publier/publier.component';
import { SharedModule } from '../../shared/shared.module';
import { PfeDetailsComponent } from './pages/pfe/pfe-details/pfe-details.component';
import { EncadrementDetailsComponent } from './pages/pfe/encadrements/encadrement-details/encadrement-details.component';
import { PfeDetailsViewComponent } from '../../shared/components/pfe-details-view/pfe-details-view.component';
import { PfeTableComponent } from '../../shared/components/pfe-table/pfe-table.component';
import { FormsModule } from '@angular/forms';
import { JuryComponent as StageJuryComponent } from './pages/stage/jury/jury.component';
import { StageDetailsComponent } from './pages/stage/jury/stage-details/stage-details.component';

import { MarkdownModule } from 'ngx-markdown';
@NgModule({
  declarations: [
    TeacherLayoutComponent,
    EncadrementsComponent,
    JuryComponent,
    RapporteurComponent,
    StageJuryComponent,
    ConsulterComponent,
    PublierComponent,
    PfeDetailsComponent,
    EncadrementDetailsComponent,
    StageDetailsComponent,
  ],
  imports: [
    PfeDetailsViewComponent,
    MarkdownModule.forChild(),
    FormsModule,
    PfeTableComponent,
    PasswordModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    ToastModule,
    FileUploadModule,
    Badge,
    PdfViewerModule,
    TooltipModule,
    SharedModule,
    CommonModule,

    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
    TeacherRoutingModule,
  ],
})
export class TeacherModule {}
