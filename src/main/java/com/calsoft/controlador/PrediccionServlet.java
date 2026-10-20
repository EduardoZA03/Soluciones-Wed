package com.calsoft.controlador;

import com.calsoft.modelo.Prediccion;
import com.calsoft.servicio.OrdenService;
import com.calsoft.servicio.PrediccionService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Predicción de reposición con IA (HU-05). Muestra consumo estimado,
 * cantidad sugerida y periodo; el administrador acepta o ajusta las
 * cantidades y genera las órdenes de compra.
 */
@WebServlet("/prediccion")
public class PrediccionServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final List<Integer> HORIZONTES = List.of(7, 14, 30);

    private final PrediccionService prediccionService = new PrediccionService();
    private final OrdenService ordenService = new OrdenService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int horizonte = 7;
        try {
            int h = new Formulario(req).enteroOpcional("dias", "Periodo inválido");
            if (HORIZONTES.contains(h)) {
                horizonte = h;
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        }

        List<Prediccion> predicciones = new ArrayList<>();
        try {
            predicciones = prediccionService.predecir(horizonte);
        } catch (SQLException e) {
            errorServidor(req, "PREDICCION", e);
        }
        // Primero los que conviene reponer, luego por menor cobertura.
        predicciones.sort((a, b) -> {
            if (a.isConviene() != b.isConviene()) {
                return a.isConviene() ? -1 : 1;
            }
            return Integer.compare(cobertura(a), cobertura(b));
        });

        BigDecimal costo = BigDecimal.ZERO;
        int aReponer = 0;
        for (Prediccion p : predicciones) {
            if (p.isConviene()) {
                aReponer++;
                costo = costo.add(p.getCostoSugerido());
            }
        }
        req.setAttribute("predicciones", predicciones);
        req.setAttribute("horizonte", horizonte);
        req.setAttribute("horizontes", HORIZONTES);
        req.setAttribute("aReponer", aReponer);
        req.setAttribute("costoSugerido", com.calsoft.modelo.Numeros.soles(costo));
        mostrar(req, resp, "prediccion");
    }

    private static int cobertura(Prediccion p) {
        return p.getDiasCobertura() == null ? Integer.MAX_VALUE : p.getDiasCobertura();
    }

    /** Crea órdenes en borrador con las cantidades marcadas (aceptadas o ajustadas). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String[] seleccion = req.getParameterValues("insumo");
        try {
            if (seleccion == null || seleccion.length == 0) {
                throw new ValidacionException("Marca al menos un insumo para generar la orden");
            }
            Map<Integer, BigDecimal> cantidades = new LinkedHashMap<>();
            for (String s : seleccion) {
                int id;
                try {
                    id = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    throw new ValidacionException("Selección inválida");
                }
                cantidades.put(id, f.positivo("cantidad_" + id, "Las cantidades marcadas deben ser mayores que cero"));
            }
            List<Integer> ids = ordenService.crearDesdeSugerencias(cantidades, nombreUsuario(req));
            exito(req, ids.size() == 1
                    ? "Se creó 1 orden de compra en borrador."
                    : "Se crearon " + ids.size() + " órdenes de compra en borrador (una por proveedor).");
            redirigir(req, resp, ids.size() == 1 ? "/compras?id=" + ids.get(0) : "/compras?estado=Borrador");
            return;
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "PREDICCION ORDEN", e);
        }
        redirigir(req, resp, "/prediccion?dias=" + f.texto("dias", "7"));
    }
}
