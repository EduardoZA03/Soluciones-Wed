<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Inventario"/>
<c:set var="activo" value="inventario"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Inventario</h1>
        <p>Control de productos y stock</p>
    </div>
    <button type="button" class="btn btn-primary" data-abrir="modalProducto" data-limpiar
            data-titulo="Nuevo producto" data-boton="Agregar"><i data-lucide="plus"></i> Nuevo producto</button>
</div>

<div class="pila">
    <div class="grilla-stats tres">
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Productos</span><span class="icono tono-brand"><i data-lucide="package"></i></span></div>
            <div class="valor">${fn:length(items)}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Bajo stock</span><span class="icono tono-warning"><i data-lucide="alert-triangle"></i></span></div>
            <div class="valor">${bajoStock}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Valor total</span><span class="icono tono-success"><i data-lucide="wallet"></i></span></div>
            <div class="valor">S/ ${valorTotal}</div>
        </div>
    </div>

    <div class="card tabla-envoltura">
        <table class="tabla">
            <thead>
                <tr>
                    <th>Producto</th><th>Categoría</th><th>Stock</th><th>Mínimo</th>
                    <th>Precio</th><th class="derecha">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="i" items="${items}">
                    <tr>
                        <td class="fuerte">${fn:escapeXml(i.producto)}</td>
                        <td>${fn:escapeXml(i.categoria)}</td>
                        <td>
                            ${i.stock} ${fn:escapeXml(i.unidad)}
                            <c:if test="${i.bajoStock}"><span class="badge badge-danger">bajo</span></c:if>
                        </td>
                        <td>${i.stockMinimo}</td>
                        <td>S/ ${i.precioTexto}</td>
                        <td>
                            <div class="acciones">
                                <button type="button" class="btn btn-sm btn-secondary" data-abrir="modalProducto"
                                        data-titulo="Editar producto" data-boton="Guardar cambios"
                                        data-campo-id="${i.id}"
                                        data-campo-producto="${fn:escapeXml(i.producto)}"
                                        data-campo-categoria="${fn:escapeXml(i.categoria)}"
                                        data-campo-stock="${i.stock}"
                                        data-campo-unidad="${fn:escapeXml(i.unidad)}"
                                        data-campo-stock-minimo="${i.stockMinimo}"
                                        data-campo-precio="${i.precioTexto}">Editar</button>
                                <form method="post" action="${ctx}/inventario"
                                      data-confirmar="¿Eliminar &quot;${fn:escapeXml(i.producto)}&quot;?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${i.id}">
                                    <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty items}">
                    <tr><td colspan="6" class="vacio">No hay productos.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- El mismo modal sirve para crear (sin id) y para editar (con id). --%>
<dialog class="modal" id="modalProducto">
    <div class="modal-cabecera">
        <h3 data-titulo-modal>Nuevo producto</h3>
        <button type="button" class="btn-icono" data-cerrar aria-label="Cerrar"><i data-lucide="x"></i></button>
    </div>
    <div class="modal-cuerpo">
        <form method="post" action="${ctx}/inventario" class="form-pila">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="" data-limpiable>
            <div class="campo">
                <label for="producto">Producto</label>
                <input class="input" id="producto" name="producto" placeholder="Ej. Pollo entero" maxlength="100" required>
            </div>
            <div class="campo">
                <label for="categoria">Categoría</label>
                <input class="input" id="categoria" name="categoria" placeholder="Ej. Carnes" maxlength="50">
            </div>
            <div class="dos-col">
                <div class="campo">
                    <label for="stock">Stock</label>
                    <input class="input" id="stock" name="stock" type="number" min="0" placeholder="Ej. 40" required>
                </div>
                <div class="campo">
                    <label for="unidad">Unidad</label>
                    <input class="input" id="unidad" name="unidad" value="unidad" placeholder="unidad / kg" maxlength="20">
                </div>
            </div>
            <div class="dos-col">
                <div class="campo">
                    <label for="stock_minimo">Stock mínimo</label>
                    <input class="input" id="stock_minimo" name="stock_minimo" type="number" min="0" placeholder="Ej. 10">
                </div>
                <div class="campo">
                    <label for="precio">Precio (S/)</label>
                    <input class="input" id="precio" name="precio" type="number" min="0" step="0.01" placeholder="Ej. 18.00">
                </div>
            </div>
            <div class="botones-form">
                <button type="button" class="btn btn-outline btn-full" data-cerrar>Cancelar</button>
                <button type="submit" class="btn btn-primary btn-full" data-boton-modal>Agregar</button>
            </div>
        </form>
    </div>
</dialog>

<%@ include file="plantilla/pie.jspf" %>
