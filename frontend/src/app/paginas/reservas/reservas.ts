import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { StatCard } from '../../compartido/stat-card';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { ESTADOS_RESERVA, Reserva, tono } from '../../core/modelos';

/** Reservas de mesa. */
@Component({
  selector: 'app-reservas',
  imports: [ReactiveFormsModule, Modal, StatCard],
  templateUrl: './reservas.html',
})
export class Reservas implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly tono = tono;
  protected readonly estados = ESTADOS_RESERVA;

  protected readonly reservas = signal<Reserva[]>([]);
  protected readonly modalAbierto = signal(false);
  protected readonly cargando = signal(true);
  protected readonly resumen = computed(() => {
    const l = this.reservas();
    return {
      total: l.length,
      pendientes: l.filter((r) => r.estado === 'Pendiente').length,
      confirmadas: l.filter((r) => r.estado === 'Confirmada').length,
      canceladas: l.filter((r) => r.estado === 'Cancelada').length,
    };
  });

  protected readonly form = this.fb.group({
    cliente: ['', Validators.required],
    telefono: [''],
    fecha: ['', Validators.required],
    hora: ['', Validators.required],
    personas: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    mesa: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.reservas().subscribe({
      next: (l) => { this.reservas.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  nuevo(): void {
    this.form.reset();
    this.modalAbierto.set(true);
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.api.crearReserva(this.form.getRawValue()).subscribe({
      next: () => { this.avisos.exito('Reserva registrada'); this.modalAbierto.set(false); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  cambiarEstado(r: Reserva, estado: string): void {
    this.api.cambiarEstadoReserva(r.id, estado).subscribe({ next: () => this.cargar(), error: (e) => this.avisos.errorHttp(e) });
  }

  eliminar(r: Reserva): void {
    if (!confirm(`¿Eliminar la reserva de ${r.cliente}?`)) {
      return;
    }
    this.api.eliminarReserva(r.id).subscribe({
      next: () => { this.avisos.exito('Reserva eliminada'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
