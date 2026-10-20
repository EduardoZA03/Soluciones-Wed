import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { InsumosService } from '../../core/maestros.service';
import { Insumo, OrdenCompra, ResultadoEnvio, tono } from '../../core/modelos';

/** Detalle de una orden: edición del borrador, aprobación y envío por API, QR y línea de tiempo. */
@Component({
  selector: 'app-orden',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './orden.html',
})
export class Orden implements OnInit {
  private readonly api = inject(ApiService);
  private readonly insumosServicio = inject(InsumosService);
  private readonly avisos = inject(AvisosService);
  protected readonly auth = inject(AuthService);
  protected readonly tono = tono;

  /** :id de la ruta, enlazado por el router. */
  readonly id = input.required<string>();

  protected readonly orden = signal<OrdenCompra | null>(null);
  protected readonly insumos = signal<Insumo[]>([]);
  protected readonly enviando = signal(false);
  /** Cantidades y precios editados en el borrador: itemId → valores */
  private cambios = new Map<number, { cantidad: number; precioUnitario: number }>();

  ngOnInit(): void {
    this.cargar();
    this.insumosServicio.listar().subscribe({ next: (l) => this.insumos.set(l), error: () => {} });
  }

  cargar(): void {
    this.cambios.clear();
    this.api.orden(Number(this.id())).subscribe({
      next: (o) => this.orden.set(o),
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  qr(codigo: string): string {
    return `/api/qr?texto=${encodeURIComponent(codigo)}`;
  }

  editar(itemId: number, campo: 'cantidad' | 'precioUnitario', valor: string, actual: { cantidad: number; precioUnitario: number }): void {
    const c = this.cambios.get(itemId) ?? { ...actual };
    c[campo] = Number(valor);
    this.cambios.set(itemId, c);
  }

  guardarCambios(): void {
    const o = this.orden()!;
    const items = [...this.cambios.entries()].map(([id, v]) => ({ id, ...v }));
    if (items.length === 0) {
      this.avisos.exito('No hay cambios por guardar');
      return;
    }
    this.api.actualizarItemsOrden(o.id, items).subscribe({
      next: (r) => { this.cambios.clear(); this.orden.set(r); this.avisos.exito('Cambios guardados'); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  agregar(insumoId: string, cantidad: string): void {
    if (!insumoId || !(Number(cantidad) > 0)) {
      this.avisos.error('Elige un insumo y una cantidad mayor que cero');
      return;
    }
    this.api.agregarItemOrden(this.orden()!.id, Number(insumoId), Number(cantidad)).subscribe({
      next: (r) => this.orden.set(r),
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  quitar(itemId: number): void {
    this.api.quitarItemOrden(this.orden()!.id, itemId).subscribe({
      next: (r) => this.orden.set(r),
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  aprobar(): void {
    const o = this.orden()!;
    if (!confirm(`¿Aprobar la orden por S/ ${o.total.toFixed(2)} y enviarla al proveedor?`)) {
      return;
    }
    this.enviar(this.api.aprobarOrden(o.id));
  }

  reenviar(): void {
    this.enviar(this.api.reenviarOrden(this.orden()!.id));
  }

  cancelar(): void {
    if (!confirm('¿Cancelar esta orden?')) {
      return;
    }
    this.api.cancelarOrden(this.orden()!.id).subscribe({
      next: (r) => { this.orden.set(r); this.avisos.exito('Orden cancelada'); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  private enviar(peticion: import('rxjs').Observable<ResultadoEnvio>): void {
    this.enviando.set(true);
    peticion.subscribe({
      next: (r) => {
        this.enviando.set(false);
        r.exito ? this.avisos.exito(r.mensaje) : this.avisos.error(r.mensaje);
        this.cargar();
      },
      error: (e) => { this.enviando.set(false); this.avisos.errorHttp(e); this.cargar(); },
    });
  }
}
