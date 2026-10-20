import { Component, input } from '@angular/core';

/**
 * Tarjeta de estadística reutilizable.
 * Uso: <app-stat-card etiqueta="Total" [valor]="10" icono="list-check" tono="primary" />
 */
@Component({
  selector: 'app-stat-card',
  template: `
    <div class="card h-100">
      <div class="card-body">
        <div class="d-flex align-items-center justify-content-between">
          <span class="stat-etiqueta">{{ etiqueta() }}</span>
          <span class="stat-icono bg-{{ tono() }}-subtle text-{{ tono() }}-emphasis"><i class="bi bi-{{ icono() }}"></i></span>
        </div>
        <div class="stat-valor">{{ valor() }}</div>
        @if (sub()) {
          <div class="small text-body-secondary mt-1">{{ sub() }}</div>
        }
      </div>
    </div>
  `,
})
export class StatCard {
  readonly etiqueta = input.required<string>();
  readonly valor = input<string | number | null>('—');
  readonly icono = input('graph-up');
  readonly tono = input('primary');
  readonly sub = input<string | null>(null);
}
