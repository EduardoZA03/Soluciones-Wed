package com.calsoft.integracion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * JSON con el que responde el proveedor:
 * <pre>
 * { "aceptada": true, "confirmacion": "PRV-482913", "fecha_entrega": "2026-10-21",
 *   "mensaje": "Pedido confirmado",
 *   "items": [ { "item_id": 1, "disponible": true, "cantidad_confirmada": 40 } ] }
 * </pre>
 */
public record RespuestaProveedor(boolean aceptada,
                                 String confirmacion,
                                 @JsonProperty("fecha_entrega") String fechaEntrega,
                                 String mensaje,
                                 List<Item> items) {

    public record Item(@JsonProperty("item_id") Integer itemId,
                       boolean disponible,
                       @JsonProperty("cantidad_confirmada") BigDecimal cantidadConfirmada) {
    }
}
