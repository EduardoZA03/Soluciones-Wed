import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Modal } from '../../compartido/modal';
import { StatCard } from '../../compartido/stat-card';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { CategoriasService, InsumosService, ProveedoresService } from '../../core/maestros.service';
import { Categoria, Insumo, Proveedor, tono } from '../../core/modelos';

/** CRUD de la tabla maestra Insumos, con búsqueda, filtros y movimientos de stock (HU-02). */
@Component({
  selector: 'app-insumos',
  imports: [ReactiveFormsModule, Modal, StatCard, DecimalPipe, RouterLink],
  templateUrl: './insumos.html',
})
export class Insumos implements OnInit {
  private readonly servicio = inject(InsumosService);
  private readonly categoriasServicio = inject(CategoriasService);
  private readonly proveedoresServicio = inject(ProveedoresService);
  private readonly avisos = inject(AvisosService);
  protected readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly tono = tono;

  protected readonly lista = signal<Insumo[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly cargando = signal(true);
  protected readonly estados = ['Bajo stock', 'Por vencer', 'Vencido', 'OK'];

  // Filtros (se envían al backend)
  protected readonly q = signal('');
  protected readonly categoriaId = signal<number | null>(null);
  protected readonly estado = signal('');

  protected readonly resumen = computed(() => {
    const l = this.lista();
    return {
      total: l.length,
      bajo: l.filter((i) => i.bajoStock).length,
      vencer: l.filter((i) => i.estado === 'Por vencer' || i.estado === 'Vencido').length,
      valor: l.reduce((s, i) => s + i.stock * i.precio, 0),
    };
  });

  // Modal crear / editar
  protected readonly modalInsumo = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    categoriaId: this.fb.control<number | null>(null, Validators.required),
    stock: this.fb.control<number | null>(0, [Validators.required, Validators.min(0)]),
    unidad: ['unidad', Validators.required],
    stockMinimo: this.fb.control<number>(0, [Validators.required, Validators.min(0)]),
    precio: this.fb.control<number>(0, [Validators.required, Validators.min(0)]),
    fechaVencimiento: this.fb.control<string | null>(null),
    proveedorId: this.fb.control<number | null>(null),
  });

  // Modal movimiento
  protected readonly movimientoDe = signal<Insumo | null>(null);
  protected readonly formMov = this.fb.group({
    tipo: ['ENTRADA', Validators.required],
    cantidad: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    motivo: [''],
  });

  ngOnInit(): void {
    this.cargar();
    this.categoriasServicio.listar().subscribe({ next: (c) => this.categorias.set(c), error: () => {} });
    if (this.auth.sesion()?.rol !== 'Cocina') {
      this.proveedoresServicio.listar().subscribe({ next: (p) => this.proveedores.set(p), error: () => {} });
    }
  }

  cargar(): void {
    this.servicio.listar({ q: this.q(), categoriaId: this.categoriaId(), estado: this.estado() }).subscribe({
      next: (l) => { this.lista.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  limpiarFiltros(): void {
    this.q.set('');
    this.categoriaId.set(null);
    this.estado.set('');
    this.cargar();
  }

  /** Solo Administrador y Almacén pueden modificar insumos (Cocina solo consulta). */
  puedeEditar(): boolean {
    return this.auth.esAdministrador() || this.auth.sesion()?.rol === 'Almacén';
  }

  nuevo(): void {
    this.editandoId.set(null);
    this.form.reset({ unidad: 'unidad', stock: 0, stockMinimo: 0, precio: 0 });
    this.form.controls.stock.enable();
    this.modalInsumo.set(true);
  }

  editar(i: Insumo): void {
    this.editandoId.set(i.id);
    this.form.reset({
      nombre: i.nombre, categoriaId: i.categoria?.id ?? null, stock: i.stock, unidad: i.unidad,
      stockMinimo: i.stockMinimo, precio: i.precio, fechaVencimiento: i.fechaVencimiento,
      proveedorId: i.proveedor?.id ?? null,
    });
    this.form.controls.stock.disable(); // el stock cambia solo con movimientos (kardex)
    this.modalInsumo.set(true);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const datos = { ...this.form.getRawValue(), fechaVencimiento: this.form.value.fechaVencimiento || null };
    const id = this.editandoId();
    const peticion = id ? this.servicio.actualizar(id, datos) : this.servicio.crear(datos);
    peticion.subscribe({
      next: () => {
        this.avisos.exito(id ? 'Insumo actualizado' : 'Insumo creado');
        this.modalInsumo.set(false);
        this.cargar();
      },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  eliminar(i: Insumo): void {
    if (!confirm(`¿Eliminar el insumo "${i.nombre}"?`)) {
      return;
    }
    this.servicio.eliminar(i.id).subscribe({
      next: () => { this.avisos.exito('Insumo eliminado'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  abrirMovimiento(i: Insumo): void {
    this.formMov.reset({ tipo: 'ENTRADA', cantidad: null, motivo: '' });
    this.movimientoDe.set(i);
  }

  registrarMovimiento(): void {
    const i = this.movimientoDe();
    if (!i || this.formMov.invalid) {
      this.formMov.markAllAsTouched();
      return;
    }
    const { tipo, cantidad, motivo } = this.formMov.getRawValue();
    this.servicio.movimiento(i.id, tipo, cantidad!, motivo).subscribe({
      next: (r) => { this.avisos.exito(r.mensaje); this.movimientoDe.set(null); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
