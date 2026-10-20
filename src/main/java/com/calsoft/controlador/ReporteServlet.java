package com.calsoft.controlador;

import com.calsoft.dao.ReporteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Agregados de pedidos, reservas, inventario y usuarios. */
@WebServlet("/reportes")
public class ReporteServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("data", reporteDAO.resumen());
        } catch (SQLException e) {
            errorServidor(req, "REPORTES", e);
        }
        mostrar(req, resp, "reportes");
    }
}
