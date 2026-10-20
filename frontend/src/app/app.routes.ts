import { Routes } from '@angular/router';
import { invitadoGuard, moduloGuard, sesionGuard } from './core/guards';
import { Shell } from './layout/shell';
import { Login } from './paginas/login/login';

/**
 * Rutas de la aplicación. Cada página se carga solo cuando se visita (lazy loading)
 * y "moduloGuard" verifica que el rol pueda usarla (HU-01).
 */
export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard] },
  {
    path: '',
    component: Shell,
    canActivate: [sesionGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', loadComponent: () => import('./paginas/inicio/inicio').then((m) => m.Inicio) },

      // Tablas maestras
      { path: 'categorias', data: { modulo: 'categorias' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/categorias/categorias').then((m) => m.Categorias) },
      { path: 'proveedores', data: { modulo: 'proveedores' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/proveedores/proveedores').then((m) => m.Proveedores) },
      { path: 'insumos', data: { modulo: 'insumos' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/insumos/insumos').then((m) => m.Insumos) },

      // Operación
      { path: 'pedidos', data: { modulo: 'pedidos' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/pedidos/pedidos').then((m) => m.Pedidos) },
      { path: 'cocina', data: { modulo: 'cocina' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/cocina/cocina').then((m) => m.Cocina) },
      { path: 'reservas', data: { modulo: 'reservas' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/reservas/reservas').then((m) => m.Reservas) },

      // Inventario con IA
      { path: 'carta', data: { modulo: 'carta' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/carta/carta').then((m) => m.Carta) },
      { path: 'alertas', data: { modulo: 'alertas' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/alertas/alertas').then((m) => m.Alertas) },
      { path: 'prediccion', data: { modulo: 'prediccion' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/prediccion/prediccion').then((m) => m.Prediccion) },
      { path: 'kardex', data: { modulo: 'kardex' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/kardex/kardex').then((m) => m.Kardex) },

      // Abastecimiento
      { path: 'compras', data: { modulo: 'compras' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/compras/compras').then((m) => m.Compras) },
      { path: 'compras/:id', data: { modulo: 'compras' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/orden/orden').then((m) => m.Orden) },
      { path: 'recepcion', data: { modulo: 'recepcion' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/recepcion/recepcion').then((m) => m.Recepcion) },

      // Administración
      { path: 'usuarios', data: { modulo: 'usuarios' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/usuarios/usuarios').then((m) => m.Usuarios) },
      { path: 'reportes', data: { modulo: 'reportes' }, canActivate: [moduloGuard],
        loadComponent: () => import('./paginas/reportes/reportes').then((m) => m.Reportes) },
    ],
  },
  { path: '**', redirectTo: '' },
];
