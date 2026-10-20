package com.calsoft.filtro;

import com.calsoft.modelo.Usuario;
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
 * Además:
 * - comprueba que el rol del usuario pueda entrar al módulo (HU-01);
 * - verifica el token CSRF en cada POST, para que otra página web no
 *   pueda enviar formularios en nombre del usuario logueado.
 *
 * Las rutas /api/ no usan sesión: se autentican con su propia API key.
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
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!usuario.puede(modulo(ruta))) {
            if ("GET".equalsIgnoreCase(req.getMethod())) {
                sesion.setAttribute("flashError", "Tu rol (" + usuario.getRol() + ") no tiene acceso a ese módulo.");
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Tu rol no tiene acceso a este módulo.");
            }
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

    /** "/compras" → "compras"; "/" → "". */
    private static String modulo(String ruta) {
        String sinBarra = ruta.startsWith("/") ? ruta.substring(1) : ruta;
        int fin = sinBarra.indexOf('/');
        return fin < 0 ? sinBarra : sinBarra.substring(0, fin);
    }

    private static boolean esPublica(String ruta) {
        return ruta.equals("/login")
                || ruta.startsWith("/api/")
                || ruta.startsWith("/css/")
                || ruta.startsWith("/js/")
                || ruta.startsWith("/img/")
                || ruta.startsWith("/webjars/")
                || ruta.equals("/favicon.ico");
    }
}
