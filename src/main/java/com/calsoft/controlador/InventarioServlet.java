package com.calsoft.controlador;

import com.calsoft.dao.InventarioDAO;
import com.calsoft.modelo.Producto;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Productos, stock, precio y alerta de bajo stock. */
@WebServlet("/inventario")
public class InventarioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final InventarioDAO inventarioDAO = new InventarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Producto> items = new ArrayList<>();
        try {
            items = inventarioDAO.listar();
        } catch (SQLException e) {
            errorServidor(req, "LISTAR INVENTARIO", e);
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        int bajoStock = 0;
        for (Producto p : items) {
            valorTotal = valorTotal.add(p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())));
            if (p.isBajoStock()) {
                bajoStock++;
            }
        }

        req.setAttribute("items", items);
        req.setAttribute("bajoStock", bajoStock);
        req.setAttribute("valorTotal", valorTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
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
                case "eliminar":
                    if (inventarioDAO.eliminar(f.id())) {
                        exito(req, "Producto eliminado");
                    } else {
                        error(req, "Producto no encontrado");
                    }
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "INVENTARIO " + accion, e);
        }
        redirigir(req, resp, "/inventario");
    }

    /** Crea el producto si no llega id; si llega, lo actualiza. */
    private void guardar(HttpServletRequest req, Formulario f) throws ValidacionException, SQLException {
        Producto p = new Producto();
        p.setProducto(f.texto("producto"));
        if (p.getProducto().isEmpty()) {
            throw new ValidacionException("El producto es obligatorio");
        }
        p.setCategoria(f.texto("categoria", "General"));
        p.setStock(f.entero("stock", 0, "El stock debe ser un entero >= 0"));
        p.setUnidad(f.texto("unidad", "unidad"));
        p.setStockMinimo(f.enteroOpcional("stock_minimo", "El stock mínimo debe ser un entero >= 0"));
        p.setPrecio(f.decimal("precio", "El precio debe ser un número >= 0"));

        if (f.texto("id").isEmpty()) {
            inventarioDAO.crear(p);
            exito(req, "Producto agregado");
        } else {
            p.setId(f.id());
            if (inventarioDAO.actualizar(p)) {
                exito(req, "Producto actualizado");
            } else {
                error(req, "Producto no encontrado");
            }
        }
    }
}
