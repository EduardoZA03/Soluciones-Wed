<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Pedidos"/>
<c:set var="activo" value="pedidos"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Pedidos</h1>
        <p>Registro y seguimiento de pedidos</p>
    </div>
    <button type="button" class="btn btn-primary" data-abrir="modalPedido"><i data-lucide="plus"></i> Nuevo pedido</button>
</div>

<div class="pila">
    <div class="grilla-stats">
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Total</span><span class="icono tono-brand"><i data-lucide="list-checks"></i></span></div>
            <div class="valor">${total}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Pendientes</span><span class="icono tono-warning"><i data-lucide="clock"></i></span></div>
            <div class="valor">${pendientes}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">En cocina</span><span class="icono tono-info"><i data-lucide="chef-hat"></i></span></div>
            <div class="valor">${enCocina}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Entregados</span><span class="icono tono-success"><i data-lucide="check-circle-2"></i></span></div>
            <div class="valor">${entregados}</div>
        </div>
    </div>

    <div class="chips">
        <span class="titulo">Mesero:</span>
        <a class="chip ${filtroMesero == 'Todos' ? 'activo' : ''}" href="${ctx}/pedidos">Todos</a>
        <c:forEach var="m" items="${meseros}">
            <c:url var="urlMesero" value="/pedidos"><c:param name="mesero" value="${m}"/></c:url>
            <a class="chip ${filtroMesero == m ? 'activo' : ''}" href="${urlMesero}">${fn:escapeXml(m)}</a>
        </c:forEach>
    </div>

    <div class="card tabla-envoltura">
        <table class="tabla">
            <thead>
                <tr>
                    <th>#</th><th>Cliente</th><th>Mesero</th><th>Producto</th>
                    <th>Cant.</th><th>Mesa</th><th>Estado</th><th class="derecha">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${pedidos}" varStatus="i">
                    <tr>
                        <td class="tenue">${i.count}</td>
                        <td class="fuerte">${fn:escapeXml(p.cliente)}</td>
                        <td>${fn:escapeXml(p.mesero)}</td>
                        <td>${fn:escapeXml(p.producto)}</td>
                        <td>${p.cantidad}</td>
                        <td>${p.mesa}</td>
                        <td><span class="badge badge-${p.tono}">${p.estado}</span></td>
                        <td>
                            <div class="acciones">
                                <form method="post" action="${ctx}/pedidos">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="cambiar">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
                                    <button type="submit" class="btn btn-sm btn-secondary">Cambiar</button>
                                </form>
                                <form method="post" action="${ctx}/pedidos"
                                      data-confirmar="¿Eliminar el pedido de ${fn:escapeXml(p.cliente)}?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
                                    <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pedidos}">
                    <tr><td colspan="8" class="vacio">No hay pedidos registrados.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<dialog class="modal" id="modalPedido">
    <div class="modal-cabecera">
        <h3>Nuevo pedido</h3>
        <button type="button" class="btn-icono" data-cerrar aria-label="Cerrar"><i data-lucide="x"></i></button>
    </div>
    <div class="modal-cuerpo">
        <form method="post" action="${ctx}/pedidos" class="form-pila">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="crear">
            <input type="hidden" name="filtroMesero" value="${fn:escapeXml(filtroMesero)}">
            <div class="campo">
                <label for="cliente">Cliente</label>
                <input class="input" id="cliente" name="cliente" placeholder="Nombre del cliente" maxlength="100" required>
            </div>
            <div class="campo">
                <label for="mesero">Mesero</label>
                <input class="input" id="mesero" name="mesero" placeholder="Nombre del mesero" maxlength="100" required>
            </div>
            <div class="campo">
                <label for="producto">Producto</label>
                <input class="input" id="producto" name="producto" placeholder="Producto" maxlength="100" required>
            </div>
            <div class="dos-col">
                <div class="campo">
                    <label for="cantidad">Cantidad</label>
                    <input class="input" id="cantidad" name="cantidad" type="number" min="1" placeholder="Ej. 2" required>
                </div>
                <div class="campo">
                    <label for="mesa">Mesa</label>
                    <input class="input" id="mesa" name="mesa" type="number" min="1" placeholder="Ej. 4" required>
                </div>
            </div>
            <div class="campo">
                <label for="estado">Estado</label>
                <select class="select" id="estado" name="estado">
                    <c:forEach var="e" items="${estados}"><option value="${e}">${e}</option></c:forEach>
                </select>
            </div>
            <div class="botones-form">
                <button type="button" class="btn btn-outline btn-full" data-cerrar>Cancelar</button>
                <button type="submit" class="btn btn-primary btn-full">Registrar</button>
            </div>
        </form>
    </div>
</dialog>

<%@ include file="plantilla/pie.jspf" %>
