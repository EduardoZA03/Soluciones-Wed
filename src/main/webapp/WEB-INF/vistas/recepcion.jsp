<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Recepción"/>
<c:set var="activo" value="recepcion"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Recepción de insumos</h1>
    <p class="text-body-secondary mb-0">Escanea el QR de la orden con la cámara o con una pistola lectora de códigos</p>
</div>

<div class="row g-4">
    <div class="col-lg-5">
        <div class="card">
            <div class="card-body">
                <%-- Una pistola lectora escribe el código y presiona Enter: funciona como teclado --%>
                <form method="get" action="${ctx}/recepcion" id="formCodigo">
                    <label for="codigo" class="form-label fw-medium">Código de la orden</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text bg-white"><i class="bi bi-upc-scan"></i></span>
                        <input class="form-control font-monospace" id="codigo" name="codigo" value="${fn:escapeXml(codigo)}"
                               placeholder="OC-20261019-XXXXXX" autocomplete="off" autofocus required>
                        <button type="submit" class="btn btn-primary">Buscar</button>
                    </div>
                </form>

                <div class="text-center my-3 small text-body-tertiary">o</div>

                <button type="button" class="btn btn-outline-primary w-100 rounded-pill" id="btnCamara">
                    <i class="bi bi-camera me-1"></i>Escanear con la cámara
                </button>
                <div id="lector" class="lector-qr mt-3 d-none"></div>
                <div id="errorCamara" class="alert alert-warning small mt-3 d-none" role="alert"></div>
            </div>
        </div>
    </div>

    <div class="col-lg-7">
        <c:if test="${not empty aviso}">
            <div class="alert alert-warning"><i class="bi bi-exclamation-triangle me-1"></i>${fn:escapeXml(aviso)}</div>
        </c:if>

        <c:if test="${not empty orden and orden.recibible}">
            <div class="card">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div>
                            <h2 class="h5 fw-semibold mb-1">${orden.codigo}</h2>
                            <div class="small text-body-secondary">${fn:escapeXml(orden.proveedorNombre)} · confirmación
                                ${fn:escapeXml(orden.confirmacionProveedor)} · entrega ${orden.fechaEntregaTexto}</div>
                        </div>
                        <span class="badge rounded-pill bg-${orden.tono}-subtle text-${orden.tono}-emphasis">${orden.estado}</span>
                    </div>

                    <form method="post" action="${ctx}/recepcion">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="id" value="${orden.id}">
                        <input type="hidden" name="codigo" value="${orden.codigo}">
                        <table class="table align-middle">
                            <thead><tr><th>Insumo</th><th class="text-end">Confirmado</th><th class="text-end">Recibido</th></tr></thead>
                            <tbody>
                                <c:forEach var="it" items="${orden.items}">
                                    <c:set var="esperado" value="${it.disponible == false ? '0' : it.cantidadTexto}"/>
                                    <tr>
                                        <td class="fw-medium">${fn:escapeXml(it.insumo)}
                                            <c:if test="${it.disponible == false}"><span class="badge rounded-pill bg-danger-subtle text-danger-emphasis ms-1">sin stock</span></c:if>
                                        </td>
                                        <td class="text-end text-nowrap">${fn:escapeXml(esperado += ' ' += it.unidad)}</td>
                                        <td class="text-end">
                                            <input class="form-control form-control-sm text-end cantidad-orden ms-auto" type="number" min="0" step="0.001"
                                                   name="recibido_${it.id}" value="${esperado}" aria-label="Cantidad recibida de ${fn:escapeXml(it.insumo)}" required>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="mb-3">
                            <label for="observacion" class="form-label">Observación</label>
                            <input class="form-control" id="observacion" name="observacion" maxlength="200"
                                   placeholder="Opcional: estado de la mercadería, faltantes…">
                        </div>
                        <div class="small text-body-secondary mb-3">Valida las cantidades: si algo no coincide con lo confirmado, la orden queda con incidencia para reclamar al proveedor.</div>
                        <button type="submit" class="btn btn-success rounded-pill px-4"><i class="bi bi-box-arrow-in-down me-1"></i>Registrar recepción</button>
                    </form>
                </div>
            </div>
        </c:if>

        <c:if test="${empty orden and empty aviso}">
            <div class="card card-body text-center text-body-secondary py-5">
                <i class="bi bi-qr-code fs-1 mb-2"></i>
                Escanea o escribe el código de una orden confirmada para registrar su llegada.
            </div>
        </c:if>
    </div>
</div>

<script src="${ctx}/webjars/html5-qrcode/2.3.8/html5-qrcode.min.js"></script>
<script src="${ctx}/js/recepcion.js"></script>
<%@ include file="plantilla/pie.jspf" %>
