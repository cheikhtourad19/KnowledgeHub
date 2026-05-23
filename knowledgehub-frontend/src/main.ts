import { provideZoneChangeDetection } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import './pdf-worker.config';

import { environment } from './environments/environment';

const win = window as any;
if (win.__env) {
  Object.assign(environment, win.__env);
}

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
