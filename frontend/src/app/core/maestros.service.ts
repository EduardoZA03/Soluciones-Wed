import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Categoria, Insumo, InsumoRequest, Mensaje, Movimiento, Proveedor } from './modelos';

/** CRUD de la tabla maestra Categorías: /api/categorias */
@Injectable({ providedIn: 'root' })
export class CategoriasService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/categorias';

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.url);
  }

  crear(c: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.url, c);
  }

  actualizar(id: number, c: Categoria): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/${id}`, c);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

/** CRUD de la tabla maestra Proveedores: /api/proveedores */
@Injectable({ providedIn: 'root' })
export class ProveedoresService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/proveedores';

  listar(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.url);
  }

  crear(p: Proveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.url, p);
  }

  actualizar(id: number, p: Proveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.url}/${id}`, p);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

/** CRUD de la tabla maestra Insumos y sus movimientos: /api/insumos */
@Injectable({ providedIn: 'root' })
export class InsumosService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/insumos';

  listar(filtros: { q?: string; categoriaId?: number | null; estado?: string } = {}): Observable<Insumo[]> {
    let params = new HttpParams();
    if (filtros.q) params = params.set('q', filtros.q);
    if (filtros.categoriaId) params = params.set('categoriaId', filtros.categoriaId);
    if (filtros.estado) params = params.set('estado', filtros.estado);
    return this.http.get<Insumo[]>(this.url, { params });
  }

  crear(i: InsumoRequest): Observable<Insumo> {
    return this.http.post<Insumo>(this.url, i);
  }

  actualizar(id: number, i: InsumoRequest): Observable<Insumo> {
    return this.http.put<Insumo>(`${this.url}/${id}`, i);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  movimiento(id: number, tipo: string, cantidad: number, motivo: string): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${this.url}/${id}/movimientos`, { tipo, cantidad, motivo });
  }

  kardex(insumoId?: number | null): Observable<Movimiento[]> {
    const params = insumoId ? new HttpParams().set('insumoId', insumoId) : undefined;
    return this.http.get<Movimiento[]>('/api/movimientos', { params });
  }
}
