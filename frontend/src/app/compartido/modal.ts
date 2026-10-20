import { Component, input, output } from '@angular/core';

/**
 * Ventana modal con el estilo de Bootstrap, controlada por Angular.
 * Uso:
 *   @if (abierto()) {
 *     <app-modal titulo="Nueva categoría" (cerrar)="abierto.set(false)"> ...formulario... </app-modal>
 *   }
 */
@Component({
  selector: 'app-modal',
  template: `
    <div class="modal-fondo" (click)="cerrar.emit()"></div>
    <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true" [attr.aria-label]="titulo()">
      <div class="modal-dialog modal-dialog-centered" [class.modal-lg]="grande()">
        <div class="modal-content">
          <div class="modal-header border-0 pb-0">
            <h5 class="modal-title fw-semibold">{{ titulo() }}</h5>
            <button type="button" class="btn-close" aria-label="Cerrar" (click)="cerrar.emit()"></button>
          </div>
          <div class="modal-body">
            <ng-content />
          </div>
        </div>
      </div>
    </div>
  `,
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
})
export class Modal {
  readonly titulo = input.required<string>();
  readonly grande = input(false);
  readonly cerrar = output<void>();
}
