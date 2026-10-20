package com.calsoft.integracion;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON con el que responde el proveedor.
 * <pre>
 * {
 *   "aceptada": true,
 *   "confirmacion": "PRV-482913",
 *   "fecha_entrega": "2026-10-21",
 *   "mensaje": "Pedido confirmado",
 *   "items": [ { "item_id": 1, "disponible": true, "cantidad_confirmada": 40 } ]
 * }
 * </pre>
 */
public class RespuestaProveedor {

    public boolean aceptada;
    public String confirmacion;
    @SerializedName("fecha_entrega")
    public String fechaEntrega;
    public String mensaje;
    public List<Item> items = new ArrayList<>();

    public static class Item {
        @SerializedName("item_id")
        public int itemId;
        public boolean disponible;
        @SerializedName("cantidad_confirmada")
        public BigDecimal cantidadConfirmada;
    }
}
