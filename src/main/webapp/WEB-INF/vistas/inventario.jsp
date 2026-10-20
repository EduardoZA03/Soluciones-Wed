<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Insumos"/>
<c:set var="activo" value="inventario"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Insumos</h1>
        <p class="text-body-secondary mb-0">Stock actual, vencimientos y movimientos de cada insumo</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalProducto"
            data-limpiar data-titulo="Nuevo insumo" data-boton="Agregar">
        <i class="bi bi-plus-lg me-1"></i>Nuevo insumo
    </button>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Insumos" valor="${totalItems}" icono="box-seam" tono="primary"/>
    <ui:stat etiqueta="Bajo stock" valor="${bajoStock}" icono="exclamation-triangle" tono="danger"/>
    <ui:stat etiqueta="Por vencer / vencidos" valor="${porVencer}" icono="hourglass-split" tono="warning"/>
    <ui:stat etiqueta="Valor total" valor="S/ ${valorTotal}" icono="wallet2" tono="success"/>
</div>

<%-- Búsqueda y filtros (HU-02) --%>
<form method="get" action="${ctx}/inventario" class="card card-body mb-3">
    <div class="row g-2 align-items-end">
        <div class="col-md-5">
            <label for="q" class="form-label small text-body-secondary mb-1">Buscar</label>
            <div class="input-group">
                <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                <input class="form-control" id="q" name="q" value="${fn:escapeXml(q)}" placeholder="Nombre del insumo">
            </div>
        </div>
        <div class="col-6 col-md-3">
            <label for="categoria" class="form-label small text-body-secondary mb-1">Categoría</label>
            <select class="form-select" id="categoria" name="categoria">
                <option value="">Todas</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${fn:escapeXml(cat)}" ${cat == filtroCategoria ? 'selected' : ''}>${fn:escapeXml(cat)}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-6 col-md-2">
            <label for="estado" class="form-label small text-body-secondary mb-1">Estado</label>
            <select class="form-select" id="estado" name="estado">
                <option value="">Todos</option>
                <c:forEach var="e" items="${estados}">
                    <option value="${e}" ${e == filtroEstado ? 'selected' : ''}>${e}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2 d-flex gap-2">
            <button type="submit" class="btn btn-primary flex-grow-1">Filtrar</button>
            <a class="btn btn-outline-secondary" href="${ctx}/inventario" title="Limpiar filtros"><i class="bi bi-x-lg"></i></a>
        </div>
    </div>
</form>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th class="ps-4">Insumo</th><th>Categoría</th><th>Stock</th><th>Mínimo</th>
                    <th>Vence</th><th>Proveedor</th><th>Precio</th><th class="text-end pe-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="i" items="${items}">
                    <tr>
                        <td class="ps-4 fw-medium">${fn:escapeXml(i.producto)}</td>
                        <td>${fn:escapeXml(i.categoria)}</td>
                        <td class="text-nowrap">
                            ${fn:escapeXml(i.stockTexto += ' ' += i.unidad)}
                            <span class="badge rounded-pill bg-${i.tonoEstado}-subtle text-${i.tonoEstado}-emphasis ms-1">${i.estado}</span>
                        </td>
                        <td>${i.stockMinimoTexto}</td>
                        <td class="text-nowrap">${empty i.fechaVencimiento ? '—' : i.fechaVencimientoTexto}</td>
                        <td class="small">${empty i.proveedorNombre ? '—' : fn:escapeXml(i.proveedorNombre)}</td>
                        <td class="text-nowrap">S/ ${i.precioTexto}</td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-1">
                                <button type="button" class="btn btn-sm btn-primary rounded-pill text-nowrap"
                                        data-bs-toggle="modal" data-bs-target="#modalMovimiento" data-limpiar
                                        data-titulo="Movimiento: ${fn:escapeXml(i.producto)} (stock ${i.stockTexto} ${fn:escapeXml(i.unidad)})"
                                        data-campo-id="${i.id}">
                                    <i class="bi bi-arrow-left-right"></i> Movimiento
                                </button>
                                <c:if test="${sessionScope.usuario.puede('movimientos')}">
                                    <a class="btn btn-sm btn-outline-secondary rounded-pill" href="${ctx}/movimientos?insumo=${i.id}" title="Ver kardex">
                                        <i class="bi bi-clock-history"></i>
                                    </a>
                                </c:if>
                                <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill"
                                        data-bs-toggle="modal" data-bs-target="#modalProducto" title="Editar"
                                        data-titulo="Editar insumo" data-boton="Guardar cambios"
                                        data-campo-id="${i.id}"
                                        data-campo-producto="${fn:escapeXml(i.producto)}"
                                        data-campo-categoria="${fn:escapeXml(i.categoria)}"
                                        data-campo-unidad="${fn:escapeXml(i.unidad)}"
                                        data-campo-stock-minimo="${i.stockMinimoTexto}"
                                        data-campo-precio="${i.precioTexto}"
                                        data-campo-fecha-vencimiento="${i.fechaVencimientoTexto}"
                                        data-campo-proveedor-id="${i.proveedorId}">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <form method="post" action="${ctx}/inventario" class="m-0"
                                      data-confirmar="¿Eliminar &quot;${fn:escapeXml(i.producto)}&quot;?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${i.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill" title="Eliminar"><i class="bi bi-trash"></i></button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty items}">
                    <tr><td colspan="8" class="text-center text-body-secondary py-5">No hay insumos que coincidan.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal: crear (sin id) o editar (con id) un insumo --%>
