import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

export interface Aviso {
  id: number;
  tipo: 'success' | 'danger';
  texto: string;
}

/** Mensajes flotantes de éxito o error (se cierran solos a los 5 segundos). */
@Injectable({ providedIn: 'root' })
export class AvisosService {
  readonly avisos = signal<Aviso[]>([]);
  private siguiente = 1;

  exito(texto: string): void {
    this.mostrar('success', texto);
  }

  error(texto: string): void {
    this.mostrar('danger', texto);
  }

  /** Muestra el mensaje que envía el backend ({"mensaje": "..."}) o uno genérico. */
  errorHttp(e: unknown): void {
    if (e instanceof HttpErrorResponse) {
      if (e.status === 0) {
        this.error('No se pudo conectar con el servidor. ¿Está encendido el backend?');
        return;
      }
      if (e.status === 403) {
        this.error('Tu rol no tiene permiso para esta acción.');
        return;
      }
      this.error(e.error?.mensaje ?? 'Error del servidor');
      return;
    }
    this.error('Ocurrió un error inesperado');
  }

  cerrar(id: number): void {
    this.avisos.update((lista) => lista.filter((a) => a.id !== id));
  }

  private mostrar(tipo: Aviso['tipo'], texto: string): void {
    const id = this.siguiente++;
    this.avisos.update((lista) => [...lista, { id, tipo, texto }]);
    setTimeout(() => this.cerrar(id), 5000);
  }
}
