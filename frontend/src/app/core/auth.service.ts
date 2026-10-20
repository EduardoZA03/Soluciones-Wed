import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { Sesion } from './modelos';

const CLAVE = 'calsoft.sesion';

/**
 * Sesión del usuario (HU-01). Guarda el token JWT que devuelve el backend
 * y los módulos que puede ver según su rol.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly sesion = signal<Sesion | null>(this.leer());
  readonly conectado = computed(() => this.sesion() !== null);
  readonly esAdministrador = computed(() => this.sesion()?.rol === 'Administrador');

  login(usuario: string, clave: string): Observable<Sesion> {
    return this.http.post<Sesion>('/api/auth/login', { usuario, clave }).pipe(
      tap((s) => this.guardar(s)),
    );
  }

  logout(): void {
    try {
      localStorage.removeItem(CLAVE);
    } catch {
      /* almacenamiento no disponible */
    }
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.sesion()?.token ?? null;
  }

  /** true si el rol del usuario puede usar el módulo (para el menú y las rutas). */
  puede(modulo: string): boolean {
    return this.sesion()?.modulos.includes(modulo) ?? false;
  }

  private guardar(s: Sesion): void {
    try {
      localStorage.setItem(CLAVE, JSON.stringify(s));
    } catch {
      /* almacenamiento no disponible: la sesión dura hasta recargar */
    }
    this.sesion.set(s);
  }

  private leer(): Sesion | null {
    try {
      const guardada = localStorage.getItem(CLAVE);
      return guardada ? (JSON.parse(guardada) as Sesion) : null;
    } catch {
      return null;
    }
  }
}
