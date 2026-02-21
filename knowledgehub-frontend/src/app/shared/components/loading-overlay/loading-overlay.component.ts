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
          class="bg-white dark:bg-gray-900 rounded-lg shadow-2xl overflow-hidden flex flex-col items-center border border-gray-200 dark:border-gray-700 min-w-[160px]"
        >
          <!-- Red accent top bar -->
          <div class="w-full h-1 bg-[#C60C30]"></div>

          <div class="px-8 py-7 flex flex-col items-center gap-4">
            <p-progress-spinner
              strokeWidth="3"
              fill="transparent"
              animationDuration=".8s"
              [style]="{ width: '48px', height: '48px' }"
              ariaLabel="loading"
            />
            <div class="flex items-center gap-2">
              <span
                class="w-1.5 h-1.5 rounded-full bg-[#C60C30] animate-bounce [animation-delay:0ms]"
              ></span>
              <span
                class="w-1.5 h-1.5 rounded-full bg-[#C60C30] animate-bounce [animation-delay:150ms]"
              ></span>
              <span
                class="w-1.5 h-1.5 rounded-full bg-[#C60C30] animate-bounce [animation-delay:300ms]"
              ></span>
            </div>
            <span
              class="text-xs font-bold uppercase tracking-widest text-[#333333] dark:text-gray-300"
            >
              Chargement
            </span>
          </div>
        </div>
      </div>
    }
  `,
})
export class LoadingOverlayComponent {
  loadingService = inject(LoadingService);
}
