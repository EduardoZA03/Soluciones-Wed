<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Usuarios"/>
<c:set var="activo" value="usuarios"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Usuarios</h1>
        <p class="text-body-secondary mb-0">Gestión de empleados y accesos</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalUsuario">
        <i class="bi bi-plus-lg me-1"></i>Nuevo usuario
    </button>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr><th class="ps-4">Usuario</th><th>Rol</th><th>Creado</th><th class="text-end pe-4">Acciones</th></tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${usuarios}">
                    <c:set var="esYo" value="${u.id == sessionScope.usuario.id}"/>
                    <tr>
                        <td class="ps-4 fw-medium">
                            ${fn:escapeXml(u.usuario)}
                            <c:if test="${esYo}"><span class="badge rounded-pill bg-info-subtle text-info-emphasis ms-1">tú</span></c:if>
                        </td>
                        <td>
                            <form method="post" action="${ctx}/usuarios" class="m-0">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="rol">
                                <input type="hidden" name="id" value="${u.id}">
                                <select class="form-select form-select-sm rounded-pill select-estado" name="rol"
                                        data-autoenvio aria-label="Rol del usuario">
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r}" ${r == u.rol ? 'selected' : ''}>${r}</option>
                                    </c:forEach>
                                </select>
                            </form>
                        </td>
                        <td>${u.creadoEnTexto}</td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-2">
                                <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill text-nowrap"
                                        data-bs-toggle="modal" data-bs-target="#modalClave"
                                        data-titulo="Nueva clave para &quot;${fn:escapeXml(u.usuario)}&quot;"
                                        data-campo-id="${u.id}">Resetear clave</button>
                                <form method="post" action="${ctx}/usuarios" class="m-0"
                                      data-confirmar="¿Eliminar al usuario &quot;${fn:escapeXml(u.usuario)}&quot;?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <button type="submit" class="btn btn-sm btn-danger rounded-pill" ${esYo ? 'disabled' : ''}>Eliminar</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty usuarios}">
                    <tr><td colspan="4" class="text-center text-body-secondary py-5">No hay usuarios.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal: nuevo usuario --%>
<div class="modal fade" id="modalUsuario" tabindex="-1" aria-labelledby="tituloUsuario" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/usuarios">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloUsuario">Nuevo usuario</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <div class="mb-3">
                        <label for="usuario" class="form-label">Usuario</label>
                        <input class="form-control" id="usuario" name="usuario" placeholder="Nombre de usuario" maxlength="50" required>
                    </div>
                    <div class="mb-3">
                        <label for="clave" class="form-label">Clave</label>
                        <input class="form-control" id="clave" name="clave" type="password" placeholder="Mínimo 4 caracteres" minlength="4" required>
                    </div>
                    <div>
                        <label for="rol" class="form-label">Rol</label>
                        <select class="form-select" id="rol" name="rol">
                            <c:forEach var="r" items="${roles}">
                                <option value="${r}" ${r == 'Empleado' ? 'selected' : ''}>${r}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Crear</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- Modal: resetear clave --%>
<div class="modal fade" id="modalClave" tabindex="-1" aria-labelledby="tituloClave" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/usuarios">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloClave">Nueva clave</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="clave">
                    <input type="hidden" name="id" value="">
                    <label for="nuevaClave" class="form-label">Nueva clave</label>
                    <input class="form-control" id="nuevaClave" name="clave" type="password" placeholder="Mínimo 4 caracteres" minlength="4" required>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Guardar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
