import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { StatCard } from '../../compartido/stat-card';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { Conteo } from '../../core/modelos';

/** Reportes: pedidos, reservas, inventario, consumo, mermas y proyección de compras. */
@Component({
  selector: 'app-reportes',
  imports: [StatCard, DecimalPipe],
  templateUrl: './reportes.html',
})
export class Reportes implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  protected readonly datos = signal<any>(null);

  ngOnInit(): void {
    this.api.reportes().subscribe({ next: (d) => this.datos.set(d), error: (e) => this.avisos.errorHttp(e) });
  }

  /** Ancho (%) de cada barra respecto al mayor valor de la lista. */
  ancho(lista: Conteo[], c: Conteo): number {
    const max = Math.max(1, ...lista.map((x) => x.cantidad));
    return (c.cantidad * 100) / max;
  }
}
