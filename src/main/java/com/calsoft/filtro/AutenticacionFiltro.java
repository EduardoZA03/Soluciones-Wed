package com.calsoft.filtro;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Protege todas las páginas: si no hay sesión iniciada redirige al login.
 * Reemplaza al middleware requireAuth (JWT) y al ProtectedRoute del original.
 *
 * Además verifica el token CSRF en cada POST, para que otra página web no
 * pueda enviar formularios en nombre del usuario logueado.
 */
@WebFilter("/*")
public class AutenticacionFiltro extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String ruta = req.getRequestURI().substring(req.getContextPath().length());

        if (esPublica(ruta)) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession sesion = req.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            Object esperado = sesion.getAttribute("csrf");
            String recibido = req.getParameter("csrf");
            if (esperado == null || !esperado.equals(recibido)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido. Recarga la página.");
                return;
            }
        }

        // Evita que el botón "atrás" muestre páginas privadas después de cerrar sesión.
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, resp);
    }

    private static boolean esPublica(String ruta) {
        return ruta.equals("/login")
                || ruta.startsWith("/css/")
                || ruta.startsWith("/js/")
                || ruta.startsWith("/img/")
                || ruta.equals("/favicon.ico");
    }
}
