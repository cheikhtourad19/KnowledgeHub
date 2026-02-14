import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

@Pipe({
  name: 'dateFormat'
})
export class DateFormatPipe implements PipeTransform {
  private datePipe: DatePipe = new DatePipe('en-US');

  transform(value: Date | string | number | null | undefined, format: string = 'medium'): string | null {
    if (!value) return null;

    return this.datePipe.transform(value, format);
  }

}
