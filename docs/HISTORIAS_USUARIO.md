# Historias de usuario — cómo las cumple el sistema

Correspondencia entre la Tabla 7 del documento *"Sistema web de gestión de
inventario con inteligencia artificial e interoperabilidad con proveedores:
caso restaurante La Crianza Arequipeña"* y el código de CalSoft.

| HU | Historia | Dónde está | Cómo probarla |
|----|----------|------------|---------------|
| **HU-01** | Iniciar sesión según rol y ver solo sus funciones | `LoginServlet`, `filtro/AutenticacionFiltro`, `util/Permisos`, menú en `plantilla/cabecera.jspf` | Crear un usuario con rol *Almacén* en **Usuarios** y entrar con él: el menú cambia y los módulos ajenos redirigen con aviso. Credenciales incorrectas muestran mensaje. |
| **HU-02** | Consultar stock actualizado, buscar y filtrar | **Insumos** (`InventarioServlet`), **Kardex** (`MovimientoServlet`) | Buscar por nombre, filtrar por categoría o estado (Bajo stock / Por vencer / Vencido). Registrar una *Entrada*, *Merma* o *Ajuste* con el botón **Movimiento** y verla en el Kardex. |
| **HU-03** | Descontar insumos al registrar una venta en el POS, según receta y sin duplicar | **Carta y recetas** (`PlatoServlet`), **Pedidos (POS)**, `servicio/InventarioService.registrarPedido` | Registrar un pedido de "Rocoto relleno" × 2: el mensaje indica lo descontado y el Kardex muestra las SALIDAS con referencia `PED-n`. La columna `pedidos.stock_descontado` evita procesar dos veces. Si no hay stock suficiente, no se guarda nada. |
| **HU-04** | Alertas de stock mínimo y vencimiento, que se pueden marcar como atendidas | **Alertas** (`AlertaServlet`, `dao/AlertaDAO`) | El insumo "Pollo entero" vence en 2 días → aparece la alerta. Bajar un stock al mínimo con una *Merma* → alerta de stock mínimo. Botón **Atendida**. Las de stock se cierran solas al reponer. |
| **HU-05** | Predicción de reposición con IA (histórico + estacionalidad), aceptar o ajustar | **Predicción IA** (`servicio/PrediccionService`) | Ver consumo diario, estimado del periodo (7/14/30 días), cobertura, tendencia y confianza. Ajustar cantidades y pulsar **Generar órdenes de compra**. |
| **HU-06** | Generar y aprobar orden de compra desde una alerta, editable antes de aprobar | **Alertas → Generar orden**, **Órdenes de compra** (`CompraServlet`, `servicio/OrdenService`) | La orden nace en *Borrador* con la cantidad sugerida por la IA; se pueden cambiar cantidades, precios y agregar insumos. Solo el Administrador ve **Aprobar y enviar**. |
| **HU-07** | Enviar la orden al proveedor por API y guardar su respuesta; registrar error si no responde | `integracion/ProveedorApiCliente`, `api/ProveedorSimuladoServlet` | Al aprobar, la orden pasa a *Enviada* (POST JSON) y luego a *Confirmada* con N° de confirmación y fecha de entrega. Para ver el manejo de errores: en **Proveedores** pon una URL inexistente (p. ej. `http://localhost:9/api`) → la línea de tiempo registra *Error API* y aparece **Reintentar envío**. |
| **HU-08** | Recepción por QR o código de barras, validando cantidades | **Recepción** (`RecepcionServlet`, `QrServlet`, `js/recepcion.js`) | La orden confirmada muestra su QR. En Recepción: escanear con la cámara, con una pistola lectora o escribir el código. Si lo recibido no coincide → *Recibida con incidencia*. El stock se actualiza y queda en el Kardex. |
| **HU-09** | Trazabilidad de cada orden con fecha/hora y estado | Detalle de la orden (`orden.jsp`, tabla `orden_eventos`) | Línea de tiempo: Borrador → Aprobada → Enviada → Confirmada → Recibida (o errores/incidencias), con usuario y hora. |

## Componentes de la solución (Tabla 6 del documento)

| Componente | Requisito | Implementación |
|------------|-----------|----------------|
| Registro de consumo integrado al POS | Interoperabilidad | Pedidos (POS) + recetas → movimientos SALIDA automáticos |
| Predicción de reposición | Inteligencia artificial | Suavizado exponencial con estacionalidad semanal (`PrediccionService`) |
| Alertas de stock | Funcional | Alertas automáticas de stock mínimo y vencimiento |
| Integración con proveedor | Interoperabilidad | API REST JSON (`ProveedorApiCliente`) + proveedor simulado |
| Recepción de insumos | Funcional | QR generado con ZXing, lectura con html5-qrcode o lectora USB |
| Dashboard de reportes | Funcional | Reportes de consumo, mermas, órdenes y proyección de compras |

## Sobre el módulo de IA

Es un modelo de **pronóstico de series de tiempo** que aprende de los datos
(variante simple de Holt-Winters):

1. Calcula un **factor por día de la semana** (p. ej. el sábado se consume 1.5
   veces el promedio).
2. Estima el **nivel** de consumo diario con **suavizado exponencial**
   (α = 0.3, más peso a lo reciente).
3. **Pronostica** el periodo: nivel × factor de cada día.
4. **Sugiere** comprar: consumo estimado + stock mínimo − stock actual.
5. Mide la **confianza** con el error que tuvo al pronosticar el pasado.

Al instalar, se cargan 8 semanas de consumo histórico de demostración (con
picos de fin de semana) para que la predicción tenga datos. Con el uso real,
las ventas del POS van alimentando el histórico.

## Proveedor simulado (API)

`POST /CalSoft/api/proveedor/ordenes` con cabecera `X-Api-Key`.

Solicitud:
```json
{ "codigo": "OC-20261019-7KQ2XD", "comprador": "La Crianza Arequipeña",
  "items": [ { "item_id": 1, "insumo": "Papas", "unidad": "kg", "cantidad": 40 } ] }
```
Respuesta:
```json
{ "aceptada": true, "confirmacion": "PRV-482913", "fecha_entrega": "2026-10-21",
  "mensaje": "Pedido confirmado",
  "items": [ { "item_id": 1, "disponible": true, "cantidad_confirmada": 40 } ] }
```
Reglas: API key inválida → HTTP 401; cada ítem está disponible si la cantidad es
de hasta 500; entrega al día siguiente (si cae domingo, el lunes). Para conectar
un proveedor real basta con poner su URL y su API key en **Proveedores**.
