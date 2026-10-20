import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AvisosService } from './avisos.service';

/** Exige sesión iniciada; si no hay, va al login. */
export const sesionGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.conectado() ? true : inject(Router).createUrlTree(['/login']);
};

/**
 * Exige que el rol pueda usar el módulo de la ruta (data: { modulo: 'insumos' }).
 * El backend también lo valida: esto solo evita mostrar pantallas sin permiso.
 */
export const moduloGuard: CanActivateFn = (ruta) => {
  const auth = inject(AuthService);
  const modulo = ruta.data['modulo'] as string;
  if (auth.puede(modulo)) {
    return true;
  }
  inject(AvisosService).error(`Tu rol (${auth.sesion()?.rol}) no tiene acceso a ese módulo.`);
  return inject(Router).createUrlTree(['/inicio']);
};

/** El login solo se muestra si no hay sesión. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.conectado() ? inject(Router).createUrlTree(['/inicio']) : true;
};
