package com.calsoft.controlador;

import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.PlatoDAO;
import com.calsoft.modelo.Plato;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/** Carta del restaurante y recetas: qué insumos consume cada plato (HU-03). */
@WebServlet("/carta")
public class PlatoServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final String DUPLICADO = "23505";

    private final PlatoDAO platoDAO = new PlatoDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("platos", new ArrayList<>());
        try {
            req.setAttribute("platos", platoDAO.listar(false));
            req.setAttribute("insumos", inventarioDAO.listar());
        } catch (SQLException e) {
            errorServidor(req, "CARTA", e);
        }
        mostrar(req, resp, "carta");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "guardar":
                    Plato p = new Plato();
                    p.setNombre(f.texto("nombre"));
                    if (p.getNombre().isEmpty()) {
                        throw new ValidacionException("El nombre del plato es obligatorio");
                    }
                    p.setPrecio(f.decimal("precio", "El precio debe ser un número >= 0"));
                    p.setActivo(req.getParameter("activo") != null);
                    if (!f.texto("id").isEmpty()) {
                        p.setId(f.id());
                    }
                    platoDAO.guardar(p);
                    exito(req, "Plato guardado");
                    break;
                case "eliminar":
                    platoDAO.eliminar(f.id());
                    exito(req, "Plato eliminado");
                    break;
                case "ingrediente":
                    platoDAO.guardarIngrediente(f.id(), f.entero("insumo_id", 1, "Elige un insumo"),
                            f.positivo("cantidad", "La cantidad por porción debe ser mayor que cero"));
                    exito(req, "Receta actualizada");
                    break;
                case "quitar":
                    platoDAO.quitarIngrediente(f.entero("receta_id", 1, "Ingrediente inválido"));
                    exito(req, "Ingrediente quitado");
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            if (DUPLICADO.equals(e.getSQLState())) {
                error(req, "Ya existe un plato con ese nombre");
            } else {
                errorServidor(req, "CARTA " + accion, e);
            }
        }
        redirigir(req, resp, "/carta");
    }
}
