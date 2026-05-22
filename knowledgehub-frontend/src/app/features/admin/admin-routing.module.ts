import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { StudentsClasseComponent } from './pages/students.classe/students.classe.component';
import { StudentsGestionComponent } from './pages/students.gestion/students.gestion.component';
import { PfeGestionComponent } from './pages/pfe.gestion/pfe.gestion.component';
import { PfeDemandeComponent } from './pages/pfe.demande/pfe.demande.component';
import { DemandeDetailsComponent } from './pages/pfe.demande/demande-details/demande-details.component';
import { StageComponent } from './pages/stage/stage.component';
import { StageDetailsComponent } from './pages/stage/stage-details/stage-details.component';
import { DocumentsComponent } from './pages/documents/documents.component';
import { EnseignantComponent } from './pages/enseignant/enseignant.component';
import { NotFoundComponent } from '../../shared/components/not-found/not-found.component';
import { PfeDetailsComponent } from './pages/pfe.gestion/pfe-details/pfe-details.component';
import { ProfileComponent } from '../../shared/components/profile/profile.component';
import { AnnonceDetailsComponent } from './pages/annonce/annonce-details/annonce-details.component';
import { AnnonceComponent } from './pages/annonce/annonce.component';

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
        path: 'stage/:id',
        component: StageDetailsComponent,
      },
      {
        path: 'documents',
        component: DocumentsComponent,
      },
      {
        path: 'teachers',
        component: EnseignantComponent,
      },
      {
        path: 'pfe-details/:id',
        component: PfeDetailsComponent,
      },
      {
        path: 'profile',
        component: ProfileComponent,
      },
      {
        path:'annonce',
        component: AnnonceComponent,
      },
      {
        path: 'annonce/:id',
        component: AnnonceDetailsComponent,
      }
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
