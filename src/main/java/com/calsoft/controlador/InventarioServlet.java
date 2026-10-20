package com.calsoft.controlador;

import com.calsoft.dao.AlertaDAO;
import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.ProveedorDAO;
import com.calsoft.modelo.Movimiento;
import com.calsoft.modelo.Numeros;
import com.calsoft.modelo.Producto;
import com.calsoft.servicio.InventarioService;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Insumos, stock, vencimiento y movimientos (HU-02).
 * Permite buscar por nombre y filtrar por categoría y estado.
 */
@WebServlet("/inventario")
public class InventarioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final List<String> ESTADOS = List.of("Bajo stock", "Por vencer", "Vencido", "OK");

    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Formulario f = new Formulario(req);
        String q = f.texto("q");
        String categoria = f.texto("categoria");
        String estado = f.texto("estado");

        List<Producto> todos = new ArrayList<>();
        try {
            alertaDAO.evaluarTodo();
            todos = inventarioDAO.listar();
            req.setAttribute("proveedores", proveedorDAO.listar());
        } catch (SQLException e) {
            errorServidor(req, "LISTAR INVENTARIO", e);
        }

        TreeSet<String> categorias = new TreeSet<>();
        List<Producto> visibles = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;
        int bajoStock = 0;
        int porVencer = 0;
        String busqueda = q.toLowerCase(Locale.ROOT);
        for (Producto p : todos) {
            categorias.add(p.getCategoria());
            valorTotal = valorTotal.add(p.getPrecio().multiply(p.getStock()));
            if (p.isBajoStock()) {
                bajoStock++;
            }
            if (p.isPorVencer() || p.isVencido()) {
                porVencer++;
            }
            boolean coincide = (busqueda.isEmpty() || p.getProducto().toLowerCase(Locale.ROOT).contains(busqueda))
                    && (categoria.isEmpty() || categoria.equals(p.getCategoria()))
                    && (estado.isEmpty() || estado.equals(p.getEstado()));
            if (coincide) {
                visibles.add(p);
            }
        }

        req.setAttribute("items", visibles);
        req.setAttribute("totalItems", todos.size());
        req.setAttribute("bajoStock", bajoStock);
        req.setAttribute("porVencer", porVencer);
        req.setAttribute("valorTotal", Numeros.soles(valorTotal));
        req.setAttribute("categorias", categorias);
        req.setAttribute("estados", ESTADOS);
        req.setAttribute("tiposMovimiento", Movimiento.TIPOS_MANUALES);
        req.setAttribute("q", q);
        req.setAttribute("filtroCategoria", categoria);
        req.setAttribute("filtroEstado", estado);
        mostrar(req, resp, "inventario");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "guardar":
                    guardar(req, f);
                    break;
                case "movimiento":
                    String tipo = f.texto("tipo");
                    if (!Movimiento.TIPOS_MANUALES.contains(tipo)) {
                        throw new ValidacionException("Tipo de movimiento inválido");
                    }
                    BigDecimal cantidad = "AJUSTE".equals(tipo)
                            ? f.decimal("cantidad", "La cantidad debe ser un número >= 0")
                            : f.positivo("cantidad", "La cantidad debe ser mayor que cero");
                    BigDecimal stock = inventarioService.registrarMovimiento(f.id(), tipo, cantidad,
                            f.texto("motivo", null), nombreUsuario(req));
                    exito(req, "Movimiento registrado. Stock actual: " + Numeros.texto(stock));
                    break;
                case "eliminar":
                    if (inventarioDAO.eliminar(f.id())) {
                        exito(req, "Insumo eliminado");
                    } else {
                        error(req, "Insumo no encontrado");
                    }
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            if (InventarioDAO.EN_USO.equals(e.getSQLState())) {
                error(req, "No se puede eliminar: el insumo está en órdenes de compra. Puedes dejar su stock en 0.");
            } else {
                errorServidor(req, "INVENTARIO " + accion, e);
            }
        }
        redirigir(req, resp, "/inventario");
    }

    /** Crea el insumo si no llega id; si llega, actualiza sus datos (el stock cambia solo con movimientos). */
    private void guardar(HttpServletRequest req, Formulario f) throws ValidacionException, SQLException {
        Producto p = new Producto();
        p.setProducto(f.texto("producto"));
        if (p.getProducto().isEmpty()) {
            throw new ValidacionException("El producto es obligatorio");
        }
        p.setCategoria(f.texto("categoria", "General"));
        p.setUnidad(f.texto("unidad", "unidad"));
        p.setStockMinimo(f.decimal("stock_minimo", "El stock mínimo debe ser un número >= 0"));
        p.setPrecio(f.decimal("precio", "El precio debe ser un número >= 0"));
        p.setFechaVencimiento(f.fechaOpcional("fecha_vencimiento", "Fecha de vencimiento inválida"));
        p.setProveedorId(f.idOpcional("proveedor_id"));

        if (f.texto("id").isEmpty()) {
            p.setStock(f.decimal("stock", "El stock debe ser un número >= 0"));
            inventarioService.crearInsumo(p, nombreUsuario(req));
            exito(req, "Insumo agregado");
        } else {
            p.setId(f.id());
            if (inventarioService.actualizarInsumo(p)) {
                exito(req, "Insumo actualizado");
            } else {
                error(req, "Insumo no encontrado");
            }
        }
    }
}
