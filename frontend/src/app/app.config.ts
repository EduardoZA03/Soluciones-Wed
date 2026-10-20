import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    // withComponentInputBinding: los parámetros de la URL (:id, ?insumo=) llegan como input() del componente
    provideRouter(routes, withComponentInputBinding()),
    // Todas las llamadas a la API llevan el token JWT
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
