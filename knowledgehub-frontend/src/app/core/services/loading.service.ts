// src/app/core/services/loading.service.ts
import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  isLoading = signal(false);

  private activeRequests = 0;
  private hideTimer: ReturnType<typeof setTimeout> | null = null;
  private showTime: number | null = null;
  private readonly MIN_DISPLAY_MS = 1000;

  show(): void {
    this.activeRequests++;

    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }

    if (!this.isLoading()) {
      this.showTime = Date.now();
      this.isLoading.set(true);
    }
  }

  hide(): void {
    this.activeRequests = Math.max(0, this.activeRequests - 1);

    if (this.activeRequests > 0) return;

    const elapsed = this.showTime
      ? Date.now() - this.showTime
      : this.MIN_DISPLAY_MS;
    const remaining = Math.max(0, this.MIN_DISPLAY_MS - elapsed);

    this.hideTimer = setTimeout(() => {
      this.isLoading.set(false);
      this.showTime = null;
      this.hideTimer = null;
    }, remaining);
  }
}
