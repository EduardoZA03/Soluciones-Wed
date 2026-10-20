import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { StatCard } from '../../compartido/stat-card';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { Prediccion as PrediccionInsumo, tono } from '../../core/modelos';

interface Fila extends PrediccionInsumo {
  incluir: boolean;
  comprar: number;
}

/** Predicción de reposición con IA (HU-05): aceptar o ajustar la cantidad sugerida. */
@Component({
  selector: 'app-prediccion',
  imports: [StatCard, DecimalPipe],
  templateUrl: './prediccion.html',
})
export class Prediccion implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly tono = tono;

  protected readonly horizontes = [7, 14, 30];
  protected readonly dias = signal(7);
  protected readonly filas = signal<Fila[]>([]);
  protected readonly cargando = signal(true);

  protected readonly seleccion = computed(() => this.filas().filter((f) => f.incluir && f.comprar > 0));
  protected readonly costo = computed(() =>
    this.seleccion().reduce((s, f) => s + f.comprar * f.insumo.precio, 0));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.prediccion(this.dias()).subscribe({
      next: (l) => {
        this.filas.set(l.map((p) => ({ ...p, incluir: p.conviene, comprar: p.sugerido })));
        this.cargando.set(false);
      },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
  }

  cambiarPeriodo(valor: string): void {
    this.dias.set(Number(valor));
    this.cargar();
  }

  marcar(f: Fila, incluir: boolean): void {
    this.filas.update((l) => l.map((x) => (x === f ? { ...x, incluir } : x)));
  }

  ajustar(f: Fila, valor: string): void {
    this.filas.update((l) => l.map((x) => (x === f ? { ...x, comprar: Number(valor) || 0 } : x)));
  }

  generarOrdenes(): void {
    const items = this.seleccion().map((f) => ({ insumoId: f.insumo.id, cantidad: f.comprar }));
    if (items.length === 0) {
      this.avisos.error('Marca al menos un insumo con cantidad mayor que cero');
      return;
    }
    this.api.ordenesDesdePrediccion(items).subscribe({
      next: (r) => {
        this.avisos.exito(r.ids.length === 1
          ? 'Se creó 1 orden de compra en borrador.'
          : `Se crearon ${r.ids.length} órdenes de compra en borrador (una por proveedor).`);
        this.router.navigate(r.ids.length === 1 ? ['/compras', r.ids[0]] : ['/compras']);
      },
      error: (e) => this.avisos.errorHttp(e),
    });
  }
}
