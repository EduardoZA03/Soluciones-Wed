package com.calsoft.controlador;

import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.OrdenCompraDAO;
import com.calsoft.dao.ProveedorDAO;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenItem;
import com.calsoft.servicio.OrdenService;
import com.calsoft.util.Formulario;
import com.calsoft.util.Permisos;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Órdenes de compra (HU-06, HU-07, HU-09).
 *   GET /compras          → listado (filtrable por estado)
 *   GET /compras?id=N     → detalle: ítems, QR y línea de tiempo
 */
@WebServlet("/compras")
public class CompraServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final OrdenService ordenService = new OrdenService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Formulario f = new Formulario(req);
        try {
            if (!f.texto("id").isEmpty()) {
                OrdenCompra orden = ordenService.buscar(f.id());
                if (orden == null) {
                    error(req, "Orden no encontrada");
                    redirigir(req, resp, "/compras");
                    return;
                }
                req.setAttribute("orden", orden);
                req.setAttribute("insumos", inventarioDAO.listar());
                mostrar(req, resp, "orden");
                return;
            }
            String estado = f.texto("estado");
            req.setAttribute("ordenes", ordenDAO.listar(estado.isEmpty() ? null : estado));
            req.setAttribute("proveedores", proveedorDAO.listar());
            req.setAttribute("filtroEstado", estado);
        } catch (ValidacionException e) {
            error(req, e.getMessage());
            req.setAttribute("ordenes", new ArrayList<>());
        } catch (SQLException e) {
            errorServidor(req, "COMPRAS", e);
            req.setAttribute("ordenes", new ArrayList<>());
        }
        req.setAttribute("estados", OrdenCompra.ESTADOS);
        mostrar(req, resp, "compras");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        String usuario = nombreUsuario(req);
        Integer ordenId = null;
        try {
            switch (accion) {
                case "nueva":
                    ordenId = ordenService.crearVacia(f.entero("proveedor_id", 1, "Elige un proveedor"), usuario);
                    exito(req, "Orden creada en borrador. Agrega los insumos.");
                    break;
                case "agregar":
                    ordenId = f.id();
                    ordenService.agregarItem(ordenId, f.entero("insumo_id", 1, "Elige un insumo"),
                            f.positivo("cantidad", "La cantidad debe ser mayor que cero"));
                    break;
                case "actualizar":
                    ordenId = f.id();
                    ordenService.actualizarItems(ordenId, leerItems(req, f, ordenId));
                    exito(req, "Cambios guardados");
                    break;
                case "quitar":
                    ordenId = f.id();
                    ordenService.quitarItem(ordenId, f.entero("item_id", 1, "Ítem inválido"));
                    break;
                case "aprobar":
                    ordenId = f.id();
                    if (!Permisos.puedeAprobar(usuarioActual(req).getRol())) {
                        throw new ValidacionException("Solo el Administrador puede aprobar órdenes de compra");
                    }
                    informar(req, ordenService.aprobarYEnviar(ordenId, usuario, urlProveedorSimulado(req)));
                    break;
                case "reenviar":
                    ordenId = f.id();
                    informar(req, ordenService.enviar(ordenId, usuario, urlProveedorSimulado(req)));
                    break;
                case "cancelar":
                    ordenId = f.id();
                    ordenService.cancelar(ordenId, usuario);
                    exito(req, "Orden cancelada");
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "COMPRAS " + accion, e);
        }
        redirigir(req, resp, ordenId == null ? "/compras" : "/compras?id=" + ordenId);
    }

    private void informar(HttpServletRequest req, OrdenService.ResultadoEnvio r) {
        if (r.exito()) {
            exito(req, r.mensaje());
        } else {
            error(req, r.mensaje());
        }
    }

    /** Lee los campos cantidad_ID y precio_ID del formulario de edición. */
    private Map<Integer, BigDecimal[]> leerItems(HttpServletRequest req, Formulario f, int ordenId)
            throws SQLException, ValidacionException {
        Map<Integer, BigDecimal[]> items = new LinkedHashMap<>();
        OrdenCompra orden = ordenService.buscar(ordenId);
        if (orden == null) {
            throw new ValidacionException("Orden no encontrada");
        }
        for (OrdenItem it : orden.getItems()) {
            if (req.getParameter("cantidad_" + it.getId()) != null) {
                items.put(it.getId(), new BigDecimal[] {
                    f.positivo("cantidad_" + it.getId(), "Cantidad inválida para " + it.getInsumo()),
                    f.decimal("precio_" + it.getId(), "Precio inválido para " + it.getInsumo())
                });
            }
        }
        return items;
    }
}
