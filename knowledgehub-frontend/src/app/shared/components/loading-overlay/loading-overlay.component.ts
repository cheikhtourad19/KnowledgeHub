// src/app/shared/components/loading-overlay/loading-overlay.component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-loading-overlay',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule],
  template: `
    @if (loadingService.isLoading()) {
      <div
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 backdrop-blur-sm"
      >
        <div
          class="bg-white rounded-2xl p-8 flex flex-col items-center space-y-4 shadow-2xl"
        >
          <p-progress-spinner
            strokeWidth="4"
            fill="transparent"
            animationDuration=".8s"
            [style]="{ width: '60px', height: '60px' }"
            ariaLabel="loading"
          />
          <span class="text-sm font-medium text-[#333333]">Chargement...</span>
        </div>
      </div>
    }
  `,
})
export class LoadingOverlayComponent {
  loadingService = inject(LoadingService);
}
