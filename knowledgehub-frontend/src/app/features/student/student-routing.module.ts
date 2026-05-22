import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StudentLayoutComponent } from './student-layout/student-layout.component';
import { PfeComponent } from './pages/pfe/pfe.component';
import { StageComponent } from './pages/stage/stage.component';
import { LibraryComponent } from '../../shared/components/library/library.component';
import { DemandePfeComponent } from './pages/demande.pfe/demande.pfe.component';
import { ProfileComponent } from '../../shared/components/profile/profile.component';
import { StudentStageDetailsComponent } from './pages/stage/stage-details/stage-details.component';
import { DocumentComponent } from '../../shared/components/library/document/document.component';
import { StudentAnnonceComponent } from './pages/annonce/annonce.component';
import { StudentAnnonceDetailsComponent } from './pages/annonce/annonce-details.component';

const routes: Routes = [
  {
    path: '',
    component: StudentLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'library',
        pathMatch: 'full',
      },
      {
        path: 'pfe/mes-projets',
        component: PfeComponent,
      },
      {
        path: 'stage',
        component: StageComponent,
      },
      {
        path: 'stage/:id',
        component: StudentStageDetailsComponent,
      },
      {
        path: 'library',
        component: LibraryComponent,
      },
      {
        path: 'annonces',
        component: StudentAnnonceComponent,
      },
      {
        path: 'annonces/:id',
        component: StudentAnnonceDetailsComponent,
      },
      {
        path: 'pfe/demandes',
        component: DemandePfeComponent,
      },
      {
        path: 'profile',
        component: ProfileComponent,
      },
      {
        path: 'document/:id',
        component: DocumentComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class StudentRoutingModule {}
