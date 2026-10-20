import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { StatCard } from '../../compartido/stat-card';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { ESTADOS_PEDIDO, Pedido, Plato, tono } from '../../core/modelos';

/** POS: pedidos (ventas). Al registrar se descuentan los insumos según la receta (HU-03). */
@Component({
  selector: 'app-pedidos',
  imports: [ReactiveFormsModule, Modal, StatCard, DecimalPipe],
  templateUrl: './pedidos.html',
})
export class Pedidos implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly tono = tono;
  protected readonly estados = ESTADOS_PEDIDO;

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly platos = signal<Plato[]>([]);
  protected readonly filtroMesero = signal('Todos');
  protected readonly modalAbierto = signal(false);
  protected readonly cargando = signal(true);

  protected readonly meseros = computed(() => [...new Set(this.pedidos().map((p) => p.mesero))].sort());
  protected readonly visibles = computed(() =>
    this.filtroMesero() === 'Todos' ? this.pedidos() : this.pedidos().filter((p) => p.mesero === this.filtroMesero()));
  protected readonly resumen = computed(() => {
    const l = this.pedidos();
    return {
      total: l.length,
      pendientes: l.filter((p) => p.estado === 'Pendiente').length,
      cocina: l.filter((p) => p.estado === 'En cocina').length,
      entregados: l.filter((p) => p.estado === 'Entregado').length,
    };
  });

  protected readonly form = this.fb.group({
    cliente: ['', Validators.required],
    mesero: ['', Validators.required],
    platoId: this.fb.control<number | null>(null, Validators.required),
    cantidad: [1, [Validators.required, Validators.min(1)]],
    mesa: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    estado: ['Pendiente'],
  });

  ngOnInit(): void {
    this.cargar();
    this.api.platos(true).subscribe({ next: (p) => this.platos.set(p), error: () => {} });
  }

  cargar(): void {
    this.api.pedidos().subscribe({
      next: (l) => { this.pedidos.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  nuevo(): void {
    this.form.reset({ cliente: '', mesero: '', platoId: null, cantidad: 1, mesa: null, estado: 'Pendiente' });
    this.modalAbierto.set(true);
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.api.crearPedido(this.form.getRawValue()).subscribe({
      next: (r) => { this.avisos.exito(r.mensaje); this.modalAbierto.set(false); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  cambiar(p: Pedido): void {
    this.api.cambiarEstadoPedido(p.id).subscribe({ next: () => this.cargar(), error: (e) => this.avisos.errorHttp(e) });
  }

  eliminar(p: Pedido): void {
    if (!confirm(`¿Eliminar el pedido de ${p.cliente}?`)) {
      return;
    }
    this.api.eliminarPedido(p.id).subscribe({
      next: () => { this.avisos.exito('Pedido eliminado'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
