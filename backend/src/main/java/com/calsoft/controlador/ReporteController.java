package com.calsoft.controlador;

import com.calsoft.servicio.ReporteService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inicio (cualquier usuario con sesión) y Reportes (Administrador). */
@RestController
@RequestMapping("/api")
public class ReporteController {

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/inicio")
    public Map<String, Object> inicio() {
        return servicio.inicio();
    }

    @GetMapping("/reportes")
    public Map<String, Object> reportes() {
        return servicio.reportes();
    }
}
