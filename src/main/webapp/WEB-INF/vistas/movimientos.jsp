<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Kardex"/>
<c:set var="activo" value="movimientos"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Kardex</h1>
        <p class="text-body-secondary mb-0">Historial de entradas, salidas (ventas POS), mermas y ajustes</p>
    </div>
    <form method="get" action="${ctx}/movimientos" class="d-flex gap-2">
        <select class="form-select" name="insumo" data-autoenvio aria-label="Filtrar por insumo">
            <option value="">Todos los insumos</option>
            <c:forEach var="i" items="${insumos}">
                <option value="${i.id}" ${i.id == filtroInsumo ? 'selected' : ''}>${fn:escapeXml(i.producto)}</option>
            </c:forEach>
        </select>
    </form>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 small">
            <thead>
                <tr>
                    <th class="ps-4">Fecha</th><th>Insumo</th><th>Tipo</th><th class="text-end">Cantidad</th>
                    <th class="text-end">Stock</th><th>Motivo</th><th>Referencia</th><th class="pe-4">Usuario</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="m" items="${movimientos}">
                    <tr>
                        <td class="ps-4 text-nowrap">${m.fechaTexto}</td>
                        <td class="fw-medium">${fn:escapeXml(m.insumo)}</td>
                        <td><span class="badge rounded-pill bg-${m.tono}-subtle text-${m.tono}-emphasis">${m.tipo}</span></td>
                        <td class="text-end text-nowrap">${fn:escapeXml(m.cantidadTexto += ' ' += m.unidad)}</td>
                        <td class="text-end">${empty m.stockResultante ? '—' : m.stockResultanteTexto}</td>
                        <td>${fn:escapeXml(m.motivo)}</td>
                        <td class="text-nowrap">${fn:escapeXml(m.referencia)}</td>
                        <td class="pe-4">${fn:escapeXml(m.usuario)}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty movimientos}">
                    <tr><td colspan="8" class="text-center text-body-secondary py-5">No hay movimientos.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
