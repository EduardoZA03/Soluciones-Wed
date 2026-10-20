package com.calsoft.controlador;

import com.calsoft.dao.ProveedorDAO;
import com.calsoft.modelo.Proveedor;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/** Proveedores y su conexión por API (HU-07). */
@WebServlet("/proveedores")
public class ProveedorServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final String EN_USO = "23503";

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("proveedores", new ArrayList<>());
        try {
            req.setAttribute("proveedores", proveedorDAO.listar());
        } catch (SQLException e) {
            errorServidor(req, "PROVEEDORES", e);
        }
        req.setAttribute("urlSimulador", urlProveedorSimulado(req));
        mostrar(req, resp, "proveedores");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "guardar":
                    Proveedor p = new Proveedor();
                    p.setNombre(f.texto("nombre"));
                    if (p.getNombre().isEmpty()) {
                        throw new ValidacionException("El nombre es obligatorio");
                    }
                    String ruc = f.texto("ruc");
                    if (!ruc.isEmpty() && !ruc.matches("\\d{11}")) {
                        throw new ValidacionException("El RUC debe tener 11 dígitos");
                    }
                    String url = f.texto("api_url");
                    if (!url.isEmpty() && !url.matches("https?://.+")) {
                        throw new ValidacionException("La URL de la API debe empezar con http:// o https://");
                    }
                    p.setRuc(ruc.isEmpty() ? null : ruc);
                    p.setTelefono(f.texto("telefono", null));
                    p.setEmail(f.texto("email", null));
                    p.setApiUrl(url.isEmpty() ? null : url);
                    if (!f.texto("id").isEmpty()) {
                        p.setId(f.id());
                    }
                    // Si se deja la API key en blanco al editar, se conserva la anterior.
                    String apiKey = f.texto("api_key");
                    if (apiKey.isEmpty() && p.getId() != 0) {
                        apiKey = buscarApiKey(p.getId());
                    }
                    p.setApiKey(apiKey.isEmpty() ? null : apiKey);
                    proveedorDAO.guardar(p);
                    exito(req, "Proveedor guardado");
                    break;
                case "eliminar":
                    proveedorDAO.eliminar(f.id());
                    exito(req, "Proveedor eliminado");
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            if (EN_USO.equals(e.getSQLState())) {
                error(req, "No se puede eliminar: el proveedor tiene órdenes de compra registradas");
            } else {
                errorServidor(req, "PROVEEDORES " + accion, e);
            }
        }
        redirigir(req, resp, "/proveedores");
    }

    private String buscarApiKey(int id) throws SQLException {
        for (Proveedor p : proveedorDAO.listar()) {
            if (p.getId() == id) {
                return p.getApiKey() == null ? "" : p.getApiKey();
            }
        }
        return "";
    }
}
