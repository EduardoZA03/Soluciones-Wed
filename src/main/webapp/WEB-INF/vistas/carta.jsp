<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Carta y recetas"/>
<c:set var="activo" value="carta"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Carta y recetas</h1>
        <p class="text-body-secondary mb-0">Al vender un plato en el POS se descuentan automáticamente los insumos de su receta</p>
    </div>
    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalPlato"
            data-limpiar data-titulo="Nuevo plato" data-boton="Agregar">
        <i class="bi bi-plus-lg me-1"></i>Nuevo plato
    </button>
</div>

<div class="row g-4">
    <c:forEach var="p" items="${platos}">
        <div class="col-md-6 col-xl-4">
            <div class="card h-100">
                <div class="card-body d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div>
                            <h2 class="h5 fw-semibold mb-1">${fn:escapeXml(p.nombre)}</h2>
                            <span class="text-body-secondary">S/ ${p.precioTexto}</span>
                            <c:if test="${not p.activo}"><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis ms-1">inactivo</span></c:if>
                        </div>
                        <div class="d-flex gap-1">
                            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill" title="Editar"
                                    data-bs-toggle="modal" data-bs-target="#modalPlato"
                                    data-titulo="Editar plato" data-boton="Guardar cambios"
                                    data-campo-id="${p.id}" data-campo-nombre="${fn:escapeXml(p.nombre)}"
                                    data-campo-precio="${p.precioTexto}" data-activo="${p.activo}">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <form method="post" action="${ctx}/carta" class="m-0"
                                  data-confirmar="¿Eliminar el plato &quot;${fn:escapeXml(p.nombre)}&quot; y su receta?">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="${p.id}">
                                <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill" title="Eliminar"><i class="bi bi-trash"></i></button>
                            </form>
                        </div>
                    </div>

                    <div class="titulo-seccion mb-2">Receta por porción</div>
                    <c:choose>
                        <c:when test="${empty p.receta}">
                            <p class="small text-body-tertiary">Sin ingredientes: este plato no descuenta insumos.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="list-group list-group-flush mb-3">
                                <c:forEach var="r" items="${p.receta}">
                                    <li class="list-group-item px-0 d-flex justify-content-between align-items-center">
                                        <span>${fn:escapeXml(r.insumo)}</span>
                                        <span class="d-flex align-items-center gap-2">
                                            <span class="text-body-secondary">${fn:escapeXml(r.cantidadTexto += ' ' += r.unidad)}</span>
                                            <form method="post" action="${ctx}/carta" class="m-0">
                                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                                <input type="hidden" name="accion" value="quitar">
                                                <input type="hidden" name="receta_id" value="${r.id}">
                                                <button type="submit" class="btn btn-sm btn-link text-danger p-0" title="Quitar"><i class="bi bi-x-circle"></i></button>
                                            </form>
                                        </span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>

                    <form method="post" action="${ctx}/carta" class="mt-auto">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="ingrediente">
                        <input type="hidden" name="id" value="${p.id}">
                        <div class="input-group input-group-sm">
                            <select class="form-select" name="insumo_id" aria-label="Insumo" required>
                                <option value="">Agregar insumo…</option>
                                <c:forEach var="i" items="${insumos}">
                                    <option value="${i.id}">${fn:escapeXml(i.producto)} (${fn:escapeXml(i.unidad)})</option>
                                </c:forEach>
                            </select>
                            <input class="form-control cantidad-receta" name="cantidad" type="number" min="0.001" step="0.001"
                                   placeholder="Cant." aria-label="Cantidad por porción" required>
                            <button type="submit" class="btn btn-primary" title="Agregar a la receta"><i class="bi bi-plus-lg"></i></button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty platos}">
        <div class="col-12"><div class="card card-body text-center text-body-secondary py-5">Aún no hay platos en la carta.</div></div>
    </c:if>
</div>

<div class="modal fade" id="modalPlato" tabindex="-1" aria-labelledby="tituloPlato" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${ctx}/carta">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold" id="tituloPlato">Nuevo plato</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="guardar">
                    <input type="hidden" name="id" value="" data-limpiable>
                    <div class="mb-3">
                        <label for="nombre" class="form-label">Nombre</label>
                        <input class="form-control" id="nombre" name="nombre" maxlength="100" placeholder="Ej. Chupe de camarones" required>
                    </div>
                    <div class="mb-3">
                        <label for="precio" class="form-label">Precio de venta (S/)</label>
                        <input class="form-control" id="precio" name="precio" type="number" min="0" step="0.01" placeholder="Ej. 25.00">
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" role="switch" id="activo" name="activo" checked>
                        <label class="form-check-label" for="activo">Disponible para la venta</label>
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
