import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

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
@NgModule({
  declarations: [
    TeacherLayoutComponent,
    EncadrementsComponent,
    JuryComponent,
    RapporteurComponent,
    ConsulterComponent,
    PublierComponent,
  ],
  imports: [
    SharedModule,
    CommonModule,
    TopbarComponent,
    SidebarComponent,
    LogoutModalComponent,
    TeacherRoutingModule,
  ],
})
export class TeacherModule {}
