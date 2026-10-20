<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Inventario"/>
<c:set var="activo" value="inventario"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Inventario</h1>
        <p class="text-body-secondary mb-0">Control de productos y stock</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalProducto"
            data-limpiar data-titulo="Nuevo producto" data-boton="Agregar">
        <i class="bi bi-plus-lg me-1"></i>Nuevo producto
    </button>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Productos" valor="${fn:length(items)}" icono="box-seam" tono="primary" columnas="col-12 col-sm-4"/>
    <ui:stat etiqueta="Bajo stock" valor="${bajoStock}" icono="exclamation-triangle" tono="warning" columnas="col-12 col-sm-4"/>
    <ui:stat etiqueta="Valor total" valor="S/ ${valorTotal}" icono="wallet2" tono="success" columnas="col-12 col-sm-4"/>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th class="ps-4">Producto</th><th>Categoría</th><th>Stock</th><th>Mínimo</th>
                    <th>Precio</th><th class="text-end pe-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="i" items="${items}">
                    <tr>
                        <td class="ps-4 fw-medium">${fn:escapeXml(i.producto)}</td>
                        <td>${fn:escapeXml(i.categoria)}</td>
                        <td class="text-nowrap">
                            ${fn:escapeXml(i.stock += " " += i.unidad)}
                            <c:if test="${i.bajoStock}"><span class="badge rounded-pill bg-danger-subtle text-danger-emphasis ms-1">bajo</span></c:if>
                        </td>
                        <td>${i.stockMinimo}</td>
                        <td class="text-nowrap">S/ ${i.precioTexto}</td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-2">
                                <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill"
                                        data-bs-toggle="modal" data-bs-target="#modalProducto"
                                        data-titulo="Editar producto" data-boton="Guardar cambios"
                                        data-campo-id="${i.id}"
                                        data-campo-producto="${fn:escapeXml(i.producto)}"
                                        data-campo-categoria="${fn:escapeXml(i.categoria)}"
                                        data-campo-stock="${i.stock}"
                                        data-campo-unidad="${fn:escapeXml(i.unidad)}"
                                        data-campo-stock-minimo="${i.stockMinimo}"
                                        data-campo-precio="${i.precioTexto}">Editar</button>
                                <form method="post" action="${ctx}/inventario" class="m-0"
                                      data-confirmar="¿Eliminar &quot;${fn:escapeXml(i.producto)}&quot;?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${i.id}">
                                    <button type="submit" class="btn btn-sm btn-danger rounded-pill">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty items}">
                    <tr><td colspan="6" class="text-center text-body-secondary py-5">No hay productos.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal: el mismo sirve para crear (sin id) y para editar (con id) --%>
<div class="modal fade" id="modalProducto" tabindex="-1" aria-labelledby="tituloProducto" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/inventario">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloProducto">Nuevo producto</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="guardar">
                    <input type="hidden" name="id" value="" data-limpiable>
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="producto" class="form-label">Producto</label>
                            <input class="form-control" id="producto" name="producto" placeholder="Ej. Pollo entero" maxlength="100" required>
                        </div>
                        <div class="col-12">
                            <label for="categoria" class="form-label">Categoría</label>
                            <input class="form-control" id="categoria" name="categoria" placeholder="Ej. Carnes" maxlength="50">
                        </div>
                        <div class="col-6">
                            <label for="stock" class="form-label">Stock</label>
                            <input class="form-control" id="stock" name="stock" type="number" min="0" placeholder="Ej. 40" required>
                        </div>
                        <div class="col-6">
                            <label for="unidad" class="form-label">Unidad</label>
                            <input class="form-control" id="unidad" name="unidad" value="unidad" placeholder="unidad / kg" maxlength="20">
                        </div>
                        <div class="col-6">
                            <label for="stock_minimo" class="form-label">Stock mínimo</label>
                            <input class="form-control" id="stock_minimo" name="stock_minimo" type="number" min="0" placeholder="Ej. 10">
                        </div>
                        <div class="col-6">
                            <label for="precio" class="form-label">Precio (S/)</label>
                            <input class="form-control" id="precio" name="precio" type="number" min="0" step="0.01" placeholder="Ej. 18.00">
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Agregar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
