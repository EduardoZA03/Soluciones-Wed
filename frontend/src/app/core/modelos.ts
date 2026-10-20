// Tipos de los datos que devuelve la API REST del backend (Spring Boot).

export interface Sesion {
  token: string;
  id: number;
  usuario: string;
  rol: string;
  modulos: string[];
}

// ---------- Tablas maestras ----------
export interface Categoria {
  id?: number;
  nombre: string;
  descripcion?: string | null;
}

export interface Proveedor {
  id?: number;
  nombre: string;
  ruc?: string | null;
  telefono?: string | null;
  email?: string | null;
  apiUrl?: string | null;
  apiKey?: string | null;      // solo se envía, nunca llega
  apiKeyOculta?: string;
  simulado?: boolean;
}

export interface Insumo {
  id: number;
  nombre: string;
  categoria: Categoria | null;
  stock: number;
  unidad: string;
  stockMinimo: number;
  precio: number;
  fechaVencimiento: string | null;
  proveedor: Proveedor | null;
  estado: 'OK' | 'Bajo stock' | 'Por vencer' | 'Vencido';
  bajoStock: boolean;
}

export interface InsumoRequest {
  nombre: string;
  categoriaId: number | null;
  stock?: number | null;
  unidad: string;
  stockMinimo: number;
  precio: number;
  fechaVencimiento: string | null;
  proveedorId: number | null;
}

export interface Movimiento {
  id: number;
  insumo: Insumo;
  tipo: 'ENTRADA' | 'SALIDA' | 'MERMA' | 'AJUSTE';
  cantidad: number;
  stockResultante: number | null;
  motivo: string | null;
  referencia: string | null;
  usuario: string | null;
  creadoEn: string;
}

// ---------- Carta y POS ----------
export interface RecetaItem {
  id: number;
  insumo: Insumo;
  cantidad: number;
}

export interface Plato {
  id: number;
  nombre: string;
  precio: number;
  activo: boolean;
  receta: RecetaItem[];
}

export interface Pedido {
  id: number;
  cliente: string;
  mesero: string;
  producto: string;
  cantidad: number;
  mesa: number;
  estado: 'Pendiente' | 'En cocina' | 'Entregado';
  platoId: number | null;
  stockDescontado: boolean;
  creadoEn: string;
}

export interface Reserva {
  id: number;
  cliente: string;
  telefono: string | null;
  fecha: string;
  hora: string;
  personas: number;
  mesa: number;
  estado: 'Pendiente' | 'Confirmada' | 'Cancelada';
}

export interface Usuario {
  id: number;
  usuario: string;
  rol: string;
  creadoEn: string;
}

// ---------- Inventario con IA e interoperabilidad ----------
export interface Alerta {
  id: number;
  insumoId: number;
  insumo: string;
  tipo: 'STOCK_MINIMO' | 'VENCIMIENTO';
  motivo: string;
  mensaje: string;
  estado: 'Pendiente' | 'Atendida';
  creadoEn: string;
  atendidaEn: string | null;
  atendidaPor: string | null;
  ordenId: number | null;
  ordenCodigo: string | null;
}

export interface Prediccion {
  insumo: Insumo;
  horizonteDias: number;
  diasConDatos: number;
  consumoDiario: number;
  consumoEstimado: number;
  sugerido: number;
  diasCobertura: number | null;
  confianza: 'Alta' | 'Media' | 'Baja';
  tendencia: string;
  costoSugerido: number;
  conviene: boolean;
}

export interface OrdenItem {
  id: number;
  insumo: Insumo;
  cantidad: number;
  precioUnitario: number;
  disponible: boolean | null;
  cantidadRecibida: number | null;
  subtotal: number;
}

export interface OrdenEvento {
  id: number;
  estado: string;
  detalle: string | null;
  usuario: string | null;
  creadoEn: string;
}

export interface OrdenCompra {
  id: number;
  codigo: string;
  proveedor: Proveedor;
  estado: string;
  alertaId: number | null;
  fechaEntrega: string | null;
  confirmacionProveedor: string | null;
  creadoPor: string;
  creadoEn: string;
  items: OrdenItem[];
  eventos: OrdenEvento[];
  total: number;
  editable: boolean;
  reenviable: boolean;
  recibible: boolean;
  cancelable: boolean;
}

export interface ResultadoEnvio {
  exito: boolean;
  mensaje: string;
}

export interface Conteo {
  nombre: string;
  cantidad: number;
}

export interface Mensaje {
  mensaje: string;
}

// ---------- Colores de Bootstrap según el estado ----------
export const ESTADOS_PEDIDO = ['Pendiente', 'En cocina', 'Entregado'] as const;
export const ESTADOS_RESERVA = ['Pendiente', 'Confirmada', 'Cancelada'] as const;
export const ESTADOS_ORDEN = ['Borrador', 'Aprobada', 'Enviada', 'Confirmada', 'Rechazada',
  'Recibida', 'Recibida con incidencia', 'Cancelada'] as const;

export function tono(estado: string | null | undefined): string {
  switch (estado) {
    case 'Entregado': case 'Confirmada': case 'Recibida': case 'OK': case 'ENTRADA': case 'Alta':
      return 'success';
    case 'En cocina': case 'Aprobada': case 'Enviada':
      return 'info';
    case 'Cancelada': case 'AJUSTE': case 'Baja':
      return 'secondary';
    case 'Rechazada': case 'Recibida con incidencia': case 'Bajo stock': case 'Vencido': case 'MERMA':
    case 'STOCK_MINIMO':
      return 'danger';
    case 'SALIDA':
      return 'primary';
    default:
      return 'warning';  // Pendiente, Borrador, Por vencer, VENCIMIENTO, Media
  }
}
