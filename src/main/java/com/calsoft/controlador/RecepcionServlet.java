package com.calsoft.controlador;

import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenItem;
import com.calsoft.servicio.OrdenService;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Recepción de insumos por QR o código de barras (HU-08).
 * El código se lee con la cámara (html5-qrcode) o con una pistola lectora,
 * que escribe el código en el campo como si fuera un teclado.
 */
@WebServlet("/recepcion")
public class RecepcionServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final OrdenService ordenService = new OrdenService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String codigo = new Formulario(req).texto("codigo").toUpperCase(Locale.ROOT);
        req.setAttribute("codigo", codigo);
        if (!codigo.isEmpty()) {
            try {
                OrdenCompra orden = ordenService.buscarPorCodigo(codigo);
                if (orden == null) {
                    req.setAttribute("aviso", "No existe una orden con el código " + codigo);
                } else {
                    req.setAttribute("orden", orden);
                    if (!orden.isRecibible()) {
                        req.setAttribute("aviso", "La orden está en estado \"" + orden.getEstado()
                                + "\". Solo se reciben órdenes confirmadas por el proveedor.");
                    }
                }
            } catch (SQLException e) {
                errorServidor(req, "RECEPCION BUSCAR", e);
            }
        }
        mostrar(req, resp, "recepcion");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String codigo = f.texto("codigo");
        try {
            OrdenCompra orden = ordenService.buscar(f.id());
            if (orden == null) {
                throw new ValidacionException("Orden no encontrada");
            }
            Map<Integer, BigDecimal> recibido = new HashMap<>();
            for (OrdenItem it : orden.getItems()) {
                recibido.put(it.getId(), f.decimal("recibido_" + it.getId(),
                        "Cantidad recibida inválida para " + it.getInsumo()));
            }
            String estado = ordenService.recibir(orden.getId(), recibido, f.texto("observacion"), nombreUsuario(req));
            if (OrdenCompra.RECIBIDA.equals(estado)) {
                exito(req, "Recepción registrada: stock actualizado y orden " + orden.getCodigo() + " cerrada.");
            } else {
                error(req, "Recepción registrada CON INCIDENCIA: el stock se actualizó con lo recibido. "
                        + "Revisa la línea de tiempo de la orden para reclamar al proveedor.");
            }
            redirigir(req, resp, "/compras?id=" + orden.getId());
            return;
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "RECEPCION", e);
        }
        redirigir(req, resp, "/recepcion?codigo=" + URLEncoder.encode(codigo, StandardCharsets.UTF_8));
    }
}
