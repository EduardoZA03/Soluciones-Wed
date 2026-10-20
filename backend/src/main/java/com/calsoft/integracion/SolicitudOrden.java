package com.calsoft.integracion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * JSON que el backend envía al proveedor con la orden de compra:
 * <pre>
 * { "codigo": "OC-20261019-7KQ2XD", "comprador": "La Crianza Arequipeña",
 *   "items": [ { "item_id": 1, "insumo": "Papas", "unidad": "kg", "cantidad": 40 } ] }
 * </pre>
 */
public record SolicitudOrden(String codigo, String comprador, List<Item> items) {

    public record Item(@JsonProperty("item_id") Integer itemId, String insumo, String unidad, BigDecimal cantidad) {
    }
}
