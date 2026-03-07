// src/app/shared/components/loading-overlay/loading-overlay.component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-loading-overlay',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule],
  styles: [
    `
      @keyframes pulse-ring {
        0% {
          transform: scale(0.88);
          opacity: 0.5;
        }
        50% {
          transform: scale(1.08);
          opacity: 0.15;
        }
        100% {
          transform: scale(0.88);
          opacity: 0.5;
        }
      }
      .pulse-ring {
        animation: pulse-ring 2s ease-in-out infinite;
      }

      @keyframes fade-in {
        from {
          opacity: 0;
          transform: scale(0.95) translateY(8px);
        }
        to {
          opacity: 1;
          transform: scale(1) translateY(0);
        }
      }
      .fade-in {
        animation: fade-in 0.3s ease-out forwards;
      }
    `,
  ],
  template: `
    @if (loadingService.isLoading()) {
      <div
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 backdrop-blur-sm"
      >
        <div
          class="fade-in bg-white dark:bg-gray-900 rounded-2xl shadow-2xl overflow-hidden flex flex-col items-center border border-gray-200 dark:border-gray-700 min-w-[220px]"
        >
          <!-- Red accent top bar -->
          <div class="w-full h-1.5 bg-[#C60C30]"></div>

          <div class="px-10 py-8 flex flex-col items-center gap-5">
            <!-- Logo + spinner -->
            <div class="relative flex items-center justify-center w-24 h-24">
              <!-- Outer pulse ring -->
              <div
                class="pulse-ring absolute inset-0 rounded-full border-2 border-[#C60C30]/25"
              ></div>

              <!-- Spinner — full size of container -->
              <p-progress-spinner
                strokeWidth="3"
                fill="transparent"
                animationDuration=".8s"
                [style]="{ width: '96px', height: '96px' }"
                ariaLabel="loading"
                class="absolute inset-0"
              />

              <!-- Logo pill — large, centered inside spinner -->
            </div>

            <!-- Bouncing dots -->
            <div class="flex items-center gap-1.5">
              <span
                class="w-2 h-2 rounded-full bg-[#C60C30] animate-bounce [animation-delay:0ms]"
              ></span>
              <span
                class="w-2 h-2 rounded-full bg-[#C60C30] animate-bounce [animation-delay:150ms]"
              ></span>
              <span
                class="w-2 h-2 rounded-full bg-[#C60C30] animate-bounce [animation-delay:300ms]"
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
