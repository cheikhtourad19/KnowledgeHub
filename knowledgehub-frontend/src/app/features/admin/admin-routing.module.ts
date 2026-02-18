import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { StudentsClasseComponent } from './pages/students.classe/students.classe.component';
import { StudentsGestionComponent } from './pages/students.gestion/students.gestion.component';
import { PfeGestionComponent } from './pages/pfe.gestion/pfe.gestion.component';
import { PfeDemandeComponent } from './pages/pfe.demande/pfe.demande.component';
import { DemandeDetailsComponent } from './pages/pfe.demande/demande-details/demande-details.component';
import { StageComponent } from './pages/stage/stage.component';
import { DocumentsComponent } from './pages/documents/documents.component';
import { EnseignantComponent } from './pages/enseignant/enseignant.component';
import { NotFoundComponent } from '../../shared/components/not-found/not-found.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'students/gestion',
        pathMatch: 'full',
      },
      {
        path: 'students/gestion',
        component: StudentsGestionComponent,
      },
      {
        path: 'students/classes',
        component: StudentsClasseComponent,
      },
      {
        path: 'pfe/gestion',
        component: PfeGestionComponent,
      },
      {
        path: 'pfe/demandes',
        component: PfeDemandeComponent,
      },
      {
        path: 'pfe/demandes/:id',
        component: DemandeDetailsComponent,
      },
      {
        path: 'stage',
        component: StageComponent,
      },
      {
        path: 'documents',
        component: DocumentsComponent,
      },
      {
        path: 'teachers',
        component: EnseignantComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
