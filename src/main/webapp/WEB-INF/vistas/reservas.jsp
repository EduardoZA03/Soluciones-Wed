<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Reservas"/>
<c:set var="activo" value="reservas"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Reservas</h1>
        <p class="text-body-secondary mb-0">Administración de reservas del restaurante</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalReserva">
        <i class="bi bi-plus-lg me-1"></i>Nueva reserva
    </button>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Total" valor="${total}" icono="calendar-check" tono="primary"/>
    <ui:stat etiqueta="Pendientes" valor="${pendientes}" icono="clock" tono="warning"/>
    <ui:stat etiqueta="Confirmadas" valor="${confirmadas}" icono="check-circle" tono="success"/>
    <ui:stat etiqueta="Canceladas" valor="${canceladas}" icono="x-circle" tono="danger"/>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th class="ps-4">Cliente</th><th>Teléfono</th><th>Fecha</th><th>Hora</th>
                    <th>Pers.</th><th>Mesa</th><th>Estado</th><th class="text-end pe-4">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="r" items="${reservas}">
                    <tr>
                        <td class="ps-4 fw-medium">${fn:escapeXml(r.cliente)}</td>
                        <td>${empty r.telefono ? '—' : fn:escapeXml(r.telefono)}</td>
                        <td>${r.fechaTexto}</td>
                        <td>${r.horaTexto}</td>
                        <td>${r.personas}</td>
                        <td>${r.mesa}</td>
                        <td><span class="badge rounded-pill bg-${r.tono}-subtle text-${r.tono}-emphasis">${r.estado}</span></td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end align-items-center gap-2">
                                <form method="post" action="${ctx}/reservas" class="m-0">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <select class="form-select form-select-sm rounded-pill select-estado" name="estado"
                                            data-autoenvio aria-label="Estado de la reserva">
                                        <c:forEach var="e" items="${estados}">
                                            <option value="${e}" ${e == r.estado ? 'selected' : ''}>${e}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                                <form method="post" action="${ctx}/reservas" class="m-0"
                                      data-confirmar="¿Eliminar la reserva de ${fn:escapeXml(r.cliente)}?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <button type="submit" class="btn btn-sm btn-danger rounded-pill">Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reservas}">
                    <tr><td colspan="8" class="text-center text-body-secondary py-5">No hay reservas.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal: nueva reserva --%>
<div class="modal fade" id="modalReserva" tabindex="-1" aria-labelledby="tituloReserva" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/reservas">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloReserva">Nueva reserva</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="cliente" class="form-label">Cliente</label>
                            <input class="form-control" id="cliente" name="cliente" placeholder="Nombre del cliente" maxlength="100" required>
                        </div>
                        <div class="col-12">
                            <label for="telefono" class="form-label">Teléfono</label>
                            <input class="form-control" id="telefono" name="telefono" placeholder="Opcional" maxlength="30">
                        </div>
                        <div class="col-6">
                            <label for="fecha" class="form-label">Fecha</label>
                            <input class="form-control" id="fecha" name="fecha" type="date" required>
                        </div>
                        <div class="col-6">
                            <label for="hora" class="form-label">Hora</label>
                            <input class="form-control" id="hora" name="hora" type="time" required>
                        </div>
                        <div class="col-6">
                            <label for="personas" class="form-label">Personas</label>
                            <input class="form-control" id="personas" name="personas" type="number" min="1" placeholder="Ej. 4" required>
                        </div>
                        <div class="col-6">
                            <label for="mesa" class="form-label">Mesa</label>
                            <input class="form-control" id="mesa" name="mesa" type="number" min="1" placeholder="Ej. 7" required>
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
