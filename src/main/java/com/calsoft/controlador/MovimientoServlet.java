package com.calsoft.controlador;

import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.MovimientoDAO;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/** Kardex: historial de movimientos de stock, de todos los insumos o de uno. */
@WebServlet("/movimientos")
public class MovimientoServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer insumo = null;
        try {
            insumo = new Formulario(req).idOpcional("insumo");
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        }
        req.setAttribute("movimientos", new ArrayList<>());
        try {
            req.setAttribute("movimientos", movimientoDAO.listar(insumo, 300));
            req.setAttribute("insumos", inventarioDAO.listar());
        } catch (SQLException e) {
            errorServidor(req, "KARDEX", e);
        }
        req.setAttribute("filtroInsumo", insumo);
        mostrar(req, resp, "movimientos");
    }
}
