package com.calsoft.controlador;

import com.calsoft.dao.PedidoDAO;
import com.calsoft.dao.PlatoDAO;
import com.calsoft.modelo.Pedido;
import com.calsoft.servicio.InventarioService;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * POS: registro de pedidos (ventas), cambio de estado y resumen.
 * Al registrar un pedido se descuentan sus insumos según la receta (HU-03).
 */
@WebServlet("/pedidos")
public class PedidoServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PlatoDAO platoDAO = new PlatoDAO();
    private final InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String filtro = req.getParameter("mesero");
        if (filtro == null || filtro.isBlank()) {
            filtro = "Todos";
        }

        List<Pedido> pedidos = new ArrayList<>();
        try {
            pedidos = pedidoDAO.listar();
            req.setAttribute("platos", platoDAO.listar(true));
        } catch (SQLException e) {
            errorServidor(req, "LISTAR PEDIDOS", e);
        }

        TreeSet<String> meseros = new TreeSet<>();
        int pendientes = 0;
        int enCocina = 0;
        int entregados = 0;
        List<Pedido> visibles = new ArrayList<>();
        for (Pedido p : pedidos) {
            meseros.add(p.getMesero());
            switch (p.getEstado()) {
                case "Pendiente":
                    pendientes++;
                    break;
                case "En cocina":
                    enCocina++;
                    break;
                default:
                    entregados++;
            }
            if (filtro.equals("Todos") || filtro.equals(p.getMesero())) {
                visibles.add(p);
            }
        }

        req.setAttribute("pedidos", visibles);
        req.setAttribute("meseros", meseros);
        req.setAttribute("filtroMesero", filtro);
        req.setAttribute("total", pedidos.size());
        req.setAttribute("pendientes", pendientes);
        req.setAttribute("enCocina", enCocina);
        req.setAttribute("entregados", entregados);
        req.setAttribute("estados", Pedido.ESTADOS);
        mostrar(req, resp, "pedidos");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "crear":
                    String resumen = crear(req, f);
                    exito(req, "Pedido registrado. " + resumen);
                    break;
                case "cambiar":
                    cambiar(req, f.id());
                    break;
                case "eliminar":
                    if (pedidoDAO.eliminar(f.id())) {
                        exito(req, "Pedido eliminado");
                    } else {
                        error(req, "Pedido no encontrado");
                    }
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "PEDIDOS " + accion, e);
        }

        // Mantiene el filtro de mesero que estaba activo.
        String mesero = f.texto("filtroMesero");
        String ruta = "/pedidos";
        if (!mesero.isEmpty() && !mesero.equals("Todos")) {
            ruta += "?mesero=" + URLEncoder.encode(mesero, StandardCharsets.UTF_8);
        }
        redirigir(req, resp, ruta);
    }

    private String crear(HttpServletRequest req, Formulario f) throws ValidacionException, SQLException {
        Pedido p = new Pedido();
        p.setCliente(f.texto("cliente"));
        p.setMesero(f.texto("mesero"));
        if (p.getCliente().isEmpty() || p.getMesero().isEmpty()) {
            throw new ValidacionException("Cliente y mesero son obligatorios");
        }
        p.setPlatoId(f.entero("plato_id", 1, "Elige un plato de la carta"));
        p.setCantidad(f.entero("cantidad", 1, "La cantidad debe ser un entero positivo"));
        p.setMesa(f.entero("mesa", 1, "La mesa debe ser un entero positivo"));
        String estado = f.texto("estado", "Pendiente");
        if (!Pedido.ESTADOS.contains(estado)) {
            throw new ValidacionException("Estado inválido");
        }
        p.setEstado(estado);
        return inventarioService.registrarPedido(p, nombreUsuario(req));
    }

    /** Pasa el pedido al siguiente estado del ciclo. */
    private void cambiar(HttpServletRequest req, int id) throws SQLException {
        String actual = pedidoDAO.obtenerEstado(id);
        if (actual == null) {
            error(req, "Pedido no encontrado");
            return;
        }
        pedidoDAO.actualizarEstado(id, Pedido.siguienteEstado(actual));
    }
}
