<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Reportes"/>
<c:set var="activo" value="reportes"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Reportes</h1>
        <p>Estadísticas del negocio</p>
    </div>
</div>

<c:if test="${not empty data}">
<div class="pila">
    <section>
        <h2 class="seccion-titulo">Pedidos</h2>
        <div class="grilla-stats">
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Total</span><span class="icono tono-brand"><i data-lucide="list-checks"></i></span></div>
                <div class="valor">${data.pedidosTotal}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Pendientes</span><span class="icono tono-warning"><i data-lucide="clock"></i></span></div>
                <div class="valor">${data.pedidosPendientes}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">En cocina</span><span class="icono tono-info"><i data-lucide="chef-hat"></i></span></div>
                <div class="valor">${data.pedidosEnCocina}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Entregados</span><span class="icono tono-success"><i data-lucide="check-circle-2"></i></span></div>
                <div class="valor">${data.pedidosEntregados}</div>
            </div>
        </div>
    </section>

    <section>
        <h2 class="seccion-titulo">Reservas</h2>
        <div class="grilla-stats">
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Total</span><span class="icono tono-brand"><i data-lucide="calendar-check"></i></span></div>
                <div class="valor">${data.reservasTotal}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Pendientes</span><span class="icono tono-warning"><i data-lucide="clock"></i></span></div>
                <div class="valor">${data.reservasPendientes}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Confirmadas</span><span class="icono tono-success"><i data-lucide="check-circle-2"></i></span></div>
                <div class="valor">${data.reservasConfirmadas}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Canceladas</span><span class="icono tono-danger"><i data-lucide="alert-triangle"></i></span></div>
                <div class="valor">${data.reservasCanceladas}</div>
            </div>
        </div>
    </section>

    <section>
        <h2 class="seccion-titulo">Inventario</h2>
        <div class="grilla-stats">
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Productos</span><span class="icono tono-info"><i data-lucide="package"></i></span></div>
                <div class="valor">${data.productosTotal}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Bajo stock</span><span class="icono tono-warning"><i data-lucide="alert-triangle"></i></span></div>
                <div class="valor">${data.bajoStock}</div>
            </div>
            <div class="card card-cuerpo stat">
                <div class="fila"><span class="etiqueta">Valor total</span><span class="icono tono-success"><i data-lucide="wallet"></i></span></div>
                <div class="valor">S/ ${data.valorInventarioTexto}</div>
            </div>
        </div>
    </section>

    <div class="grilla-2">
        <div class="card card-cuerpo">
            <div class="card-cabecera"><h3>Top productos pedidos</h3></div>
            <c:choose>
                <c:when test="${empty data.topProductos}">
                    <p class="tenue">Aún no hay pedidos.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="t" items="${data.topProductos}">
                        <div class="barra-fila">
                            <div class="rotulo"><span>${fn:escapeXml(t.nombre)}</span><span>${t.cantidad}</span></div>
                            <div class="barra"><div style="width: ${t.cantidad * 100 / data.maxTopProductos}%"></div></div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="card card-cuerpo">
            <div class="card-cabecera"><h3>Usuarios por rol</h3></div>
            <div class="filas-divididas">
                <c:forEach var="u" items="${data.usuariosPorRol}">
                    <div><span>${fn:escapeXml(u.nombre)}</span><strong>${u.cantidad}</strong></div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>
</c:if>

<%@ include file="plantilla/pie.jspf" %>
