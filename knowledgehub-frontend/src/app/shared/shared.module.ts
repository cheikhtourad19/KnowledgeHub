import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';

@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    FileSizePipe,
    DateFormatPipe,
    UnauthorizedComponent
  ]
})
export class SharedModule { }
