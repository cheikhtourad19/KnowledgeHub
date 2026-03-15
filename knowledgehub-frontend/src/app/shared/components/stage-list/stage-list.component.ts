import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Router } from '@angular/router';
import { Stage } from '../../models/resource-service/entities/stage.model';
import { TypeStage } from '../../models/resource-service/enums/type-stage.enum';
import { SharedModule } from '../../shared.module';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stage-list',
  imports: [SharedModule, SelectModule, FormsModule, CommonModule],
  templateUrl: './stage-list.component.html',
  styleUrls: ['./stage-list.component.scss'],
})
export class StageListComponent implements OnChanges {
  @Input() stages: Stage[] = [];
  @Input() loading = false;
  /**
   * If provided, the component will navigate to `${stageDetailsRoute}/${stage.id}` on row click.
   * Otherwise it will only emit the `stageClick` event.
   */
  @Input() stageDetailsRoute?: string;
  @Input() showYearFilter = true;

  @Output() stageClick = new EventEmitter<Stage>();

  filteredStages: Stage[] = [];
  availableYears: number[] = [];
  selectedYear: number | null = null;

  constructor(private router: Router) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.stages) {
      this.computeAvailableYears();
      this.applyFilter();
    }
  }

  computeAvailableYears(): void {
    const years = new Set<number>();
    this.stages.forEach((stage) => {
      if (stage.year) {
        years.add(stage.year);
      }
    });
    this.availableYears = Array.from(years).sort((a, b) => b - a);
  }

  applyFilter(): void {
    if (this.selectedYear === null) {
      this.filteredStages = [...this.stages];
    } else {
      this.filteredStages = this.stages.filter(
        (stage) => stage.year === this.selectedYear,
      );
    }
  }

  onYearChange(): void {
    this.applyFilter();
  }

  clearFilter(): void {
    this.selectedYear = null;
    this.applyFilter();
  }

  onRowClick(stage: Stage): void {
    this.stageClick.emit(stage);
    if (this.stageDetailsRoute) {
      this.router.navigate([this.stageDetailsRoute, stage.id]);
    }
  }

  getTypeStageLabel(typeStage?: TypeStage): string {
    if (!typeStage) return '—';
    return typeStage === TypeStage.TECHNICIEN ? 'Technicien' : 'Ouvrier';
  }

  getTypeStageClass(typeStage?: TypeStage): string {
    if (!typeStage)
      return 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400';
    return typeStage === TypeStage.TECHNICIEN
      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
      : 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400';
  }

  getFileCount(stage: Stage): string {
    let count = 0;
    if (stage.reportFile || stage.reportFileId) count++;
    if (stage.journalFile || stage.journalFileId) count++;
    if (stage.attestationFile || stage.attestationFileId) count++;
    return `${count}/3`;
  }
}
