import { provideZoneChangeDetection } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import './pdf-worker.config';

import { environment } from './environments/environment';

if (environment.production) {
  window.console.log = () => {};
  window.console.debug = () => {};
  window.console.info = () => {};
}

platformBrowserDynamic()
  .bootstrapModule(AppModule, {
    applicationProviders: [provideZoneChangeDetection()],
  })
  .catch((err) => console.error(err));
