package com.calsoft.controlador;

import com.calsoft.dao.PedidoDAO;
import com.calsoft.modelo.Pedido;
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

/** Vista de cocina: reutiliza los pedidos activos (Pendiente / En cocina). */
@WebServlet("/cocina")
public class CocinaServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Pedido> pendientes = new ArrayList<>();
        List<Pedido> enCocina = new ArrayList<>();
        try {
            for (Pedido p : pedidoDAO.listar()) {
                if ("Pendiente".equals(p.getEstado())) {
                    pendientes.add(p);
                } else if ("En cocina".equals(p.getEstado())) {
                    enCocina.add(p);
                }
            }
        } catch (SQLException e) {
            errorServidor(req, "COCINA", e);
        }
        req.setAttribute("pendientes", pendientes);
        req.setAttribute("enCocina", enCocina);
        // El navegador recarga la página cada 10 segundos.
        resp.setHeader("Refresh", "10");
        mostrar(req, resp, "cocina");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        try {
            String estado = f.texto("estado");
            if (!estado.equals("En cocina") && !estado.equals("Entregado")) {
                throw new ValidacionException("Estado inválido");
            }
            if (!pedidoDAO.actualizarEstado(f.id(), estado)) {
                error(req, "Pedido no encontrado");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "COCINA AVANZAR", e);
        }
        redirigir(req, resp, "/cocina");
    }
}
