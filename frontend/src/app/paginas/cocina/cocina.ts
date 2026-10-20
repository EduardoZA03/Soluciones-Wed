import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { Pedido } from '../../core/modelos';

/** Vista de cocina: pedidos activos, se actualiza sola cada 10 segundos. */
@Component({
  selector: 'app-cocina',
  templateUrl: './cocina.html',
})
export class Cocina implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private temporizador?: ReturnType<typeof setInterval>;

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly porPreparar = computed(() => this.pedidos().filter((p) => p.estado === 'Pendiente'));
  protected readonly enPreparacion = computed(() => this.pedidos().filter((p) => p.estado === 'En cocina'));

  ngOnInit(): void {
    this.cargar();
    this.temporizador = setInterval(() => this.cargar(), 10000);
  }

  ngOnDestroy(): void {
    clearInterval(this.temporizador);
  }

  cargar(): void {
    this.api.pedidos(true).subscribe({ next: (l) => this.pedidos.set(l), error: () => {} });
  }

  avanzar(p: Pedido, estado: string): void {
    this.api.cambiarEstadoPedido(p.id, estado).subscribe({ next: () => this.cargar(), error: (e) => this.avisos.errorHttp(e) });
  }
}
