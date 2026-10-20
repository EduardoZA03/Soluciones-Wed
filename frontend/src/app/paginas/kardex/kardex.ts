import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { AvisosService } from '../../core/avisos.service';
import { InsumosService } from '../../core/maestros.service';
import { Insumo, Movimiento, tono } from '../../core/modelos';

/** Kardex: historial de movimientos de stock (HU-02). Filtro ?insumo=ID */
@Component({
  selector: 'app-kardex',
  imports: [DatePipe],
  templateUrl: './kardex.html',
})
export class Kardex implements OnInit {
  private readonly servicio = inject(InsumosService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly tono = tono;

  /** Parámetro de la URL (?insumo=5), enlazado por el router. */
  readonly insumo = input<string | undefined>();

  protected readonly movimientos = signal<Movimiento[]>([]);
  protected readonly insumos = signal<Insumo[]>([]);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.servicio.listar().subscribe({ next: (l) => this.insumos.set(l), error: () => {} });
    this.cargar();
  }

  filtrar(valor: string): void {
    this.router.navigate(['/kardex'], { queryParams: { insumo: valor || null } }).then(() => this.cargar());
  }

  private cargar(): void {
    const id = this.insumo() ? Number(this.insumo()) : null;
    this.servicio.kardex(id).subscribe({
      next: (m) => { this.movimientos.set(m); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }
}
