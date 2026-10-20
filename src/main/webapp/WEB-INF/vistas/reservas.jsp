<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Reservas"/>
<c:set var="activo" value="reservas"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Reservas</h1>
        <p>Administración de reservas del restaurante</p>
    </div>
    <button type="button" class="btn btn-primary" data-abrir="modalReserva"><i data-lucide="plus"></i> Nueva reserva</button>
</div>

<div class="pila">
    <div class="grilla-stats">
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Total</span><span class="icono tono-brand"><i data-lucide="calendar-check"></i></span></div>
            <div class="valor">${total}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Pendientes</span><span class="icono tono-warning"><i data-lucide="clock"></i></span></div>
            <div class="valor">${pendientes}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Confirmadas</span><span class="icono tono-success"><i data-lucide="check-circle-2"></i></span></div>
            <div class="valor">${confirmadas}</div>
        </div>
        <div class="card card-cuerpo stat">
            <div class="fila"><span class="etiqueta">Canceladas</span><span class="icono tono-danger"><i data-lucide="x-circle"></i></span></div>
            <div class="valor">${canceladas}</div>
        </div>
    </div>

    <div class="card tabla-envoltura">
        <table class="tabla">
            <thead>
                <tr>
                    <th>Cliente</th><th>Teléfono</th><th>Fecha</th><th>Hora</th>
                    <th>Pers.</th><th>Mesa</th><th>Estado</th><th class="derecha">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="r" items="${reservas}">
                    <tr>
                        <td class="fuerte">${fn:escapeXml(r.cliente)}</td>
                        <td>${empty r.telefono ? '—' : fn:escapeXml(r.telefono)}</td>
                        <td>${r.fechaTexto}</td>
                        <td>${r.horaTexto}</td>
                        <td>${r.personas}</td>
                        <td>${r.mesa}</td>
                        <td><span class="badge badge-${r.tono}">${r.estado}</span></td>
                        <td>
                            <div class="acciones">
                                <form method="post" action="${ctx}/reservas">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <select class="select select-mini" name="estado" data-autoenvio aria-label="Estado">
                                        <c:forEach var="e" items="${estados}">
                                            <option value="${e}" ${e == r.estado ? 'selected' : ''}>${e}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                                <form method="post" action="${ctx}/reservas"
                                      data-confirmar="¿Eliminar la reserva de ${fn:escapeXml(r.cliente)}?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reservas}">
                    <tr><td colspan="8" class="vacio">No hay reservas.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<dialog class="modal" id="modalReserva">
    <div class="modal-cabecera">
        <h3>Nueva reserva</h3>
        <button type="button" class="btn-icono" data-cerrar aria-label="Cerrar"><i data-lucide="x"></i></button>
    </div>
    <div class="modal-cuerpo">
        <form method="post" action="${ctx}/reservas" class="form-pila">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="crear">
            <div class="campo">
                <label for="cliente">Cliente</label>
                <input class="input" id="cliente" name="cliente" placeholder="Nombre del cliente" maxlength="100" required>
            </div>
            <div class="campo">
                <label for="telefono">Teléfono</label>
                <input class="input" id="telefono" name="telefono" placeholder="Opcional" maxlength="30">
            </div>
            <div class="dos-col">
                <div class="campo">
                    <label for="fecha">Fecha</label>
                    <input class="input" id="fecha" name="fecha" type="date" required>
                </div>
                <div class="campo">
                    <label for="hora">Hora</label>
                    <input class="input" id="hora" name="hora" type="time" required>
                </div>
            </div>
            <div class="dos-col">
                <div class="campo">
                    <label for="personas">Personas</label>
                    <input class="input" id="personas" name="personas" type="number" min="1" placeholder="Ej. 4" required>
                </div>
                <div class="campo">
                    <label for="mesa">Mesa</label>
                    <input class="input" id="mesa" name="mesa" type="number" min="1" placeholder="Ej. 7" required>
                </div>
            </div>
            <div class="botones-form">
                <button type="button" class="btn btn-outline btn-full" data-cerrar>Cancelar</button>
                <button type="submit" class="btn btn-primary btn-full">Registrar</button>
            </div>
        </form>
    </div>
</dialog>

<%@ include file="plantilla/pie.jspf" %>
