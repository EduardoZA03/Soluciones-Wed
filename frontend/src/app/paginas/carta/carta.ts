import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { InsumosService } from '../../core/maestros.service';
import { Insumo, Plato } from '../../core/modelos';

/** Carta y recetas (HU-03): cada plato descuenta del inventario los insumos de su receta. */
@Component({
  selector: 'app-carta',
  imports: [ReactiveFormsModule, Modal, DecimalPipe],
  templateUrl: './carta.html',
})
export class Carta implements OnInit {
  private readonly api = inject(ApiService);
  private readonly insumosServicio = inject(InsumosService);
  private readonly avisos = inject(AvisosService);

  protected readonly platos = signal<Plato[]>([]);
  protected readonly insumos = signal<Insumo[]>([]);
  protected readonly modalAbierto = signal(false);
  protected readonly editandoId = signal<number | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    precio: [0, [Validators.required, Validators.min(0)]],
    activo: [true],
  });

  ngOnInit(): void {
    this.cargar();
    this.insumosServicio.listar().subscribe({ next: (l) => this.insumos.set(l), error: () => {} });
  }

  cargar(): void {
    this.api.platos().subscribe({ next: (p) => this.platos.set(p), error: (e) => this.avisos.errorHttp(e) });
  }

  nuevo(): void {
    this.editandoId.set(null);
    this.form.reset({ nombre: '', precio: 0, activo: true });
    this.modalAbierto.set(true);
  }

  editar(p: Plato): void {
    this.editandoId.set(p.id);
    this.form.reset({ nombre: p.nombre, precio: p.precio, activo: p.activo });
    this.modalAbierto.set(true);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const id = this.editandoId();
    const datos = this.form.getRawValue();
    (id ? this.api.actualizarPlato(id, datos) : this.api.crearPlato(datos)).subscribe({
      next: () => { this.avisos.exito('Plato guardado'); this.modalAbierto.set(false); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  eliminar(p: Plato): void {
    if (!confirm(`¿Eliminar el plato "${p.nombre}" y su receta?`)) {
      return;
    }
    this.api.eliminarPlato(p.id).subscribe({
      next: () => { this.avisos.exito('Plato eliminado'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  agregarIngrediente(p: Plato, insumoId: string, cantidad: string): void {
    if (!insumoId || !(Number(cantidad) > 0)) {
      this.avisos.error('Elige un insumo y una cantidad mayor que cero');
      return;
    }
    this.api.guardarIngrediente(p.id, Number(insumoId), Number(cantidad)).subscribe({
      next: () => { this.avisos.exito('Receta actualizada'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  quitarIngrediente(p: Plato, recetaId: number): void {
    this.api.quitarIngrediente(p.id, recetaId).subscribe({
      next: () => this.cargar(),
      error: (e) => this.avisos.errorHttp(e),
    });
  }
}
