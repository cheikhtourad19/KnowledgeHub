import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/not-found/not-found.component';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
  ],
  imports: [CommonModule, RouterModule],
  exports: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent,
    NotFoundComponent,
  ],
})
export class SharedModule {}
