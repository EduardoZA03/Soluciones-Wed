# API REST — CalSoft (Spring Boot)

Base: `http://localhost:8081/api` · Formato: JSON · Autenticación: `Authorization: Bearer <token>`

Los errores responden `{"mensaje": "..."}` con 400 (datos inválidos o regla de
negocio), 401 (sin token o vencido), 403 (rol sin permiso), 404 (no existe) o
409 (duplicado / en uso).

## Autenticación

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/login` | `{usuario, clave}` → `{token, id, usuario, rol, modulos[]}` |
| GET | `/auth/yo` | Datos del usuario del token |

## Tablas maestras (CRUD)

### Categorías — Administrador, Almacén (lectura también Cocina)

| Método | Ruta | Cuerpo / descripción |
|--------|------|----------------------|
| GET | `/categorias` | Lista ordenada por nombre |
| GET | `/categorias/{id}` | Una categoría |
| POST | `/categorias` | `{nombre, descripcion}` → 201 |
| PUT | `/categorias/{id}` | `{nombre, descripcion}` |
| DELETE | `/categorias/{id}` | 204 · 400 si algún insumo la usa |

### Proveedores — Administrador (lectura también Almacén)

| Método | Ruta | Cuerpo / descripción |
|--------|------|----------------------|
| GET | `/proveedores` | Lista (la API key llega oculta: `apiKeyOculta`) |
| GET | `/proveedores/{id}` | Uno |
| POST | `/proveedores` | `{nombre, ruc, telefono, email, apiUrl, apiKey}` → 201 |
| PUT | `/proveedores/{id}` | Igual; `apiKey` vacía = conservar la actual |
| DELETE | `/proveedores/{id}` | 204 · 400 si tiene órdenes |

### Insumos — Administrador, Almacén (lectura también Cocina)

| Método | Ruta | Cuerpo / descripción |
|--------|------|----------------------|
| GET | `/insumos?q=&categoriaId=&estado=` | Búsqueda y filtros (`estado`: OK, Bajo stock, Por vencer, Vencido) |
| GET | `/insumos/{id}` | Uno |
| POST | `/insumos` | `{nombre, categoriaId, stock, unidad, stockMinimo, precio, fechaVencimiento, proveedorId}` → 201 |
| PUT | `/insumos/{id}` | Igual (el stock no cambia aquí) |
| DELETE | `/insumos/{id}` | 204 · 400 si está en órdenes |
| POST | `/insumos/{id}/movimientos` | `{tipo: ENTRADA\|MERMA\|AJUSTE, cantidad, motivo}` |
| GET | `/movimientos?insumoId=` | Kardex (últimos 300) |

## Operación

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| GET | `/inicio` | todos | Resumen de la pantalla de inicio |
| GET | `/platos?activos=` | Admin, Cocina, Mesero, Empleado | Carta con recetas |
| POST / PUT / DELETE | `/platos`, `/platos/{id}` | Admin, Cocina | CRUD de platos |
| POST | `/platos/{id}/receta` | Admin, Cocina | `{insumoId, cantidad}` agrega o cambia un ingrediente |
| DELETE | `/platos/{id}/receta/{recetaId}` | Admin, Cocina | Quita un ingrediente |
| GET | `/pedidos?activos=` | Admin, Mesero, Empleado, Cocina | Pedidos |
| POST | `/pedidos` | Admin, Mesero, Empleado | `{cliente, mesero, platoId, cantidad, mesa}` · descuenta insumos por receta |
| PATCH | `/pedidos/{id}/estado` | + Cocina | Sin cuerpo: siguiente estado; `{estado}`: lo fija |
| DELETE | `/pedidos/{id}` | Admin, Mesero, Empleado | |
| GET / POST | `/reservas` | Admin, Mesero, Empleado | |
| PATCH | `/reservas/{id}/estado` | | `{estado}` |
| DELETE | `/reservas/{id}` | | |

## Inventario con IA e interoperabilidad

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| GET | `/alertas?estado=Pendiente\|Atendida` | Admin, Almacén | Alertas (evalúa stock y vencimientos) |
| GET | `/alertas/pendientes` | Admin, Almacén | `{cantidad}` para el menú |
| POST | `/alertas/{id}/atender` | Admin, Almacén | Marca como atendida |
| POST | `/alertas/{id}/orden` | Admin, Almacén | Crea orden en Borrador con la cantidad sugerida por la IA |
| GET | `/prediccion?dias=7\|14\|30` | Admin | Predicción por insumo |
| POST | `/prediccion/ordenes` | Admin | `{items:[{insumoId, cantidad}]}` → `{ids}` (una orden por proveedor) |
| GET | `/ordenes?estado=` | Admin, Almacén | Órdenes de compra |
| GET | `/ordenes/{id}` · `/ordenes/codigo/{codigo}` | Admin, Almacén | Detalle con ítems y línea de tiempo |
| POST | `/ordenes` | Admin, Almacén | `{proveedorId}` → Borrador |
| POST / PUT | `/ordenes/{id}/items` | Admin, Almacén | Agregar `{insumoId, cantidad}` / editar `{items:[{id, cantidad, precioUnitario}]}` |
| DELETE | `/ordenes/{id}/items/{itemId}` | Admin, Almacén | |
| POST | `/ordenes/{id}/aprobar` | **solo Admin** | Aprueba y envía al proveedor → `{exito, mensaje}` |
| POST | `/ordenes/{id}/reenviar` | Admin, Almacén | Reintenta si la API falló |
| POST | `/ordenes/{id}/cancelar` | Admin, Almacén | |
| POST | `/ordenes/{id}/recepcion` | Admin, Almacén | `{recibido: {itemId: cantidad}, observacion}` |
| GET | `/qr?texto=` | pública | Imagen PNG del QR |

## Administración

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET / POST | `/usuarios` | Lista / crea `{usuario, clave, rol}` |
| GET | `/usuarios/roles` | Roles válidos |
| PATCH | `/usuarios/{id}` | `{rol}` y/o `{clave}` |
| DELETE | `/usuarios/{id}` | No permite borrarse a sí mismo |
| GET | `/reportes` | Indicadores, consumo, mermas, proyección de compras |

## Proveedor simulado (pública, con API key)

`POST /api/proveedor-simulado/ordenes` — cabecera `X-Api-Key`.

```json
// Solicitud que envía CalSoft
{ "codigo": "OC-20261019-7KQ2XD", "comprador": "La Crianza Arequipeña",
  "items": [ { "item_id": 1, "insumo": "Papas", "unidad": "kg", "cantidad": 40 } ] }

// Respuesta del proveedor
{ "aceptada": true, "confirmacion": "PRV-482913", "fecha_entrega": "2026-10-21",
  "mensaje": "Pedido confirmado",
  "items": [ { "item_id": 1, "disponible": true, "cantidad_confirmada": 40 } ] }
```

Reglas: API key inválida → 401; un ítem está disponible si pide hasta 500;
entrega al día siguiente (si cae domingo, el lunes).
