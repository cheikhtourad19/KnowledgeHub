import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { StudentsGestionComponent } from './pages/students.gestion/students.gestion.component';
import { StudentsClasseComponent } from './pages/students.classe/students.classe.component';
import { PfeGestionComponent } from './pages/pfe.gestion/pfe.gestion.component';
import { PfeDemandeComponent } from './pages/pfe.demande/pfe.demande.component';
import { StageComponent } from './pages/stage/stage.component';
import { EnseignantComponent } from './pages/enseignant/enseignant.component';
import { DocumentsComponent } from './pages/documents/documents.component';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';

@NgModule({
  declarations: [
    AdminLayoutComponent,

    StudentsGestionComponent,
    StudentsClasseComponent,
    PfeGestionComponent,
    PfeDemandeComponent,
    StageComponent,
    EnseignantComponent,
    DocumentsComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    AdminRoutingModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    ToastModule,
    FileUploadModule,
  ],
})
export class AdminModule {}
