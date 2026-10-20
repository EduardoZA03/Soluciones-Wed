import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { AvisosService } from '../../core/avisos.service';
import { ProveedoresService } from '../../core/maestros.service';
import { Proveedor } from '../../core/modelos';

/** CRUD de la tabla maestra Proveedores (con su conexión por API). */
@Component({
  selector: 'app-proveedores',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './proveedores.html',
})
export class Proveedores implements OnInit {
  private readonly servicio = inject(ProveedoresService);
  private readonly avisos = inject(AvisosService);

  protected readonly lista = signal<Proveedor[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(true);
  protected readonly modalAbierto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly guardando = signal(false);

  protected readonly visibles = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    return this.lista().filter((p) => !q || p.nombre.toLowerCase().includes(q) || (p.ruc ?? '').includes(q));
  });

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    ruc: ['', Validators.pattern(/^\d{11}$/)],
    telefono: ['', Validators.maxLength(30)],
    email: ['', [Validators.email, Validators.maxLength(100)]],
    apiUrl: ['', Validators.pattern(/^https?:\/\/.+/)],
    apiKey: ['', Validators.maxLength(100)],
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

  editar(p: Proveedor): void {
    this.editandoId.set(p.id!);
    this.form.setValue({
      nombre: p.nombre, ruc: p.ruc ?? '', telefono: p.telefono ?? '', email: p.email ?? '',
      apiUrl: p.apiUrl ?? '', apiKey: '',
    });
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
        this.avisos.exito(id ? 'Proveedor actualizado' : 'Proveedor creado');
        this.modalAbierto.set(false);
        this.guardando.set(false);
        this.cargar();
      },
      error: (e) => { this.avisos.errorHttp(e); this.guardando.set(false); },
    });
  }

  eliminar(p: Proveedor): void {
    if (!confirm(`¿Eliminar al proveedor "${p.nombre}"?`)) {
      return;
    }
    this.servicio.eliminar(p.id!).subscribe({
      next: () => { this.avisos.exito('Proveedor eliminado'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
