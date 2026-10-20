<%--
    Tarjeta de estadística reutilizable (Bootstrap card).
    Uso:  <ui:stat etiqueta="Total" valor="${total}" icono="list-check" tono="primary"/>
      icono    → nombre de Bootstrap Icons sin "bi-" (https://icons.getbootstrap.com)
      tono     → color de Bootstrap: primary, success, warning, danger, info
      sub      → texto pequeño opcional debajo del valor
      columnas → clases de columna (por defecto 4 por fila en escritorio, 2 en móvil)
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="etiqueta" required="true" %>
<%@ attribute name="valor" required="true" %>
<%@ attribute name="icono" required="true" %>
<%@ attribute name="tono" required="false" %>
<%@ attribute name="sub" required="false" %>
<%@ attribute name="columnas" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="t" value="${empty tono ? 'primary' : tono}"/>
<div class="${empty columnas ? 'col-6 col-lg-3' : columnas}">
    <div class="card h-100">
        <div class="card-body">
            <div class="d-flex align-items-center justify-content-between">
                <span class="stat-etiqueta">${etiqueta}</span>
                <span class="stat-icono bg-${t}-subtle text-${t}-emphasis"><i class="bi bi-${icono}"></i></span>
            </div>
            <div class="stat-valor">${valor}</div>
            <c:if test="${not empty sub}"><div class="small text-body-secondary mt-1">${sub}</div></c:if>
        </div>
    </div>
</div>
