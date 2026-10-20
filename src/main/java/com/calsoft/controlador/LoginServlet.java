package com.calsoft.controlador;

import com.calsoft.dao.UsuarioDAO;
import com.calsoft.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;

/** Inicio de sesión: valida credenciales contra la BD (hash bcrypt) y crea la sesión. */
@WebServlet("/login")
public class LoginServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute("usuario") != null) {
            redirigir(req, resp, "/dashboard");
            return;
        }
        mostrar(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String usuario = req.getParameter("usuario") == null ? "" : req.getParameter("usuario").trim();
        String clave = req.getParameter("clave") == null ? "" : req.getParameter("clave").trim();
        req.setAttribute("usuarioIngresado", usuario);

        if (usuario.isEmpty() || clave.isEmpty()) {
            req.setAttribute("error", "Usuario y clave son obligatorios");
            mostrar(req, resp, "login");
            return;
        }

        Usuario u;
        try {
            u = usuarioDAO.autenticar(usuario, clave);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "ERROR LOGIN", e);
            req.setAttribute("error", "No se pudo conectar con la base de datos");
            mostrar(req, resp, "login");
            return;
        }

        // Mismo mensaje si el usuario no existe o la clave no coincide,
        // para no revelar cuáles usuarios existen.
        if (u == null) {
            req.setAttribute("error", "Usuario o contraseña incorrectos");
            mostrar(req, resp, "login");
            return;
        }

        // Nueva sesión para evitar fijación de sesión.
        HttpSession anterior = req.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = req.getSession(true);
        sesion.setAttribute("usuario", u);
        sesion.setAttribute("csrf", nuevoToken());
        redirigir(req, resp, "/dashboard");
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
