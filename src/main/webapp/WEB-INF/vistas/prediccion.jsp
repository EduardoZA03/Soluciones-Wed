<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Predicción IA"/>
<c:set var="activo" value="prediccion"/>
<%@ include file="plantilla/cabecera.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
    <div>
        <h1 class="h2 fw-bold mb-1">Predicción de reposición <span class="badge rounded-pill bg-primary-subtle text-primary-emphasis fs-6 align-middle"><i class="bi bi-stars"></i> IA</span></h1>
        <p class="text-body-secondary mb-0">Consumo esperado según el histórico y la estacionalidad semanal</p>
    </div>
    <form method="get" action="${ctx}/prediccion" class="d-flex align-items-center gap-2">
        <label for="dias" class="text-body-secondary text-nowrap">Periodo:</label>
        <select class="form-select" id="dias" name="dias" data-autoenvio>
            <c:forEach var="h" items="${horizontes}">
                <option value="${h}" ${h == horizonte ? 'selected' : ''}>Próximos ${h} días</option>
            </c:forEach>
        </select>
    </form>
</div>

<div class="row g-3 mb-4">
    <ui:stat etiqueta="Insumos a reponer" valor="${aReponer}" icono="cart-plus" tono="warning" columnas="col-6 col-lg-4"/>
    <ui:stat etiqueta="Costo estimado" valor="S/ ${costoSugerido}" icono="wallet2" tono="success" columnas="col-6 col-lg-4"/>
    <ui:stat etiqueta="Periodo" valor="${horizonte} días" icono="calendar-range" tono="info" columnas="col-12 col-lg-4"/>
</div>

<form method="post" action="${ctx}/prediccion">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <input type="hidden" name="dias" value="${horizonte}">
    <div class="card mb-3">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th class="ps-4"><span class="visually-hidden">Incluir</span></th>
                        <th>Insumo</th><th class="text-end">Stock</th><th class="text-end">Consumo diario</th>
                        <th class="text-end">Estimado ${horizonte} días</th><th class="text-end">Cobertura</th>
                        <th>Tendencia</th><th>Confianza</th><th class="pe-4">Comprar</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${predicciones}">
                        <tr class="${p.conviene ? '' : 'text-body-secondary'}">
                            <td class="ps-4">
                                <input class="form-check-input" type="checkbox" name="insumo" value="${p.insumo.id}"
                                       ${p.conviene ? 'checked' : ''} aria-label="Incluir ${fn:escapeXml(p.insumo.producto)}">
                            </td>
                            <td class="fw-medium">${fn:escapeXml(p.insumo.producto)}
                                <div class="small text-body-tertiary fw-normal">${p.diasConDatos} días con datos</div>
                            </td>
                            <td class="text-end text-nowrap">${fn:escapeXml(p.insumo.stockTexto += ' ' += p.insumo.unidad)}</td>
                            <td class="text-end">${p.consumoDiarioTexto}</td>
                            <td class="text-end fw-medium">${p.consumoEstimadoTexto}</td>
                            <td class="text-end text-nowrap">
                                <c:choose>
                                    <c:when test="${empty p.diasCobertura}">—</c:when>
                                    <c:otherwise><span class="${p.diasCobertura < horizonte ? 'text-danger fw-semibold' : ''}">${p.diasCobertura} días</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="small">${p.tendencia}</td>
                            <td><span class="badge rounded-pill bg-${p.tonoConfianza}-subtle text-${p.tonoConfianza}-emphasis">${p.confianza}</span></td>
                            <td class="pe-4">
                                <div class="input-group input-group-sm flex-nowrap">
                                    <input class="form-control text-end cantidad-orden" type="number" min="0" step="0.001"
                                           name="cantidad_${p.insumo.id}" value="${p.sugeridoTexto}" aria-label="Cantidad a comprar">
                                    <span class="input-group-text">${fn:escapeXml(p.insumo.unidad)}</span>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty predicciones}">
                        <tr><td colspan="9" class="text-center text-body-secondary py-5">No hay insumos.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <span class="small text-body-secondary">Acepta las cantidades sugeridas o ajústalas; se crea una orden en borrador por proveedor.</span>
        <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-cart-plus me-1"></i>Generar órdenes de compra</button>
    </div>
</form>

<div class="card">
    <div class="card-body small">
        <h2 class="h6 fw-semibold"><i class="bi bi-cpu me-1"></i>¿Cómo predice el modelo?</h2>
        <ol class="mb-2">
            <li><b>Aprende la estacionalidad semanal</b> de las salidas de las últimas 8 semanas: cuánto se consume cada día de la semana respecto al promedio (los fines de semana suelen ser más altos).</li>
            <li><b>Estima el nivel de consumo</b> con suavizado exponencial: da más peso a los días recientes (α = 0.3).</li>
            <li><b>Pronostica</b> el periodo elegido: nivel × factor de cada día.</li>
            <li><b>Sugiere comprar</b>: consumo estimado + stock mínimo − stock actual (redondeado hacia arriba).</li>
        </ol>
        <p class="mb-0 text-body-secondary">La <b>confianza</b> sale del error que tuvo el modelo al pronosticar el pasado (Alta &lt; 25 %, Media &lt; 45 %). La <b>cobertura</b> es para cuántos días alcanza el stock actual.</p>
    </div>
</div>

<%@ include file="plantilla/pie.jspf" %>
