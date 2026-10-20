<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Orden ${orden.codigo}"/>
<c:set var="activo" value="compras"/>
<%@ include file="plantilla/cabecera.jspf" %>

<a class="small text-decoration-none link-secondary" href="${ctx}/compras"><i class="bi bi-arrow-left me-1"></i>Órdenes de compra</a>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mt-2 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">${orden.codigo}
            <span class="badge rounded-pill bg-${orden.tono}-subtle text-${orden.tono}-emphasis fs-6 align-middle">${orden.estado}</span>
        </h1>
        <p class="text-body-secondary mb-0">
            ${fn:escapeXml(orden.proveedorNombre)} · creada ${orden.creadoEnTexto} por ${fn:escapeXml(orden.creadoPor)}
            <c:if test="${not empty orden.confirmacionProveedor}"> · confirmación <b>${fn:escapeXml(orden.confirmacionProveedor)}</b></c:if>
            <c:if test="${not empty orden.fechaEntrega}"> · entrega <b>${orden.fechaEntregaTexto}</b></c:if>
        </p>
    </div>
    <div class="d-flex flex-wrap gap-2">
        <c:if test="${orden.editable and sessionScope.usuario.puedeAprobar}">
            <form method="post" action="${ctx}/compras" class="m-0"
                  data-confirmar="¿Aprobar la orden por S/ ${orden.totalTexto} y enviarla al proveedor?">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="aprobar">
                <input type="hidden" name="id" value="${orden.id}">
                <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-send-check me-1"></i>Aprobar y enviar</button>
            </form>
        </c:if>
        <c:if test="${orden.reenviable}">
            <form method="post" action="${ctx}/compras" class="m-0">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="reenviar">
                <input type="hidden" name="id" value="${orden.id}">
                <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-arrow-repeat me-1"></i>Reintentar envío</button>
            </form>
        </c:if>
        <c:if test="${orden.recibible and sessionScope.usuario.puede('recepcion')}">
            <a class="btn btn-success rounded-pill px-4" href="${ctx}/recepcion?codigo=${orden.codigo}"><i class="bi bi-box-arrow-in-down me-1"></i>Recibir</a>
        </c:if>
        <c:if test="${orden.cancelable}">
            <form method="post" action="${ctx}/compras" class="m-0" data-confirmar="¿Cancelar esta orden?">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="cancelar">
                <input type="hidden" name="id" value="${orden.id}">
                <button type="submit" class="btn btn-outline-danger rounded-pill px-4">Cancelar</button>
            </form>
        </c:if>
    </div>
</div>

<c:if test="${orden.editable and not sessionScope.usuario.puedeAprobar}">
    <div class="alert alert-info"><i class="bi bi-info-circle me-1"></i>Borrador listo para revisión: solo el Administrador puede aprobarlo y enviarlo.</div>
</c:if>

