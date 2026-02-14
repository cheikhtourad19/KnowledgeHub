import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileSizePipe } from './pipes/file-size.pipe';
import { DateFormatPipe } from './pipes/date-format.pipe';



@NgModule({
  declarations: [
    FileSizePipe,
    DateFormatPipe
  ],
  imports: [
    CommonModule
  ]
})
export class SharedModule { }
