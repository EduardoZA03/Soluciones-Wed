package com.calsoft.api;

import com.calsoft.dao.ProveedorDAO;
import com.calsoft.integracion.RespuestaProveedor;
import com.calsoft.integracion.SolicitudOrden;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proveedor SIMULADO: imita el sistema externo de un proveedor para poder
 * probar la interoperabilidad sin depender de un tercero.
 *
 *   POST /api/proveedor/ordenes   (cabecera X-Api-Key)
 *
 * Reglas del simulador:
 *  - API key inválida → HTTP 401.
 *  - Cada ítem está disponible si la cantidad pedida es de hasta 500.
 *  - Entrega al día siguiente (si cae domingo, el lunes).
 *
 * Para usar un proveedor real basta con poner su URL en el módulo Proveedores:
 * CalSoft le enviará el mismo JSON.
 */
@WebServlet("/api/proveedor/ordenes")
public class ProveedorSimuladoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ProveedorSimuladoServlet.class.getName());
    private static final Gson GSON = new Gson();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BigDecimal MAXIMO_DISPONIBLE = new BigDecimal("500");

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    /** Descripción de la API, útil para probarla desde el navegador. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        responder(resp, 200, Map.of(
                "servicio", "Proveedor simulado CalSoft",
                "uso", "POST con JSON {codigo, comprador, items:[{item_id, insumo, unidad, cantidad}]}",
                "autenticacion", "Cabecera X-Api-Key con la API key del proveedor"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String apiKey = req.getHeader("X-Api-Key");
            if (apiKey == null || apiKey.isBlank() || !proveedorDAO.existeApiKey(apiKey)) {
                responder(resp, 401, Map.of("error", "API key inválida"));
                return;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Proveedor simulado: error de BD", e);
            responder(resp, 500, Map.of("error", "Error interno del proveedor"));
            return;
        }

        SolicitudOrden solicitud;
        try {
            solicitud = GSON.fromJson(req.getReader(), SolicitudOrden.class);
        } catch (JsonParseException e) {
            responder(resp, 400, Map.of("error", "JSON inválido"));
            return;
        }
        if (solicitud == null || solicitud.items == null || solicitud.items.isEmpty()) {
            responder(resp, 400, Map.of("error", "La orden no tiene ítems"));
            return;
        }

        RespuestaProveedor r = new RespuestaProveedor();
        int disponibles = 0;
        for (SolicitudOrden.Item item : solicitud.items) {
            RespuestaProveedor.Item ri = new RespuestaProveedor.Item();
            ri.itemId = item.itemId;
            ri.disponible = item.cantidad != null && item.cantidad.compareTo(MAXIMO_DISPONIBLE) <= 0;
            ri.cantidadConfirmada = ri.disponible ? item.cantidad : BigDecimal.ZERO;
            r.items.add(ri);
            if (ri.disponible) {
                disponibles++;
            }
        }

        r.aceptada = disponibles > 0;
        if (r.aceptada) {
            LocalDate entrega = LocalDate.now().plusDays(1);
            if (entrega.getDayOfWeek() == DayOfWeek.SUNDAY) {
                entrega = entrega.plusDays(1);
            }
            r.confirmacion = "PRV-" + (100000 + RANDOM.nextInt(900000));
            r.fechaEntrega = entrega.toString();
            r.mensaje = disponibles == solicitud.items.size()
                    ? "Pedido confirmado"
                    : "Pedido confirmado parcialmente: " + (solicitud.items.size() - disponibles) + " ítem(s) sin stock";
        } else {
            r.mensaje = "Sin stock para los ítems solicitados";
        }
        responder(resp, 200, r);
    }

    private static void responder(HttpServletResponse resp, int estado, Object cuerpo) throws IOException {
        resp.setStatus(estado);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(cuerpo));
    }
}
