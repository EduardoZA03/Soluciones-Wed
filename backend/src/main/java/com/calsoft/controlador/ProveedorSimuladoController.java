package com.calsoft.controlador;

import com.calsoft.integracion.RespuestaProveedor;
import com.calsoft.integracion.SolicitudOrden;
import com.calsoft.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proveedor SIMULADO: imita el sistema externo de un proveedor para probar
 * la interoperabilidad sin depender de un tercero.
 *
 *   POST /api/proveedor-simulado/ordenes   (cabecera X-Api-Key)
 *
 * Reglas: API key inválida → 401; cada ítem está disponible si la cantidad
 * es de hasta 500; entrega al día siguiente (si cae domingo, el lunes).
 */
@RestController
@RequestMapping("/api/proveedor-simulado")
public class ProveedorSimuladoController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BigDecimal MAXIMO_DISPONIBLE = new BigDecimal("500");

    private final ProveedorRepository proveedorRepo;

    public ProveedorSimuladoController(ProveedorRepository proveedorRepo) {
        this.proveedorRepo = proveedorRepo;
    }

    @GetMapping("/ordenes")
    public Map<String, String> info() {
        return Map.of("servicio", "Proveedor simulado CalSoft",
                "uso", "POST con JSON {codigo, comprador, items:[{item_id, insumo, unidad, cantidad}]}",
                "autenticacion", "Cabecera X-Api-Key con la API key del proveedor");
    }

    @PostMapping("/ordenes")
    public ResponseEntity<?> recibirOrden(@RequestHeader(value = "X-Api-Key", required = false) String apiKey,
                                          @RequestBody SolicitudOrden solicitud) {
        if (apiKey == null || apiKey.isBlank() || !proveedorRepo.existsByApiKey(apiKey)) {
            return ResponseEntity.status(401).body(Map.of("error", "API key inválida"));
        }
        if (solicitud.items() == null || solicitud.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La orden no tiene ítems"));
        }

        List<RespuestaProveedor.Item> items = new ArrayList<>();
        int disponibles = 0;
        for (SolicitudOrden.Item item : solicitud.items()) {
            boolean disponible = item.cantidad() != null && item.cantidad().compareTo(MAXIMO_DISPONIBLE) <= 0;
            items.add(new RespuestaProveedor.Item(item.itemId(), disponible, disponible ? item.cantidad() : BigDecimal.ZERO));
            if (disponible) {
                disponibles++;
            }
        }
        if (disponibles == 0) {
            return ResponseEntity.ok(new RespuestaProveedor(false, null, null,
                    "Sin stock para los ítems solicitados", items));
        }
        LocalDate entrega = LocalDate.now().plusDays(1);
        if (entrega.getDayOfWeek() == DayOfWeek.SUNDAY) {
            entrega = entrega.plusDays(1);
        }
        String mensaje = disponibles == items.size()
                ? "Pedido confirmado"
                : "Pedido confirmado parcialmente: " + (items.size() - disponibles) + " ítem(s) sin stock";
        return ResponseEntity.ok(new RespuestaProveedor(true, "PRV-" + (100000 + RANDOM.nextInt(900000)),
                entrega.toString(), mensaje, items));
    }
}
