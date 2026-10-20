<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Reportes"/>
<c:set var="activo" value="reportes"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Reportes</h1>
    <p class="text-body-secondary mb-0">Estadísticas del negocio</p>
</div>

<c:if test="${not empty data}">
    <h2 class="titulo-seccion mb-3">Pedidos</h2>
    <div class="row g-3 mb-4">
        <ui:stat etiqueta="Total" valor="${data.pedidosTotal}" icono="list-check" tono="primary"/>
        <ui:stat etiqueta="Pendientes" valor="${data.pedidosPendientes}" icono="clock" tono="warning"/>
        <ui:stat etiqueta="En cocina" valor="${data.pedidosEnCocina}" icono="egg-fried" tono="info"/>
        <ui:stat etiqueta="Entregados" valor="${data.pedidosEntregados}" icono="check-circle" tono="success"/>
    </div>

    <h2 class="titulo-seccion mb-3">Reservas</h2>
    <div class="row g-3 mb-4">
        <ui:stat etiqueta="Total" valor="${data.reservasTotal}" icono="calendar-check" tono="primary"/>
        <ui:stat etiqueta="Pendientes" valor="${data.reservasPendientes}" icono="clock" tono="warning"/>
        <ui:stat etiqueta="Confirmadas" valor="${data.reservasConfirmadas}" icono="check-circle" tono="success"/>
        <ui:stat etiqueta="Canceladas" valor="${data.reservasCanceladas}" icono="x-circle" tono="danger"/>
    </div>

    <h2 class="titulo-seccion mb-3">Inventario</h2>
    <div class="row g-3 mb-4">
        <ui:stat etiqueta="Productos" valor="${data.productosTotal}" icono="box-seam" tono="info"/>
        <ui:stat etiqueta="Bajo stock" valor="${data.bajoStock}" icono="exclamation-triangle" tono="warning"/>
        <ui:stat etiqueta="Valor total" valor="S/ ${data.valorInventarioTexto}" icono="wallet2" tono="success"/>
    </div>

    <div class="row g-4">
        <div class="col-lg-6">
            <div class="card h-100">
                <div class="card-body">
                    <h3 class="h5 fw-semibold mb-4">Top productos pedidos</h3>
                    <c:choose>
                        <c:when test="${empty data.topProductos}">
                            <p class="text-body-tertiary mb-0">Aún no hay pedidos.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="t" items="${data.topProductos}">
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small mb-1">
                                        <span class="fw-medium">${fn:escapeXml(t.nombre)}</span>
                                        <span class="text-body-secondary">${t.cantidad}</span>
                                    </div>
                                    <%-- Bootstrap fija el largo de la barra con width en línea --%>
                                    <div class="progress progress-delgada rounded-pill" role="progressbar" aria-label="${fn:escapeXml(t.nombre)}"
                                         aria-valuenow="${t.cantidad}" aria-valuemin="0" aria-valuemax="${data.maxTopProductos}">
                                        <div class="progress-bar bg-primary rounded-pill" style="width: ${t.cantidad * 100 / data.maxTopProductos}%"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="col-lg-6">
            <div class="card h-100">
                <div class="card-body">
                    <h3 class="h5 fw-semibold mb-3">Usuarios por rol</h3>
                    <ul class="list-group list-group-flush">
                        <c:forEach var="u" items="${data.usuariosPorRol}">
                            <li class="list-group-item d-flex justify-content-between px-0">
                                <span>${fn:escapeXml(u.nombre)}</span>
                                <span class="fw-bold">${u.cantidad}</span>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</c:if>

<%@ include file="plantilla/pie.jspf" %>
