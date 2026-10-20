<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Inicio"/>
<c:set var="activo" value="dashboard"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Inicio</h1>
        <p>Resumen general del restaurante</p>
    </div>
</div>

<div class="pila">
    <div class="grilla-stats">
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Pedidos activos</span><span class="icono tono-brand"><i data-lucide="list-checks"></i></span></div>
            <div class="valor">${empty stats ? '—' : stats.pedidosActivos}</div>
            <c:if test="${not empty stats}"><div class="sub">${stats.pedidosTotal} en total</div></c:if>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Reservas pendientes</span><span class="icono tono-warning"><i data-lucide="calendar-clock"></i></span></div>
            <div class="valor">${empty stats ? '—' : stats.reservasPendientes}</div>
            <c:if test="${not empty stats}"><div class="sub">${stats.reservasConfirmadas} confirmadas</div></c:if>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Bajo stock</span><span class="icono tono-danger"><i data-lucide="alert-triangle"></i></span></div>
            <div class="valor">${empty stats ? '—' : stats.bajoStock}</div>
            <c:if test="${not empty stats}"><div class="sub">${stats.productosTotal} productos</div></c:if>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Valor inventario</span><span class="icono tono-success"><i data-lucide="wallet"></i></span></div>
            <div class="valor">${empty stats ? '—' : 'S/ '.concat(stats.valorInventarioEntero)}</div>
        </div>
    </div>

    <div class="grilla-2">
        <div class="card card-cuerpo">
            <div class="card-cabecera">
                <h2>Pedidos recientes</h2>
                <a class="ver-todos" href="${ctx}/pedidos">Ver todos →</a>
            </div>
            <c:choose>
                <c:when test="${empty pedidosRecientes}">
                    <div class="caja-vacia">No hay pedidos.</div>
                </c:when>
                <c:otherwise>
                    <ul class="lista">
                        <c:forEach var="p" items="${pedidosRecientes}">
                            <li class="item">
                                <div>
                                    <div class="item-titulo">${fn:escapeXml(p.producto)} × ${p.cantidad}</div>
                                    <div class="item-sub">${fn:escapeXml(p.cliente)} · Mesa ${p.mesa}</div>
                                </div>
                                <span class="badge badge-${p.tono}">${p.estado}</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="card card-cuerpo">
            <div class="card-cabecera">
                <h2>Próximas reservas</h2>
                <a class="ver-todos" href="${ctx}/reservas">Ver todos →</a>
            </div>
            <c:choose>
                <c:when test="${empty reservasProximas}">
                    <div class="caja-vacia">Sin reservas próximas.</div>
                </c:when>
                <c:otherwise>
                    <ul class="lista">
                        <c:forEach var="r" items="${reservasProximas}">
                            <li class="item">
                                <div>
                                    <div class="item-titulo">${fn:escapeXml(r.cliente)}</div>
                                    <div class="item-sub">${r.fechaTexto} · ${r.horaTexto} · ${r.personas} pers · Mesa ${r.mesa}</div>
                                </div>
                                <span class="badge badge-${r.tono}">${r.estado}</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
