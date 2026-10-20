package com.calsoft.controlador;

import com.calsoft.dao.AlertaDAO;
import com.calsoft.dao.PedidoDAO;
import com.calsoft.dao.ReporteDAO;
import com.calsoft.dao.ReservaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Inicio: resumen general, pedidos recientes y próximas reservas. */
@WebServlet("/dashboard")
public class DashboardServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            alertaDAO.evaluarTodo();
            req.setAttribute("stats", reporteDAO.resumen());
            req.setAttribute("pedidosRecientes", pedidoDAO.listarRecientes(5));
            req.setAttribute("reservasProximas", reservaDAO.listarProximas(5));
        } catch (SQLException e) {
            errorServidor(req, "DASHBOARD", e);
        }
        mostrar(req, resp, "dashboard");
    }
}
