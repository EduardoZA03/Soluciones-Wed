<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Proveedores"/>
<c:set var="activo" value="proveedores"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Proveedores</h1>
        <p class="text-body-secondary mb-0">Datos de contacto y conexión por API para enviar órdenes de compra</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalProveedor"
            data-limpiar data-titulo="Nuevo proveedor" data-boton="Agregar">
        <i class="bi bi-plus-lg me-1"></i>Nuevo proveedor
    </button>
</div>

<div class="alert alert-light border small">
    <i class="bi bi-plug me-1"></i><b>Interoperabilidad:</b> CalSoft envía cada orden aprobada como JSON por
    <code>HTTP POST</code> a la URL del proveedor, con su API key en la cabecera <code>X-Api-Key</code>.
    Si la URL se deja vacía se usa el <b>proveedor simulado</b> incluido: <code>${fn:escapeXml(urlSimulador)}</code>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr><th class="ps-4">Proveedor</th><th>RUC</th><th>Contacto</th><th>API</th><th>API key</th><th class="text-end pe-4">Acciones</th></tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${proveedores}">
                    <tr>
                        <td class="ps-4 fw-medium">${fn:escapeXml(p.nombre)}</td>
                        <td>${empty p.ruc ? '—' : p.ruc}</td>
                        <td class="small">${fn:escapeXml(p.telefono)}<br>${fn:escapeXml(p.email)}</td>
                        <td class="small">
                            <c:choose>
                                <c:when test="${p.simulado}"><span class="badge rounded-pill bg-info-subtle text-info-emphasis">Simulado</span></c:when>
                                <c:otherwise><code>${fn:escapeXml(p.apiUrl)}</code></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="small font-monospace">${p.apiKeyOculta}</td>
                        <td class="pe-4">
                            <div class="d-flex justify-content-end gap-1">
                                <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill" title="Editar"
                                        data-bs-toggle="modal" data-bs-target="#modalProveedor"
                                        data-titulo="Editar proveedor" data-boton="Guardar cambios"
                                        data-campo-id="${p.id}" data-campo-nombre="${fn:escapeXml(p.nombre)}"
                                        data-campo-ruc="${p.ruc}" data-campo-telefono="${fn:escapeXml(p.telefono)}"
                                        data-campo-email="${fn:escapeXml(p.email)}" data-campo-api-url="${fn:escapeXml(p.apiUrl)}">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <form method="post" action="${ctx}/proveedores" class="m-0"
                                      data-confirmar="¿Eliminar al proveedor &quot;${fn:escapeXml(p.nombre)}&quot;?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill" title="Eliminar"><i class="bi bi-trash"></i></button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty proveedores}">
                    <tr><td colspan="6" class="text-center text-body-secondary py-5">No hay proveedores.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="modal fade" id="modalProveedor" tabindex="-1" aria-labelledby="tituloProveedor" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/proveedores">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloProveedor">Nuevo proveedor</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="guardar">
                    <input type="hidden" name="id" value="" data-limpiable>
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="nombre" class="form-label">Razón social</label>
                            <input class="form-control" id="nombre" name="nombre" maxlength="100" required>
                        </div>
                        <div class="col-6">
                            <label for="ruc" class="form-label">RUC</label>
                            <input class="form-control" id="ruc" name="ruc" maxlength="11" pattern="\d{11}" placeholder="11 dígitos">
                        </div>
                        <div class="col-6">
                            <label for="telefono" class="form-label">Teléfono</label>
                            <input class="form-control" id="telefono" name="telefono" maxlength="30">
                        </div>
                        <div class="col-12">
                            <label for="email" class="form-label">Email</label>
                            <input class="form-control" id="email" name="email" type="email" maxlength="100">
                        </div>
                        <div class="col-12">
                            <label for="api_url" class="form-label">URL de la API</label>
                            <input class="form-control" id="api_url" name="api_url" type="url" maxlength="255"
                                   placeholder="Vacío = proveedor simulado">
                        </div>
                        <div class="col-12">
                            <label for="api_key" class="form-label">API key</label>
                            <input class="form-control" id="api_key" name="api_key" maxlength="100" autocomplete="off"
                                   placeholder="Al editar, déjalo vacío para conservar la actual">
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Agregar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
