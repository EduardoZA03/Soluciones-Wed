# Historias de usuario — cómo las cumple el sistema

Correspondencia entre la Tabla 7 del documento *"Sistema web de gestión de
inventario con inteligencia artificial e interoperabilidad con proveedores:
caso restaurante La Crianza Arequipeña"* y el código (Angular + Spring Boot).

Rutas de código: backend en `backend/src/main/java/com/calsoft/…`,
frontend en `frontend/src/app/…`.

| HU | Historia | Backend (Spring Boot) | Frontend (Angular) | Cómo probarla |
|----|----------|-----------------------|--------------------|---------------|
| **HU-01** | Iniciar sesión según rol y ver solo sus funciones | `AuthController`, `seguridad/JwtService`, `seguridad/SecurityConfig`, `seguridad/Roles` | `paginas/login`, `core/auth.service.ts`, `core/guards.ts`, menú en `layout/shell` | Crear un usuario con rol *Almacén* en **Usuarios** y entrar con él: el menú cambia y la API responde 403 a lo que no le corresponde. Credenciales incorrectas muestran mensaje. |
| **HU-02** | Consultar stock actualizado, buscar y filtrar | `InsumoController`, `servicio/InventarioService` | `paginas/insumos`, `paginas/kardex` | Buscar por nombre, filtrar por categoría o estado. Registrar *Entrada*, *Merma* o *Ajuste* con el botón de movimiento y verlo en **Kardex**. |
| **HU-03** | Descontar insumos al vender en el POS, según receta y sin duplicar | `PedidoController`, `InventarioService.registrarPedido` (transacción) | `paginas/pedidos`, `paginas/carta` | Registrar "Rocoto relleno × 2": el aviso indica lo descontado y el Kardex muestra las SALIDAS `PED-n`. `pedidos.stock_descontado` evita procesar dos veces; si falta stock no se guarda nada. |
| **HU-04** | Alertas de stock mínimo y vencimiento; marcar como atendida | `servicio/AlertaService` (INSERT…SELECT con índice único parcial), `AlertaController` | `paginas/alertas`, contador en el menú | "Pollo entero" vence en 2 días → alerta. Una *Merma* que deje un insumo en su mínimo → alerta de stock. Las de stock se cierran solas al reponer. |
| **HU-05** | Predicción de reposición con IA (histórico + estacionalidad), aceptar o ajustar | `servicio/PrediccionService`, `PrediccionController` | `paginas/prediccion` | Cambiar el periodo (7/14/30 días), ajustar cantidades y pulsar **Generar órdenes de compra**. |
| **HU-06** | Generar y aprobar una orden desde una alerta, editable antes de aprobar | `servicio/OrdenService`, `OrdenController`, `AlertaController` | `paginas/alertas`, `paginas/orden` | **Generar orden** en una alerta → Borrador con la cantidad de la IA; editar cantidades/precios; solo el Administrador ve **Aprobar y enviar**. |
| **HU-07** | Enviar la orden al proveedor por API; guardar respuesta o error | `integracion/ProveedorApiCliente` (RestClient), `controlador/ProveedorSimuladoController` | `paginas/orden`, `paginas/proveedores` | Al aprobar: *Enviada* (POST JSON) → *Confirmada* con N° y fecha. Para ver un error: en **Proveedores** pon la URL `http://localhost:9/api` → la línea de tiempo registra *Error API* y aparece **Reintentar envío**. |
| **HU-08** | Recepción por QR / código de barras validando cantidades | `QrController` (ZXing), `OrdenService.recibir` | `paginas/recepcion` (html5-qrcode) | La orden confirmada muestra su QR. En **Recepción**: cámara, lectora USB o escribir el código. Si algo no coincide → *Recibida con incidencia*. |
| **HU-09** | Trazabilidad de cada orden | `OrdenCompra.eventos` (tabla `orden_eventos`) | `paginas/orden` | Línea de tiempo: Borrador → Aprobada → Enviada → Confirmada → Recibida (o errores), con usuario y hora. |

## Tablas maestras (requisito del avance T2)

| Maestra | Entidad | Repositorio | Servicio | Controlador | Pantalla |
|---------|---------|-------------|----------|-------------|----------|
| Categorías | `modelo/Categoria` | `CategoriaRepository` | `CategoriaService` | `CategoriaController` | `paginas/categorias` |
| Proveedores | `modelo/Proveedor` | `ProveedorRepository` | `ProveedorService` | `ProveedorController` | `paginas/proveedores` |
| Insumos | `modelo/Insumo` | `InsumoRepository` | `InventarioService` | `InsumoController` | `paginas/insumos` |

## Componentes de la solución (Tabla 6 del documento)

| Componente | Requisito | Implementación |
|------------|-----------|----------------|
| Registro de consumo integrado al POS | Interoperabilidad | Pedidos (POS) + recetas → movimientos SALIDA automáticos |
| Predicción de reposición | Inteligencia artificial | Suavizado exponencial con estacionalidad semanal |
| Alertas de stock | Funcional | Alertas automáticas de stock mínimo y vencimiento |
| Integración con proveedor | Interoperabilidad | API REST JSON + proveedor simulado |
| Recepción de insumos | Funcional | QR con ZXing; lectura con cámara o lectora USB |
| Dashboard de reportes | Funcional | Consumo, mermas, órdenes y proyección de compras |

## Sobre el módulo de IA

Modelo de **pronóstico de series de tiempo** que aprende del histórico de
salidas (variante simple de Holt-Winters):

1. Factor por **día de la semana** (p. ej. el sábado se consume 1.5 veces el promedio).
2. **Nivel** de consumo diario con **suavizado exponencial** (α = 0.3).
3. **Pronóstico** del periodo: nivel × factor de cada día.
4. **Sugerencia**: consumo estimado + stock mínimo − stock actual.
5. **Confianza** según el error del modelo al pronosticar el pasado.

Al instalar se cargan 8 semanas de consumo de demostración; con el uso, las
ventas del POS alimentan el histórico.
