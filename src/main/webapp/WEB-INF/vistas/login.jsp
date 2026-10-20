<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión · CalSoft</title>
    <link rel="icon" href="${ctx}/img/logo.png">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@500;600;700;800&family=Caveat:wght@600;700&display=swap">
    <link rel="stylesheet" href="${ctx}/css/estilos.css">
</head>
<body>
<main class="login">
    <%-- Hero (solo en pantallas grandes) --%>
    <aside class="hero">
        <div class="blob1"></div>
        <div class="blob2"></div>

        <div class="marca">
            <img src="${ctx}/img/logo.png" alt="">
            <span class="marca-nombre">CalSoft</span>
        </div>

        <div>
            <span class="hero-etiqueta">Sistema de gestión gastronómica</span>
            <h1>Tu restaurante,<br>en <span class="script">orden</span></h1>
            <p>Pedidos, reservas, inventario, cocina y reportes en un solo lugar.
               Simple, rápido y hecho para tu negocio.</p>
            <div class="chips">
                <span>Pedidos</span><span>Reservas</span><span>Inventario</span><span>Cocina</span>
            </div>
        </div>

        <div class="hero-pie">Hecho para microempresas gastronómicas 🍲</div>
    </aside>

    <%-- Formulario --%>
    <div class="panel-login">
        <div class="marca marca-movil">
            <img src="${ctx}/img/logo.png" alt="">
            <span class="marca-nombre">CalSoft</span>
        </div>

        <form method="post" action="${ctx}/login" class="form-pila">
            <div>
                <h2>Iniciar sesión</h2>
                <p class="sub">Ingresa al panel de gestión</p>
            </div>

            <div class="campo">
                <label for="usuario">Usuario</label>
                <input class="input" id="usuario" name="usuario" placeholder="Ingrese su usuario"
                       value="${fn:escapeXml(usuarioIngresado)}" autofocus required>
            </div>
            <div class="campo">
                <label for="clave">Contraseña</label>
                <input class="input" id="clave" name="clave" type="password" placeholder="Ingrese su contraseña" required>
            </div>

            <c:if test="${not empty error}">
                <div class="alerta alerta-error" style="margin:12px 0 0">${fn:escapeXml(error)}</div>
            </c:if>

            <button type="submit" class="btn btn-primary btn-lg btn-full">Ingresar</button>

            <p class="demo">Demo: <b>admin</b> / <b>1234</b></p>
        </form>
    </div>
</main>
</body>
</html>
