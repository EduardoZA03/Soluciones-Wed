import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { Alerta, tono } from '../../core/modelos';

/** Alertas de stock mínimo y vencimiento (HU-04); desde aquí se genera la orden (HU-06). */
@Component({
  selector: 'app-alertas',
  imports: [DatePipe, RouterLink],
  templateUrl: './alertas.html',
})
export class Alertas implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);
  protected readonly tono = tono;

  protected readonly pendientes = signal<Alerta[]>([]);
  protected readonly atendidas = signal<Alerta[]>([]);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.alertas('Pendiente').subscribe({
      next: (l) => { this.pendientes.set(l); this.cargando.set(false); },
      error: (e) => { this.avisos.errorHttp(e); this.cargando.set(false); },
    });
    this.api.alertas('Atendida').subscribe({ next: (l) => this.atendidas.set(l), error: () => {} });
  }

  atender(a: Alerta): void {
    this.api.atenderAlerta(a.id).subscribe({
      next: () => { this.avisos.exito('Alerta marcada como atendida'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  generarOrden(a: Alerta): void {
    this.api.ordenDesdeAlerta(a.id).subscribe({
      next: (o) => {
        this.avisos.exito('Orden creada en borrador con la cantidad sugerida por la IA. Revísala y apruébala.');
        this.router.navigate(['/compras', o.id]);
      },
      error: (e) => this.avisos.errorHttp(e),
    });
  }
}
