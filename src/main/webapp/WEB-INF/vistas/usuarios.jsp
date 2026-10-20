<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Usuarios"/>
<c:set var="activo" value="usuarios"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="encabezado">
    <div>
        <h1>Usuarios</h1>
        <p>Gestión de empleados y accesos</p>
    </div>
    <button type="button" class="btn btn-primary" data-abrir="modalUsuario"><i data-lucide="plus"></i> Nuevo usuario</button>
</div>

<div class="card tabla-envoltura">
    <table class="tabla">
        <thead>
            <tr><th>Usuario</th><th>Rol</th><th>Creado</th><th class="derecha">Acciones</th></tr>
        </thead>
        <tbody>
            <c:forEach var="u" items="${usuarios}">
                <c:set var="esYo" value="${u.id == sessionScope.usuario.id}"/>
                <tr>
                    <td class="fuerte">
                        ${fn:escapeXml(u.usuario)}
                        <c:if test="${esYo}"><span class="badge badge-info">tú</span></c:if>
                    </td>
                    <td>
                        <form method="post" action="${ctx}/usuarios">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="accion" value="rol">
                            <input type="hidden" name="id" value="${u.id}">
                            <select class="select select-mini" name="rol" data-autoenvio aria-label="Rol">
                                <c:forEach var="r" items="${roles}">
                                    <option value="${r}" ${r == u.rol ? 'selected' : ''}>${r}</option>
                                </c:forEach>
                            </select>
                        </form>
                    </td>
                    <td>${u.creadoEnTexto}</td>
                    <td>
                        <div class="acciones">
                            <button type="button" class="btn btn-sm btn-secondary" data-abrir="modalClave"
                                    data-titulo="Nueva clave para &quot;${fn:escapeXml(u.usuario)}&quot;"
                                    data-campo-id="${u.id}">Resetear clave</button>
                            <form method="post" action="${ctx}/usuarios"
                                  data-confirmar="¿Eliminar al usuario &quot;${fn:escapeXml(u.usuario)}&quot;?">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="${u.id}">
                                <button type="submit" class="btn btn-sm btn-danger" ${esYo ? 'disabled' : ''}>Eliminar</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty usuarios}">
                <tr><td colspan="4" class="vacio">No hay usuarios.</td></tr>
            </c:if>
        </tbody>
    </table>
</div>

<dialog class="modal" id="modalUsuario">
    <div class="modal-cabecera">
        <h3>Nuevo usuario</h3>
        <button type="button" class="btn-icono" data-cerrar aria-label="Cerrar"><i data-lucide="x"></i></button>
    </div>
    <div class="modal-cuerpo">
        <form method="post" action="${ctx}/usuarios" class="form-pila">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="crear">
            <div class="campo">
                <label for="usuario">Usuario</label>
                <input class="input" id="usuario" name="usuario" placeholder="Nombre de usuario" maxlength="50" required>
            </div>
            <div class="campo">
                <label for="clave">Clave</label>
                <input class="input" id="clave" name="clave" type="password" placeholder="Mínimo 4 caracteres" minlength="4" required>
            </div>
            <div class="campo">
                <label for="rol">Rol</label>
                <select class="select" id="rol" name="rol">
                    <c:forEach var="r" items="${roles}">
                        <option value="${r}" ${r == 'Empleado' ? 'selected' : ''}>${r}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="botones-form">
                <button type="button" class="btn btn-outline btn-full" data-cerrar>Cancelar</button>
                <button type="submit" class="btn btn-primary btn-full">Crear</button>
            </div>
        </form>
    </div>
</dialog>

<dialog class="modal" id="modalClave">
    <div class="modal-cabecera">
        <h3 data-titulo-modal>Nueva clave</h3>
        <button type="button" class="btn-icono" data-cerrar aria-label="Cerrar"><i data-lucide="x"></i></button>
    </div>
    <div class="modal-cuerpo">
        <form method="post" action="${ctx}/usuarios" class="form-pila">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="clave">
            <input type="hidden" name="id" value="">
            <div class="campo">
                <label for="nuevaClave">Nueva clave</label>
                <input class="input" id="nuevaClave" name="clave" type="password" placeholder="Mínimo 4 caracteres" minlength="4" required>
            </div>
            <div class="botones-form">
                <button type="button" class="btn btn-outline btn-full" data-cerrar>Cancelar</button>
                <button type="submit" class="btn btn-primary btn-full">Guardar</button>
            </div>
        </form>
    </div>
</dialog>

<%@ include file="plantilla/pie.jspf" %>
