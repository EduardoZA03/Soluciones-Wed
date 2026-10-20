import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

interface ItemMenu {
  ruta: string;
  modulo: string;
  texto: string;
  icono: string;
}

interface GrupoMenu {
  titulo: string | null;
  items: ItemMenu[];
}

/** Marco de la aplicación: menú lateral (filtrado por rol) y contenido. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
})
export class Shell implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  protected readonly menuAbierto = signal(false);
  protected readonly alertasPendientes = signal(0);

  private readonly todos: GrupoMenu[] = [
    { titulo: null, items: [{ ruta: '/inicio', modulo: 'dashboard', texto: 'Inicio', icono: 'grid-1x2' }] },
    {
      titulo: 'Operación', items: [
        { ruta: '/pedidos', modulo: 'pedidos', texto: 'Pedidos (POS)', icono: 'clipboard-check' },
        { ruta: '/cocina', modulo: 'cocina', texto: 'Cocina', icono: 'egg-fried' },
        { ruta: '/reservas', modulo: 'reservas', texto: 'Reservas', icono: 'calendar-event' },
      ],
    },
    {
      titulo: 'Tablas maestras', items: [
        { ruta: '/insumos', modulo: 'insumos', texto: 'Insumos', icono: 'box-seam' },
        { ruta: '/categorias', modulo: 'categorias', texto: 'Categorías', icono: 'tags' },
        { ruta: '/proveedores', modulo: 'proveedores', texto: 'Proveedores', icono: 'truck' },
      ],
    },
    {
      titulo: 'Inventario', items: [
        { ruta: '/carta', modulo: 'carta', texto: 'Carta y recetas', icono: 'journal-text' },
        { ruta: '/alertas', modulo: 'alertas', texto: 'Alertas', icono: 'bell' },
        { ruta: '/prediccion', modulo: 'prediccion', texto: 'Predicción IA', icono: 'stars' },
        { ruta: '/kardex', modulo: 'kardex', texto: 'Kardex', icono: 'arrow-left-right' },
      ],
    },
    {
      titulo: 'Abastecimiento', items: [
        { ruta: '/compras', modulo: 'compras', texto: 'Órdenes de compra', icono: 'cart-check' },
        { ruta: '/recepcion', modulo: 'recepcion', texto: 'Recepción', icono: 'qr-code-scan' },
      ],
    },
    {
      titulo: 'Administración', items: [
        { ruta: '/usuarios', modulo: 'usuarios', texto: 'Usuarios', icono: 'people' },
        { ruta: '/reportes', modulo: 'reportes', texto: 'Reportes', icono: 'bar-chart-line' },
      ],
    },
  ];

  /** Solo los grupos e ítems que el rol puede ver (HU-01). */
  protected readonly menu = computed(() =>
    this.todos
      .map((g) => ({ ...g, items: g.items.filter((i) => this.auth.puede(i.modulo)) }))
      .filter((g) => g.items.length > 0),
  );

  protected readonly inicial = computed(() => (this.auth.sesion()?.usuario ?? '?').charAt(0).toUpperCase());

  ngOnInit(): void {
    this.actualizarAlertas();
    // Al cambiar de página: cierra el menú móvil y refresca el contador de alertas.
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.menuAbierto.set(false);
      this.actualizarAlertas();
    });
  }

  private actualizarAlertas(): void {
    if (this.auth.puede('alertas')) {
      this.api.alertasPendientes().subscribe({ next: (r) => this.alertasPendientes.set(r.cantidad), error: () => {} });
    }
  }
}
