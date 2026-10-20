package com.calsoft.controlador;

import com.calsoft.dao.UsuarioDAO;
import com.calsoft.modelo.Usuario;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Alta de usuarios (clave hasheada), cambio de rol y reseteo de clave. */
@WebServlet("/usuarios")
public class UsuarioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            usuarios = usuarioDAO.listar();
        } catch (SQLException e) {
            errorServidor(req, "LISTAR USUARIOS", e);
        }
        req.setAttribute("usuarios", usuarios);
        req.setAttribute("roles", Usuario.ROLES);
        mostrar(req, resp, "usuarios");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "crear":
                    crear(req, f);
                    break;
                case "rol":
                    cambiarRol(req, f);
                    break;
                case "clave":
                    String clave = validarClave(f.texto("clave"));
                    if (usuarioDAO.actualizarClave(f.id(), clave)) {
                        exito(req, "Clave actualizada");
                    } else {
                        error(req, "Usuario no encontrado");
                    }
                    break;
                case "eliminar":
                    int id = f.id();
                    // Evita que un usuario borre su propia cuenta (se quedaría sin acceso).
                    if (id == usuarioActual(req).getId()) {
                        throw new ValidacionException("No puedes eliminar tu propia cuenta");
                    }
                    if (usuarioDAO.eliminar(id)) {
                        exito(req, "Usuario eliminado");
                    } else {
                        error(req, "Usuario no encontrado");
                    }
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "USUARIOS " + accion, e);
        }
        redirigir(req, resp, "/usuarios");
    }

    private void crear(HttpServletRequest req, Formulario f) throws ValidacionException, SQLException {
        String usuario = f.texto("usuario");
        String clave = f.texto("clave");
        String rol = f.texto("rol", "Empleado");
        if (usuario.isEmpty() || clave.isEmpty()) {
            throw new ValidacionException("Usuario y clave son obligatorios");
        }
        validarClave(clave);
        validarRol(rol);
        try {
            usuarioDAO.crear(usuario, clave, rol);
            exito(req, "Usuario creado");
        } catch (SQLException e) {
            if (UsuarioDAO.DUPLICADO.equals(e.getSQLState())) {
                throw new ValidacionException("Ese nombre de usuario ya existe");
            }
            throw e;
        }
    }

    private void cambiarRol(HttpServletRequest req, Formulario f) throws ValidacionException, SQLException {
        int id = f.id();
        String rol = f.texto("rol");
        validarRol(rol);
        if (!usuarioDAO.actualizarRol(id, rol)) {
            error(req, "Usuario no encontrado");
            return;
        }
        // Si el usuario cambió su propio rol, se refleja en la barra lateral.
        Usuario actual = usuarioActual(req);
        if (actual.getId() == id) {
            actual.setRol(rol);
        }
    }

    private static String validarClave(String clave) throws ValidacionException {
        if (clave.length() < 4) {
            throw new ValidacionException("La clave debe tener al menos 4 caracteres");
        }
        return clave;
    }

    private static void validarRol(String rol) throws ValidacionException {
        if (!Usuario.ROLES.contains(rol)) {
            throw new ValidacionException("Rol inválido");
        }
    }
}
