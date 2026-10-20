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
    <link rel="stylesheet" href="${ctx}/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/webjars/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@500;600;700;800&family=Caveat:wght@600;700&display=swap">
    <link rel="stylesheet" href="${ctx}/css/estilos.css">
</head>
<body>
<main class="container-fluid p-0 p-lg-4">
    <div class="row g-0 g-lg-4 min-vh-100">

        <%-- Hero (solo en pantallas grandes) --%>
        <div class="col-lg-7 d-none d-lg-flex">
            <aside class="hero w-100 d-flex flex-column justify-content-between">
                <div class="blob blob-1"></div>
                <div class="blob blob-2"></div>

                <div class="marca">
                    <img src="${ctx}/img/logo.png" alt="">
                    <span class="marca-nombre fs-4">CalSoft</span>
                </div>

                <div>
                    <span class="hero-etiqueta">Sistema de gestión gastronómica</span>
                    <h1 class="hero-titulo my-4">Tu restaurante,<br>en <span class="script">orden</span></h1>
                    <p class="text-secondary fs-6 mb-4 col-10">
                        Pedidos, reservas, inventario, cocina y reportes en un solo lugar.
                        Simple, rápido y hecho para tu negocio.
                    </p>
                    <div class="d-flex flex-wrap gap-2">
                        <span class="chip-hero">Pedidos</span>
                        <span class="chip-hero">Reservas</span>
                        <span class="chip-hero">Inventario</span>
                        <span class="chip-hero">Cocina</span>
                    </div>
                </div>

                <div class="small fw-medium text-body-secondary">Hecho para microempresas gastronómicas 🍲</div>
            </aside>
        </div>

        <%-- Formulario --%>
        <div class="col-lg-5 d-flex">
            <div class="card panel-login w-100 border-0 d-flex align-items-center justify-content-center p-4 p-lg-5">
                <div class="marca d-lg-none mb-4">
                    <img src="${ctx}/img/logo.png" alt="">
                    <span class="marca-nombre fs-4">CalSoft</span>
                </div>

                <form method="post" action="${ctx}/login" class="form-login w-100">
                    <h2 class="h3 fw-bold mb-1">Iniciar sesión</h2>
                    <p class="text-body-secondary mb-4">Ingresa al panel de gestión</p>

                    <div class="mb-3">
                        <label for="usuario" class="form-label fw-medium">Usuario</label>
                        <input class="form-control form-control-lg" id="usuario" name="usuario"
                               placeholder="Ingrese su usuario" value="${fn:escapeXml(usuarioIngresado)}" autofocus required>
                    </div>
                    <div class="mb-3">
                        <label for="clave" class="form-label fw-medium">Contraseña</label>
                        <input class="form-control form-control-lg" id="clave" name="clave" type="password"
                               placeholder="Ingrese su contraseña" required>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger py-2" role="alert">
                            <i class="bi bi-exclamation-circle me-1"></i>${fn:escapeXml(error)}
                        </div>
                    </c:if>

                    <button type="submit" class="btn btn-primary btn-lg rounded-pill w-100">Ingresar</button>

                    <p class="text-center small text-body-tertiary mt-3 mb-0">
                        Demo: <b class="text-body-secondary">admin</b> / <b class="text-body-secondary">1234</b>
                    </p>
                </form>
            </div>
        </div>
    </div>
</main>
</body>
</html>
