package com.calsoft.controlador;

import com.calsoft.dao.AlertaDAO;
import com.calsoft.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades comunes a todos los servlets de la aplicación.
 *
 * Los mensajes de éxito/error se guardan en la sesión ("flash") y la cabecera
 * de las vistas los muestra una sola vez. Así se puede usar el patrón
 * POST → redirect → GET sin perder el mensaje.
 */
public abstract class ServletBase extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected static final Logger LOG = Logger.getLogger("com.calsoft");

    private final AlertaDAO alertaDAO = new AlertaDAO();

    /** Muestra una vista de /WEB-INF/vistas (con el contador de alertas para el menú). */
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        Usuario u = req.getSession(false) == null ? null : (Usuario) req.getSession(false).getAttribute("usuario");
        if (u != null && u.puede("alertas")) {
            try {
                req.setAttribute("alertasPendientes", alertaDAO.contarPendientes());
            } catch (SQLException e) {
                LOG.log(Level.FINE, "No se pudo contar alertas", e);
            }
        }
        req.getRequestDispatcher("/WEB-INF/vistas/" + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    protected void exito(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flashExito", mensaje);
    }

    protected void error(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flashError", mensaje);
    }

    /** Registra el error técnico en el log y muestra un mensaje genérico. */
    protected void errorServidor(HttpServletRequest req, String contexto, Exception e) {
        LOG.log(Level.SEVERE, "ERROR " + contexto, e);
        error(req, "Error del servidor. Revisa la conexión con la base de datos.");
    }

    protected Usuario usuarioActual(HttpServletRequest req) {
        return (Usuario) req.getSession().getAttribute("usuario");
    }

    protected String nombreUsuario(HttpServletRequest req) {
        return usuarioActual(req).getUsuario();
    }

    /** URL del proveedor simulado de esta misma instalación (http://host:puerto/CalSoft/api/...). */
    protected String urlProveedorSimulado(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath() + "/api/proveedor/ordenes";
    }
}
