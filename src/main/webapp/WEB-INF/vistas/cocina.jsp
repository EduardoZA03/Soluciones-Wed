<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Cocina"/>
<c:set var="activo" value="cocina"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Cocina</h1>
        <p>Pedidos activos en preparación · se actualiza cada 10 s</p>
    </div>
</div>

<div class="grilla-2">
    <%-- Columna: por preparar --%>
    <div class="card card-cuerpo">
        <div class="card-cabecera">
            <h3>Por preparar</h3>
            <span class="badge badge-warning">${fn:length(pendientes)}</span>
        </div>
        <c:choose>
            <c:when test="${empty pendientes}">
                <p class="vacio">Sin pedidos.</p>
            </c:when>
            <c:otherwise>
                <div class="lista">
                    <c:forEach var="p" items="${pendientes}">
                        <div class="item">
                            <div>
                                <div class="item-titulo">${fn:escapeXml(p.producto)} <span class="tenue">× ${p.cantidad}</span></div>
                                <div class="item-sub">Mesa ${p.mesa} · ${fn:escapeXml(p.cliente)} · Mesero: ${fn:escapeXml(p.mesero)}</div>
                            </div>
                            <form method="post" action="${ctx}/cocina">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="id" value="${p.id}">
                                <input type="hidden" name="estado" value="En cocina">
                                <button type="submit" class="btn btn-sm btn-primary"><i data-lucide="arrow-right"></i>A cocina</button>
                            </form>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- Columna: en preparación --%>
    <div class="card card-cuerpo">
        <div class="card-cabecera">
            <h3>En preparación</h3>
            <span class="badge badge-info">${fn:length(enCocina)}</span>
        </div>
        <c:choose>
            <c:when test="${empty enCocina}">
                <p class="vacio">Sin pedidos.</p>
            </c:when>
            <c:otherwise>
                <div class="lista">
                    <c:forEach var="p" items="${enCocina}">
                        <div class="item">
                            <div>
                                <div class="item-titulo">${fn:escapeXml(p.producto)} <span class="tenue">× ${p.cantidad}</span></div>
                                <div class="item-sub">Mesa ${p.mesa} · ${fn:escapeXml(p.cliente)} · Mesero: ${fn:escapeXml(p.mesero)}</div>
                            </div>
                            <form method="post" action="${ctx}/cocina">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="id" value="${p.id}">
                                <input type="hidden" name="estado" value="Entregado">
                                <button type="submit" class="btn btn-sm btn-success"><i data-lucide="check"></i>Entregar</button>
                            </form>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
