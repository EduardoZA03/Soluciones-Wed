<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Órdenes de compra"/>
<c:set var="activo" value="compras"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Órdenes de compra</h1>
        <p class="text-body-secondary mb-0">Pedidos a proveedores enviados por API, con trazabilidad de cada paso</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalOrden">
        <i class="bi bi-plus-lg me-1"></i>Nueva orden
    </button>
</div>

<div class="d-flex flex-wrap align-items-center gap-2 mb-3">
    <span class="fw-medium text-body-secondary me-1">Estado:</span>
    <a class="btn btn-sm rounded-pill ${empty filtroEstado ? 'btn-primary' : 'btn-outline-secondary'}" href="${ctx}/compras">Todas</a>
    <c:forEach var="e" items="${estados}">
        <c:url var="urlEstado" value="/compras"><c:param name="estado" value="${e}"/></c:url>
        <a class="btn btn-sm rounded-pill ${filtroEstado == e ? 'btn-primary' : 'btn-outline-secondary'}" href="${urlEstado}">${e}</a>
    </c:forEach>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th class="ps-4">Código</th><th>Proveedor</th><th>Estado</th><th class="text-end">Ítems</th>
                    <th class="text-end">Total</th><th>Entrega</th><th>Creada</th><th class="pe-4"></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="o" items="${ordenes}">
                    <tr>
                        <td class="ps-4 fw-medium text-nowrap">${o.codigo}</td>
                        <td>${fn:escapeXml(o.proveedorNombre)}</td>
                        <td><span class="badge rounded-pill bg-${o.tono}-subtle text-${o.tono}-emphasis">${o.estado}</span></td>
                        <td class="text-end">${o.cantidadItems}</td>
                        <td class="text-end text-nowrap">S/ ${o.totalTexto}</td>
                        <td class="text-nowrap">${empty o.fechaEntrega ? '—' : o.fechaEntregaTexto}</td>
                        <td class="text-nowrap small">${o.creadoEnTexto}</td>
                        <td class="pe-4 text-end"><a class="btn btn-sm btn-outline-secondary rounded-pill" href="${ctx}/compras?id=${o.id}">Ver</a></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty ordenes}">
                    <tr><td colspan="8" class="text-center text-body-secondary py-5">No hay órdenes de compra.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="modal fade" id="modalOrden" tabindex="-1" aria-labelledby="tituloOrden" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/compras">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloOrden">Nueva orden de compra</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="nueva">
                    <label for="proveedor_id" class="form-label">Proveedor</label>
                    <select class="form-select" id="proveedor_id" name="proveedor_id" required>
                        <option value="">Elige un proveedor…</option>
                        <c:forEach var="p" items="${proveedores}">
                            <option value="${p.id}">${fn:escapeXml(p.nombre)}</option>
                        </c:forEach>
                    </select>
                    <div class="form-text">También puedes generar órdenes desde <a href="${ctx}/alertas">Alertas</a> o <a href="${ctx}/prediccion">Predicción IA</a>.</div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Crear borrador</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
