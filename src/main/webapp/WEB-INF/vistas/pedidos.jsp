<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Pedidos"/>
<c:set var="activo" value="pedidos"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Pedidos</h1>
        <p class="text-body-secondary mb-0">Registro y seguimiento de pedidos</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalPedido">
        <i class="bi bi-plus-lg me-1"></i>Nuevo pedido
    </button>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Total" valor="${total}" icono="list-check" tono="primary"/>
    <ui:stat etiqueta="Pendientes" valor="${pendientes}" icono="clock" tono="warning"/>
    <ui:stat etiqueta="En cocina" valor="${enCocina}" icono="egg-fried" tono="info"/>
    <ui:stat etiqueta="Entregados" valor="${entregados}" icono="check-circle" tono="success"/>
</div>

<%-- Filtro por mesero --%>
<div class="d-flex flex-wrap align-items-center gap-2 mb-3">
    <span class="fw-medium text-body-secondary me-1">Mesero:</span>
    <a class="btn btn-sm rounded-pill ${filtroMesero == 'Todos' ? 'btn-primary' : 'btn-outline-secondary'}" href="${ctx}/pedidos">Todos</a>
    <c:forEach var="m" items="${meseros}">
        <c:url var="urlMesero" value="/pedidos"><c:param name="mesero" value="${m}"/></c:url>
        <a class="btn btn-sm rounded-pill ${filtroMesero == m ? 'btn-primary' : 'btn-outline-secondary'}" href="${urlMesero}">${fn:escapeXml(m)}</a>
    </c:forEach>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th class="ps-4">#</th><th>Cliente</th><th>Mesero</th><th>Producto</th>
                    <th>Cant.</th><th>Mesa</th><th>Estado</th><th class="text-end pe-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${pedidos}" varStatus="i">
                    <tr>
                        <td class="ps-4 text-body-tertiary">${i.count}</td>
                        <td class="fw-medium">${fn:escapeXml(p.cliente)}</td>
                        <td>${fn:escapeXml(p.mesero)}</td>
                        <td>${fn:escapeXml(p.producto)}</td>
                        <td>${p.cantidad}</td>
                        <td>${p.mesa}</td>
                        <td><span class="badge rounded-pill bg-${p.tono}-subtle text-${p.tono}-emphasis">${p.estado}</span></td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-2">
                                <form method="post" action="${ctx}/pedidos" class="m-0">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="cambiar">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
                                    <button type="submit" class="btn btn-sm btn-outline-secondary rounded-pill">Cambiar</button>
                                </form>
                                <form method="post" action="${ctx}/pedidos" class="m-0"
                                      data-confirmar="¿Eliminar el pedido de ${fn:escapeXml(p.cliente)}?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
                                    <button type="submit" class="btn btn-sm btn-danger rounded-pill">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pedidos}">
                    <tr><td colspan="8" class="text-center text-body-secondary py-5">No hay pedidos registrados.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal: nuevo pedido --%>
<div class="modal fade" id="modalPedido" tabindex="-1" aria-labelledby="tituloPedido" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/pedidos">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloPedido">Nuevo pedido</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="cliente" class="form-label">Cliente</label>
                            <input class="form-control" id="cliente" name="cliente" placeholder="Nombre del cliente" maxlength="100" required>
                        </div>
                        <div class="col-12">
                            <label for="mesero" class="form-label">Mesero</label>
                            <input class="form-control" id="mesero" name="mesero" placeholder="Nombre del mesero" maxlength="100" required>
                        </div>
                        <div class="col-12">
                            <label for="producto" class="form-label">Producto</label>
                            <input class="form-control" id="producto" name="producto" placeholder="Producto" maxlength="100" required>
                        </div>
                        <div class="col-6">
                            <label for="cantidad" class="form-label">Cantidad</label>
                            <input class="form-control" id="cantidad" name="cantidad" type="number" min="1" placeholder="Ej. 2" required>
                        </div>
                        <div class="col-6">
                            <label for="mesa" class="form-label">Mesa</label>
                            <input class="form-control" id="mesa" name="mesa" type="number" min="1" placeholder="Ej. 4" required>
                        </div>
                        <div class="col-12">
                            <label for="estado" class="form-label">Estado</label>
                            <select class="form-select" id="estado" name="estado">
                                <c:forEach var="e" items="${estados}"><option value="${e}">${e}</option></c:forEach>
                            </select>
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
