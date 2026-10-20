import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StatCard } from '../../compartido/stat-card';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { tono } from '../../core/modelos';

/** Inicio: resumen del restaurante. */
@Component({
  selector: 'app-inicio',
  imports: [StatCard, RouterLink, DecimalPipe],
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  protected readonly auth = inject(AuthService);
  protected readonly tono = tono;
  protected readonly datos = signal<any>(null);

  ngOnInit(): void {
    this.api.inicio().subscribe({ next: (d) => this.datos.set(d), error: (e) => this.avisos.errorHttp(e) });
  }
}
