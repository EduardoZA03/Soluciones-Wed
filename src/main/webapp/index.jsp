<%-- Página de entrada: el filtro de autenticación decide si va al login o al inicio. --%>
<% response.sendRedirect(request.getContextPath() + "/dashboard"); %>
