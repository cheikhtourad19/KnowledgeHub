import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { LoadingOverlayComponent } from './components/loading-overlay/loading-overlay.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    ProfileComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ProgressSpinnerModule,
    LoadingOverlayComponent,
    ReactiveFormsModule,
  ],
  exports: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
    LoadingOverlayComponent,
    ProfileComponent,
  ],
})
export class SharedModule {}
