<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Alertas"/>
<c:set var="activo" value="alertas"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Alertas</h1>
    <p class="text-body-secondary mb-0">Se generan solas cuando un insumo llega a su stock mínimo o está por vencer</p>
</div>

<h2 class="titulo-seccion mb-3">Pendientes (${fn:length(pendientes)})</h2>
<div class="card mb-4">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr><th class="ps-4">Insumo</th><th>Motivo</th><th>Detalle</th><th>Desde</th><th class="text-end pe-4">Acciones</th></tr>
            </thead>
            <tbody>
                <c:forEach var="a" items="${pendientes}">
                    <tr>
                        <td class="ps-4 fw-medium">${fn:escapeXml(a.insumo)}</td>
                        <td><span class="badge rounded-pill bg-${a.tono}-subtle text-${a.tono}-emphasis">${a.motivo}</span></td>
                        <td>${fn:escapeXml(a.mensaje)}</td>
                        <td class="text-nowrap small">${a.creadoEnTexto}</td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-2">
                                <c:choose>
                                    <c:when test="${not empty a.ordenId}">
                                        <a class="btn btn-sm btn-outline-primary rounded-pill text-nowrap" href="${ctx}/compras?id=${a.ordenId}">
                                            <i class="bi bi-cart-check me-1"></i>${a.ordenCodigo}
                                        </a>
                                    </c:when>
                                    <c:when test="${sessionScope.usuario.puede('compras')}">
                                        <form method="post" action="${ctx}/alertas" class="m-0">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="accion" value="orden">
                                            <input type="hidden" name="id" value="${a.id}">
                                            <button type="submit" class="btn btn-sm btn-primary rounded-pill text-nowrap">
                                                <i class="bi bi-cart-plus me-1"></i>Generar orden
                                            </button>
                                        </form>
                                    </c:when>
                                </c:choose>
                                <form method="post" action="${ctx}/alertas" class="m-0">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="atender">
                                    <input type="hidden" name="id" value="${a.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-secondary rounded-pill text-nowrap">
                                        <i class="bi bi-check2 me-1"></i>Atendida
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pendientes}">
                    <tr><td colspan="5" class="text-center text-body-secondary py-5"><i class="bi bi-check-circle text-success me-1"></i>No hay alertas pendientes.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<h2 class="titulo-seccion mb-3">Atendidas recientemente</h2>
<div class="card">
    <div class="table-responsive">
        <table class="table align-middle mb-0 small">
            <thead>
                <tr><th class="ps-4">Insumo</th><th>Motivo</th><th>Detalle</th><th>Creada</th><th>Atendida</th><th class="pe-4">Por</th></tr>
            </thead>
            <tbody>
                <c:forEach var="a" items="${atendidas}">
                    <tr>
                        <td class="ps-4">${fn:escapeXml(a.insumo)}</td>
                        <td>${a.motivo}</td>
                        <td>${fn:escapeXml(a.mensaje)}</td>
                        <td class="text-nowrap">${a.creadoEnTexto}</td>
                        <td class="text-nowrap">${a.atendidaEnTexto}</td>
                        <td class="pe-4">${fn:escapeXml(a.atendidaPor)}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty atendidas}">
                    <tr><td colspan="6" class="text-center text-body-secondary py-4">Sin historial.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
