import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Modal } from '../../compartido/modal';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { ProveedoresService } from '../../core/maestros.service';
import { ESTADOS_ORDEN, OrdenCompra, Proveedor, tono } from '../../core/modelos';

/** Listado de órdenes de compra (HU-06). */
@Component({
  selector: 'app-compras',
  imports: [RouterLink, DatePipe, DecimalPipe, Modal],
  templateUrl: './compras.html',
})
export class Compras implements OnInit {
  private readonly api = inject(ApiService);
  private readonly proveedoresServicio = inject(ProveedoresService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly tono = tono;
  protected readonly estados = ESTADOS_ORDEN;

  protected readonly ordenes = signal<OrdenCompra[]>([]);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly filtro = signal('');
  protected readonly modalAbierto = signal(false);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.cargar();
    this.proveedoresServicio.listar().subscribe({ next: (p) => this.proveedores.set(p), error: () => {} });
  }

  filtrar(estado: string): void {
    this.filtro.set(estado);
    this.cargar();
  }

  cargar(): void {
    this.api.ordenes(this.filtro() || undefined).subscribe({
      next: (l) => { this.ordenes.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  crear(proveedorId: string): void {
    if (!proveedorId) {
      this.avisos.error('Elige un proveedor');
      return;
    }
    this.api.crearOrden(Number(proveedorId)).subscribe({
      next: (o) => { this.avisos.exito('Orden creada en borrador. Agrega los insumos.'); this.router.navigate(['/compras', o.id]); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }
}
