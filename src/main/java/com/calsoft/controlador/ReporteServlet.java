package com.calsoft.controlador;

import com.calsoft.dao.ReporteDAO;
import com.calsoft.modelo.Numeros;
import com.calsoft.modelo.Prediccion;
import com.calsoft.servicio.PrediccionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Agregados de pedidos, reservas, inventario, consumo, mermas y proyección de compras. */
@WebServlet("/reportes")
public class ReporteServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final PrediccionService prediccionService = new PrediccionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("data", reporteDAO.resumen());
            // Proyección de compras: costo de reponer lo que sugiere la IA para los próximos 7 días.
            BigDecimal proyeccion = BigDecimal.ZERO;
            for (Prediccion p : prediccionService.predecir(7)) {
                proyeccion = proyeccion.add(p.getCostoSugerido());
            }
            req.setAttribute("proyeccionCompras", Numeros.soles(proyeccion));
        } catch (SQLException e) {
            errorServidor(req, "REPORTES", e);
        }
        mostrar(req, resp, "reportes");
    }
}
