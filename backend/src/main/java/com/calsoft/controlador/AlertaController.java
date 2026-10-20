package com.calsoft.controlador;

import com.calsoft.dto.Dtos.AlertaVista;
import com.calsoft.modelo.OrdenCompra;
import com.calsoft.seguridad.UsuarioActual;
import com.calsoft.servicio.AlertaService;
import com.calsoft.servicio.OrdenService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Alertas de stock mínimo y vencimiento (HU-04) y órdenes desde alertas (HU-06). */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertas;
    private final OrdenService ordenes;

    public AlertaController(AlertaService alertas, OrdenService ordenes) {
        this.alertas = alertas;
        this.ordenes = ordenes;
    }

    /** estado = Pendiente | Atendida */
    @GetMapping
    public List<AlertaVista> listar(@RequestParam(defaultValue = "Pendiente") String estado) {
        return alertas.listar(estado, "Pendiente".equals(estado) ? 200 : 30);
    }

    /** Para el contador del menú. */
    @GetMapping("/pendientes")
    public Map<String, Long> pendientes() {
        return Map.of("cantidad", alertas.contarPendientes());
    }

    @PostMapping("/{id}/atender")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atender(@PathVariable Integer id) {
        alertas.atender(id, UsuarioActual.nombre());
    }

    /** Genera una orden de compra en Borrador con la cantidad sugerida por la IA. */
    @PostMapping("/{id}/orden")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenCompra generarOrden(@PathVariable Integer id) {
        return ordenes.crearDesdeAlerta(id, UsuarioActual.nombre());
    }
}
