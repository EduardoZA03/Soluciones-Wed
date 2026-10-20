package com.calsoft.controlador;

import com.calsoft.dto.Dtos.IdsCreados;
import com.calsoft.dto.Dtos.Sugerencia;
import com.calsoft.dto.Dtos.Sugerencias;
import com.calsoft.error.ValidacionException;
import com.calsoft.seguridad.UsuarioActual;
import com.calsoft.servicio.OrdenService;
import com.calsoft.servicio.PrediccionService;
import com.calsoft.servicio.PrediccionService.Prediccion;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Predicción de reposición con IA (HU-05). */
@RestController
@RequestMapping("/api/prediccion")
public class PrediccionController {

    private static final List<Integer> HORIZONTES = List.of(7, 14, 30);

    private final PrediccionService prediccion;
    private final OrdenService ordenes;

    public PrediccionController(PrediccionService prediccion, OrdenService ordenes) {
        this.prediccion = prediccion;
        this.ordenes = ordenes;
    }

    /** dias = periodo a pronosticar: 7, 14 o 30. */
    @GetMapping
    public List<Prediccion> predecir(@RequestParam(defaultValue = "7") int dias) {
        if (!HORIZONTES.contains(dias)) {
            throw new ValidacionException("Periodo inválido: usa 7, 14 o 30 días");
        }
        return prediccion.predecir(dias);
    }

    /** Crea órdenes en Borrador (una por proveedor) con las cantidades aceptadas o ajustadas. */
    @PostMapping("/ordenes")
    @ResponseStatus(HttpStatus.CREATED)
    public IdsCreados generarOrdenes(@Valid @RequestBody Sugerencias r) {
        Map<Integer, BigDecimal> cantidades = new LinkedHashMap<>();
        for (Sugerencia s : r.items()) {
            cantidades.put(s.insumoId(), s.cantidad());
        }
        return new IdsCreados(ordenes.crearDesdeSugerencias(cantidades, UsuarioActual.nombre()));
    }
}
