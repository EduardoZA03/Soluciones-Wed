import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { AvisosService } from '../../core/avisos.service';
import { CategoriasService } from '../../core/maestros.service';
import { Categoria } from '../../core/modelos';

/** CRUD de la tabla maestra Categorías. */
@Component({
  selector: 'app-categorias',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './categorias.html',
})
export class Categorias implements OnInit {
  private readonly servicio = inject(CategoriasService);
  private readonly avisos = inject(AvisosService);

  protected readonly lista = signal<Categoria[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(true);
  protected readonly modalAbierto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly guardando = signal(false);

  protected readonly visibles = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    return this.lista().filter((c) => !q || c.nombre.toLowerCase().includes(q)
      || (c.descripcion ?? '').toLowerCase().includes(q));
  });

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
    descripcion: ['', Validators.maxLength(200)],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.servicio.listar().subscribe({
      next: (l) => { this.lista.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  nuevo(): void {
    this.editandoId.set(null);
    this.form.reset();
    this.modalAbierto.set(true);
  }

  editar(c: Categoria): void {
    this.editandoId.set(c.id!);
    this.form.setValue({ nombre: c.nombre, descripcion: c.descripcion ?? '' });
    this.modalAbierto.set(true);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    const datos = this.form.getRawValue();
    const id = this.editandoId();
    const peticion = id ? this.servicio.actualizar(id, datos) : this.servicio.crear(datos);
    peticion.subscribe({
      next: () => {
        this.avisos.exito(id ? 'Categoría actualizada' : 'Categoría creada');
        this.modalAbierto.set(false);
        this.guardando.set(false);
        this.cargar();
      },
      error: (e) => { this.avisos.errorHttp(e); this.guardando.set(false); },
    });
  }

  eliminar(c: Categoria): void {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) {
      return;
    }
    this.servicio.eliminar(c.id!).subscribe({
      next: () => { this.avisos.exito('Categoría eliminada'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  invalido(campo: 'nombre' | 'descripcion'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