<div class="row g-4">
    <div class="col-lg-8">
        <%-- Ítems: editables mientras la orden está en Borrador (HU-06) --%>
        <div class="card mb-4">
            <div class="card-body">
                <h2 class="h5 fw-semibold mb-3">Ítems</h2>
                <form method="post" action="${ctx}/compras" id="formItems">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="id" value="${orden.id}">
                </form>
                <div class="table-responsive">
                    <table class="table align-middle mb-0">
                        <thead>
                            <tr>
                                <th>Insumo</th><th class="text-end">Cantidad</th><th class="text-end">Precio unit.</th>
                                <th class="text-end">Subtotal</th>
                                <c:if test="${not orden.editable}"><th>Proveedor</th><th class="text-end">Recibido</th></c:if>
                                <c:if test="${orden.editable}"><th></th></c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="it" items="${orden.items}">
                                <tr>
                                    <td class="fw-medium">${fn:escapeXml(it.insumo)}</td>
                                    <c:choose>
                                        <c:when test="${orden.editable}">
                                            <td class="text-end">
                                                <div class="input-group input-group-sm justify-content-end">
                                                    <input form="formItems" class="form-control text-end cantidad-orden" type="number" min="0.001" step="0.001"
                                                           name="cantidad_${it.id}" value="${it.cantidadTexto}" aria-label="Cantidad" required>
                                                    <span class="input-group-text">${fn:escapeXml(it.unidad)}</span>
                                                </div>
                                            </td>
                                            <td class="text-end">
                                                <input form="formItems" class="form-control form-control-sm text-end cantidad-orden ms-auto" type="number"
                                                       min="0" step="0.01" name="precio_${it.id}" value="${it.precioTexto}" aria-label="Precio">
                                            </td>
                                        </c:when>
                                        <c:otherwise>
                                            <td class="text-end text-nowrap">${fn:escapeXml(it.cantidadTexto += ' ' += it.unidad)}</td>
                                            <td class="text-end text-nowrap">S/ ${it.precioTexto}</td>
                                        </c:otherwise>
                                    </c:choose>
                                    <td class="text-end text-nowrap">S/ ${it.subtotalTexto}</td>
                                    <c:if test="${not orden.editable}">
                                        <td>
                                            <c:choose>
                                                <c:when test="${it.disponible == true}"><span class="badge rounded-pill bg-success-subtle text-success-emphasis">disponible</span></c:when>
                                                <c:when test="${it.disponible == false}"><span class="badge rounded-pill bg-danger-subtle text-danger-emphasis">sin stock</span></c:when>
                                                <c:otherwise><span class="text-body-tertiary">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">${empty it.cantidadRecibida ? '—' : it.cantidadRecibidaTexto}</td>
                                    </c:if>
                                    <c:if test="${orden.editable}">
                                        <td class="text-end">
                                            <form method="post" action="${ctx}/compras" class="m-0">
                                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                                <input type="hidden" name="accion" value="quitar">
                                                <input type="hidden" name="id" value="${orden.id}">
                                                <input type="hidden" name="item_id" value="${it.id}">
                                                <button type="submit" class="btn btn-sm btn-link text-danger" title="Quitar"><i class="bi bi-x-circle"></i></button>
                                            </form>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty orden.items}">
                                <tr><td colspan="6" class="text-center text-body-secondary py-4">La orden aún no tiene ítems.</td></tr>
                            </c:if>
                        </tbody>
                        <tfoot>
                            <tr><th colspan="3" class="text-end">Total</th><th class="text-end text-nowrap">S/ ${orden.totalTexto}</th><th colspan="2"></th></tr>
                        </tfoot>
                    </table>
                </div>

                <c:if test="${orden.editable}">
                    <div class="d-flex flex-wrap justify-content-between gap-3 mt-3">
                        <form method="post" action="${ctx}/compras" class="d-flex gap-2 flex-grow-1">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="accion" value="agregar">
                            <input type="hidden" name="id" value="${orden.id}">
                            <select class="form-select form-select-sm" name="insumo_id" aria-label="Insumo" required>
                                <option value="">Agregar insumo…</option>
                                <c:forEach var="i" items="${insumos}">
                                    <option value="${i.id}">${fn:escapeXml(i.producto)} (${fn:escapeXml(i.unidad)})</option>
                                </c:forEach>
                            </select>
                            <input class="form-control form-control-sm cantidad-orden" type="number" min="0.001" step="0.001"
                                   name="cantidad" placeholder="Cant." aria-label="Cantidad" required>
                            <button type="submit" class="btn btn-sm btn-outline-primary text-nowrap"><i class="bi bi-plus-lg"></i> Agregar</button>
                        </form>
                        <c:if test="${not empty orden.items}">
                            <button type="submit" form="formItems" class="btn btn-sm btn-outline-secondary rounded-pill px-3">
                                <i class="bi bi-save me-1"></i>Guardar cambios
                            </button>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <%-- QR de la orden para la recepción (HU-08) --%>
        <c:if test="${orden.estado == 'Confirmada' or orden.estado == 'Recibida' or orden.estado == 'Recibida con incidencia'}">
            <div class="card mb-4">
                <div class="card-body text-center">
                    <h2 class="h6 fw-semibold mb-3">Código para la recepción</h2>
                    <c:url var="urlQr" value="/qr"><c:param name="texto" value="${orden.codigo}"/></c:url>
                    <img src="${urlQr}" alt="QR de la orden ${orden.codigo}" class="img-fluid qr-orden">
                    <div class="small text-body-secondary mt-2 font-monospace">${orden.codigo}</div>
                </div>
            </div>
        </c:if>

        <%-- Línea de tiempo (HU-09) --%>
        <div class="card">
            <div class="card-body">
                <h2 class="h6 fw-semibold mb-3">Trazabilidad</h2>
                <ul class="linea-tiempo list-unstyled mb-0">
                    <c:forEach var="ev" items="${orden.eventos}">
                        <li class="linea-tiempo-item">
                            <span class="linea-tiempo-punto bg-${ev.tono}"></span>
                            <div class="fw-semibold small">${fn:escapeXml(ev.estado)}</div>
                            <div class="small text-body-secondary">${ev.fechaTexto}${empty ev.usuario ? '' : ' · '}${fn:escapeXml(ev.usuario)}</div>
                            <c:if test="${not empty ev.detalle}"><div class="small">${fn:escapeXml(ev.detalle)}</div></c:if>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
