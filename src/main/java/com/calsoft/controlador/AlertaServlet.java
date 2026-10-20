package com.calsoft.controlador;

import com.calsoft.dao.AlertaDAO;
import com.calsoft.servicio.OrdenService;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Alertas de stock mínimo y vencimiento (HU-04). Desde aquí se marca una
 * alerta como atendida o se genera la orden de compra (HU-06).
 */
@WebServlet("/alertas")
public class AlertaServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final OrdenService ordenService = new OrdenService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pendientes", new ArrayList<>());
        req.setAttribute("atendidas", new ArrayList<>());
        try {
            alertaDAO.evaluarTodo();
            req.setAttribute("pendientes", alertaDAO.listar("Pendiente", 200));
            req.setAttribute("atendidas", alertaDAO.listar("Atendida", 30));
        } catch (SQLException e) {
            errorServidor(req, "ALERTAS", e);
        }
        mostrar(req, resp, "alertas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "atender":
                    if (alertaDAO.atender(f.id(), nombreUsuario(req))) {
                        exito(req, "Alerta marcada como atendida");
                    } else {
                        error(req, "La alerta ya estaba atendida");
                    }
                    break;
                case "orden":
                    int ordenId = ordenService.crearDesdeAlerta(f.id(), nombreUsuario(req));
                    exito(req, "Orden de compra creada en borrador. Revisa las cantidades y apruébala.");
                    redirigir(req, resp, "/compras?id=" + ordenId);
                    return;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "ALERTAS " + accion, e);
        }
        redirigir(req, resp, "/alertas");
    }
}