<div class="modal fade" id="modalProducto" tabindex="-1" aria-labelledby="tituloProducto" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/inventario">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloProducto">Nuevo insumo</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="guardar">
                    <input type="hidden" name="id" value="" data-limpiable>
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="producto" class="form-label">Insumo</label>
                            <input class="form-control" id="producto" name="producto" placeholder="Ej. Pollo entero" maxlength="100" required>
                        </div>
                        <div class="col-6">
                            <label for="m_categoria" class="form-label">Categoría</label>
                            <input class="form-control" id="m_categoria" name="categoria" placeholder="Ej. Carnes" maxlength="50">
                        </div>
                        <div class="col-6">
                            <label for="unidad" class="form-label">Unidad</label>
                            <input class="form-control" id="unidad" name="unidad" value="unidad" placeholder="unidad / kg / l" maxlength="20">
                        </div>
                        <div class="col-6 solo-nuevo">
                            <label for="stock" class="form-label">Stock inicial</label>
                            <input class="form-control" id="stock" name="stock" type="number" min="0" step="0.001" placeholder="Ej. 40" required>
                        </div>
                        <div class="col-6">
                            <label for="stock_minimo" class="form-label">Stock mínimo</label>
                            <input class="form-control" id="stock_minimo" name="stock_minimo" type="number" min="0" step="0.001" placeholder="Ej. 10">
                        </div>
                        <div class="col-6">
                            <label for="precio" class="form-label">Precio unitario (S/)</label>
                            <input class="form-control" id="precio" name="precio" type="number" min="0" step="0.01" placeholder="Ej. 18.00">
                        </div>
                        <div class="col-6">
                            <label for="fecha_vencimiento" class="form-label">Vencimiento</label>
                            <input class="form-control" id="fecha_vencimiento" name="fecha_vencimiento" type="date">
                        </div>
                        <div class="col-12">
                            <label for="proveedor_id" class="form-label">Proveedor habitual</label>
                            <select class="form-select" id="proveedor_id" name="proveedor_id">
                                <option value="">— Ninguno —</option>
                                <c:forEach var="p" items="${proveedores}">
                                    <option value="${p.id}">${fn:escapeXml(p.nombre)}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-12 solo-edicion small text-body-secondary">
                            <i class="bi bi-info-circle me-1"></i>El stock se cambia con el botón <b>Movimiento</b>, para que quede en el kardex.
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

<%-- Modal: movimiento de stock (entrada, merma o ajuste por conteo) --%>
<div class="modal fade" id="modalMovimiento" tabindex="-1" aria-labelledby="tituloMovimiento" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/inventario">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloMovimiento">Movimiento</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="movimiento">
                    <input type="hidden" name="id" value="">
                    <div class="row g-3">
                        <div class="col-6">
                            <label for="tipo" class="form-label">Tipo</label>
                            <select class="form-select" id="tipo" name="tipo">
                                <option value="ENTRADA">Entrada (compra)</option>
                                <option value="MERMA">Merma (dañado / vencido)</option>
                                <option value="AJUSTE">Ajuste (conteo físico)</option>
                            </select>
                        </div>
                        <div class="col-6">
                            <label for="cantidad" class="form-label">Cantidad</label>
                            <input class="form-control" id="cantidad" name="cantidad" type="number" min="0" step="0.001" required>
                        </div>
                        <div class="col-12">
                            <label for="motivo" class="form-label">Motivo</label>
                            <input class="form-control" id="motivo" name="motivo" maxlength="200" placeholder="Opcional">
                        </div>
                        <div class="col-12 small text-body-secondary">
                            En <b>Ajuste</b>, la cantidad es el stock real contado: el sistema registra la diferencia.
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Registrar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
