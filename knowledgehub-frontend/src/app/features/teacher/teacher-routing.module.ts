import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TeacherLayoutComponent } from './teacher-layout/teacher-layout.component';
import { EncadrementsComponent } from './pages/pfe/encadrements/encadrements.component';
import { JuryComponent } from './pages/pfe/jury/jury.component';
import { RapporteurComponent } from './pages/pfe/rapporteur/rapporteur.component';
import { JuryComponent as StageJuryComponent } from './pages/stage/jury/jury.component';
import { ConsulterComponent } from './pages/bibliotheque/consulter/consulter.component';
import { PublierComponent } from './pages/bibliotheque/publier/publier.component';
import { ProfileComponent } from '../../shared/components/profile/profile.component';
import { LibraryComponent } from '../../shared/components/library/library.component';
import { DocumentComponent } from '../../shared/components/library/document/document.component';
import { EncadrementDetailsComponent } from './pages/pfe/encadrements/encadrement-details/encadrement-details.component';
import { PfeDetailsComponent } from './pages/pfe/pfe-details/pfe-details.component';
import { StageDetailsComponent } from './pages/stage/jury/stage-details/stage-details.component';
const routes: Routes = [
  {
    path: '',
    component: TeacherLayoutComponent,
    children: [
      { path: '', redirectTo: 'pfe/encadrements', pathMatch: 'full' },

      { path: 'pfe/encadrements', component: EncadrementsComponent },
      { path: 'pfe/encadrements/:id', component: EncadrementDetailsComponent },

      { path: 'pfe/jury', component: JuryComponent },
      { path: 'pfe/jury/:id', component: PfeDetailsComponent },
      { path: 'pfe/rapporteur', component: RapporteurComponent },
      { path: 'pfe/rapporteur/:id', component: PfeDetailsComponent },

      { path: 'stage/jury', component: StageJuryComponent },
      { path: 'stage/jury/:id', component: StageDetailsComponent },

      { path: 'bibliotheque/consulter', component: LibraryComponent },
      { path: 'bibliotheque/consulter/:id', component: DocumentComponent },

      { path: 'bibliotheque/publier', component: PublierComponent },

      { path: 'profile', component: ProfileComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TeacherRoutingModule {}
