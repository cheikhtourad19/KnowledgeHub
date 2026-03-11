import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import Aura from '@primeng/themes/aura';
import { PrimeNG, PrimeNGConfigType, providePrimeNG } from 'primeng/config';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
// import { loadingInterceptor } from './core/interceptors/loading.interceptor';
import { LoadingOverlayComponent } from './shared/components/loading-overlay/loading-overlay.component';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    CoreModule,
    SharedModule,
    BrowserAnimationsModule,
    ToastModule,
    ProgressSpinnerModule,
    LoadingOverlayComponent,
    NgxExtendedPdfViewerModule,
  ],
  providers: [
    provideHttpClient(
      withInterceptors([
        authInterceptor,
        errorInterceptor,
        //loadingInterceptor
        // ]),
      ]),
    ),
    MessageService,
    ConfirmationService,
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.dark', // 👈 add this
        },
      },
    }),
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
