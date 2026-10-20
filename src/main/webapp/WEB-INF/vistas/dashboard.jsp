<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Inicio"/>
<c:set var="activo" value="dashboard"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Inicio</h1>
    <p class="text-body-secondary mb-0">Resumen general del restaurante</p>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Pedidos activos" icono="list-check" tono="primary"
             valor="${empty stats ? '—' : stats.pedidosActivos}"
             sub="${empty stats ? '' : stats.pedidosTotal += ' en total'}"/>
    <ui:stat etiqueta="Reservas pendientes" icono="calendar-week" tono="warning"
             valor="${empty stats ? '—' : stats.reservasPendientes}"
             sub="${empty stats ? '' : stats.reservasConfirmadas += ' confirmadas'}"/>
    <ui:stat etiqueta="Alertas pendientes" icono="bell" tono="danger"
             valor="${empty stats ? '—' : stats.alertasPendientes}"
             sub="${empty stats ? '' : stats.bajoStock += ' insumos en stock mínimo'}"/>
    <ui:stat etiqueta="Órdenes en curso" icono="truck" tono="success"
             valor="${empty stats ? '—' : stats.ordenesEnCurso}"
             sub="${empty stats ? '' : 'Inventario: S/ ' += stats.valorInventarioEntero}"/>
</div>

<div class="row g-4">
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="titulo-seccion mb-0">Pedidos recientes</h2>
                    <a class="small fw-medium link-secondary text-decoration-none" href="${ctx}/pedidos">Ver todos →</a>
                </div>
                <c:choose>
                    <c:when test="${empty pedidosRecientes}">
                        <div class="bg-body-tertiary rounded-3 text-center text-body-secondary py-4">No hay pedidos.</div>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group gap-2">
                            <c:forEach var="p" items="${pedidosRecientes}">
                                <li class="list-group-item border rounded-3 d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="fw-medium">${fn:escapeXml(p.producto)} × ${p.cantidad}</div>
                                        <div class="small text-body-secondary">${fn:escapeXml(p.cliente)} · Mesa ${p.mesa}</div>
                                    </div>
                                    <span class="badge rounded-pill bg-${p.tono}-subtle text-${p.tono}-emphasis">${p.estado}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="titulo-seccion mb-0">Próximas reservas</h2>
                    <a class="small fw-medium link-secondary text-decoration-none" href="${ctx}/reservas">Ver todos →</a>
                </div>
                <c:choose>
                    <c:when test="${empty reservasProximas}">
                        <div class="bg-body-tertiary rounded-3 text-center text-body-secondary py-4">Sin reservas próximas.</div>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group gap-2">
                            <c:forEach var="r" items="${reservasProximas}">
                                <li class="list-group-item border rounded-3 d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="fw-medium">${fn:escapeXml(r.cliente)}</div>
                                        <div class="small text-body-secondary">${r.fechaTexto} · ${r.horaTexto} · ${r.personas} pers · Mesa ${r.mesa}</div>
                                    </div>
                                    <span class="badge rounded-pill bg-${r.tono}-subtle text-${r.tono}-emphasis">${r.estado}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
