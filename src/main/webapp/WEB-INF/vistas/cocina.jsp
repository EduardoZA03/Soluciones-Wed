<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Cocina"/>
<c:set var="activo" value="cocina"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Cocina</h1>
    <p class="text-body-secondary mb-0">Pedidos activos en preparación · se actualiza cada 10 s</p>
</div>

<div class="row g-4">
    <%-- Columna: por preparar --%>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-semibold mb-0">Por preparar</h2>
                    <span class="badge rounded-pill bg-warning-subtle text-warning-emphasis">${fn:length(pendientes)}</span>
                </div>
                <c:choose>
                    <c:when test="${empty pendientes}">
                        <p class="text-center text-body-tertiary py-4 mb-0">Sin pedidos.</p>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group gap-2">
                            <c:forEach var="p" items="${pendientes}">
                                <li class="list-group-item border rounded-3 d-flex justify-content-between align-items-start gap-3">
                                    <div>
                                        <div class="fw-semibold">${fn:escapeXml(p.producto)} <span class="text-body-tertiary">× ${p.cantidad}</span></div>
                                        <div class="small text-body-secondary">Mesa ${p.mesa} · ${fn:escapeXml(p.cliente)} · Mesero: ${fn:escapeXml(p.mesero)}</div>
                                    </div>
                                    <form method="post" action="${ctx}/cocina" class="m-0">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <input type="hidden" name="estado" value="En cocina">
                                        <button type="submit" class="btn btn-sm btn-primary rounded-pill text-nowrap">
                                            <i class="bi bi-arrow-right me-1"></i>A cocina
                                        </button>
                                    </form>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <%-- Columna: en preparación --%>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-semibold mb-0">En preparación</h2>
                    <span class="badge rounded-pill bg-info-subtle text-info-emphasis">${fn:length(enCocina)}</span>
                </div>
                <c:choose>
                    <c:when test="${empty enCocina}">
                        <p class="text-center text-body-tertiary py-4 mb-0">Sin pedidos.</p>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group gap-2">
                            <c:forEach var="p" items="${enCocina}">
                                <li class="list-group-item border rounded-3 d-flex justify-content-between align-items-start gap-3">
                                    <div>
                                        <div class="fw-semibold">${fn:escapeXml(p.producto)} <span class="text-body-tertiary">× ${p.cantidad}</span></div>
                                        <div class="small text-body-secondary">Mesa ${p.mesa} · ${fn:escapeXml(p.cliente)} · Mesero: ${fn:escapeXml(p.mesero)}</div>
                                    </div>
                                    <form method="post" action="${ctx}/cocina" class="m-0">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <input type="hidden" name="estado" value="Entregado">
                                        <button type="submit" class="btn btn-sm btn-success rounded-pill text-nowrap">
                                            <i class="bi bi-check-lg me-1"></i>Entregar
                                        </button>
                                    </form>
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
