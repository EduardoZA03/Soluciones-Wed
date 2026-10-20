import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Alerta, Mensaje, OrdenCompra, Pedido, Plato, Prediccion, Reserva, ResultadoEnvio, Usuario,
} from './modelos';

/** Llamadas a la API para los módulos de operación, abastecimiento y administración. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ---------- Inicio y reportes ----------
  inicio(): Observable<any> {
    return this.http.get('/api/inicio');
  }

  reportes(): Observable<any> {
    return this.http.get('/api/reportes');
  }

  // ---------- Carta ----------
  platos(soloActivos = false): Observable<Plato[]> {
    return this.http.get<Plato[]>('/api/platos', { params: { activos: soloActivos } });
  }

  crearPlato(p: { nombre: string; precio: number; activo: boolean }): Observable<Plato> {
    return this.http.post<Plato>('/api/platos', p);
  }

  actualizarPlato(id: number, p: { nombre: string; precio: number; activo: boolean }): Observable<Plato> {
    return this.http.put<Plato>(`/api/platos/${id}`, p);
  }

  eliminarPlato(id: number): Observable<void> {
    return this.http.delete<void>(`/api/platos/${id}`);
  }

  guardarIngrediente(platoId: number, insumoId: number, cantidad: number): Observable<Plato> {
    return this.http.post<Plato>(`/api/platos/${platoId}/receta`, { insumoId, cantidad });
  }

  quitarIngrediente(platoId: number, recetaId: number): Observable<Plato> {
    return this.http.delete<Plato>(`/api/platos/${platoId}/receta/${recetaId}`);
  }

  // ---------- POS y cocina ----------
  pedidos(soloActivos = false): Observable<Pedido[]> {
    return this.http.get<Pedido[]>('/api/pedidos', { params: { activos: soloActivos } });
  }

  crearPedido(p: object): Observable<Mensaje> {
    return this.http.post<Mensaje>('/api/pedidos', p);
  }

  /** Sin estado pasa al siguiente del ciclo. */
  cambiarEstadoPedido(id: number, estado?: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`/api/pedidos/${id}/estado`, estado ? { estado } : null);
  }

  eliminarPedido(id: number): Observable<void> {
    return this.http.delete<void>(`/api/pedidos/${id}`);
  }

  // ---------- Reservas ----------
  reservas(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>('/api/reservas');
  }

  crearReserva(r: object): Observable<Reserva> {
    return this.http.post<Reserva>('/api/reservas', r);
  }

  cambiarEstadoReserva(id: number, estado: string): Observable<Reserva> {
    return this.http.patch<Reserva>(`/api/reservas/${id}/estado`, { estado });
  }

  eliminarReserva(id: number): Observable<void> {
    return this.http.delete<void>(`/api/reservas/${id}`);
  }

  // ---------- Usuarios ----------
  usuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('/api/usuarios');
  }

  roles(): Observable<string[]> {
    return this.http.get<string[]>('/api/usuarios/roles');
  }

  crearUsuario(u: { usuario: string; clave: string; rol: string }): Observable<Usuario> {
    return this.http.post<Usuario>('/api/usuarios', u);
  }

  actualizarUsuario(id: number, cambio: { rol?: string; clave?: string }): Observable<Usuario> {
    return this.http.patch<Usuario>(`/api/usuarios/${id}`, cambio);
  }

  eliminarUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`/api/usuarios/${id}`);
  }

  // ---------- Alertas ----------
  alertas(estado: 'Pendiente' | 'Atendida'): Observable<Alerta[]> {
    return this.http.get<Alerta[]>('/api/alertas', { params: { estado } });
  }

  alertasPendientes(): Observable<{ cantidad: number }> {
    return this.http.get<{ cantidad: number }>('/api/alertas/pendientes');
  }

  atenderAlerta(id: number): Observable<void> {
    return this.http.post<void>(`/api/alertas/${id}/atender`, null);
  }

  ordenDesdeAlerta(id: number): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`/api/alertas/${id}/orden`, null);
  }

  // ---------- Predicción IA ----------
  prediccion(dias: number): Observable<Prediccion[]> {
    return this.http.get<Prediccion[]>('/api/prediccion', { params: { dias } });
  }

  ordenesDesdePrediccion(items: { insumoId: number; cantidad: number }[]): Observable<{ ids: number[] }> {
    return this.http.post<{ ids: number[] }>('/api/prediccion/ordenes', { items });
  }

  // ---------- Órdenes de compra ----------
  ordenes(estado?: string): Observable<OrdenCompra[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<OrdenCompra[]>('/api/ordenes', { params });
  }

  orden(id: number): Observable<OrdenCompra> {
    return this.http.get<OrdenCompra>(`/api/ordenes/${id}`);
  }

  ordenPorCodigo(codigo: string): Observable<OrdenCompra> {
    return this.http.get<OrdenCompra>(`/api/ordenes/codigo/${encodeURIComponent(codigo)}`);
  }

  crearOrden(proveedorId: number): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>('/api/ordenes', { proveedorId });
  }

  agregarItemOrden(id: number, insumoId: number, cantidad: number): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`/api/ordenes/${id}/items`, { insumoId, cantidad });
  }

  actualizarItemsOrden(id: number, items: { id: number; cantidad: number; precioUnitario: number }[]): Observable<OrdenCompra> {
    return this.http.put<OrdenCompra>(`/api/ordenes/${id}/items`, { items });
  }

  quitarItemOrden(id: number, itemId: number): Observable<OrdenCompra> {
    return this.http.delete<OrdenCompra>(`/api/ordenes/${id}/items/${itemId}`);
  }

  aprobarOrden(id: number): Observable<ResultadoEnvio> {
    return this.http.post<ResultadoEnvio>(`/api/ordenes/${id}/aprobar`, null);
  }

  reenviarOrden(id: number): Observable<ResultadoEnvio> {
    return this.http.post<ResultadoEnvio>(`/api/ordenes/${id}/reenviar`, null);
  }

  cancelarOrden(id: number): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`/api/ordenes/${id}/cancelar`, null);
  }

  recibirOrden(id: number, recibido: Record<number, number>, observacion: string): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`/api/ordenes/${id}/recepcion`, { recibido, observacion });
  }
}
