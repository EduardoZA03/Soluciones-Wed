package com.calsoft.integracion;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON que CalSoft envía al proveedor con la orden de compra.
 * <pre>
 * {
 *   "codigo": "OC-20261019-7KQ2XD",
 *   "comprador": "La Crianza Arequipeña",
 *   "items": [ { "item_id": 1, "insumo": "Papas", "unidad": "kg", "cantidad": 40 } ]
 * }
 * </pre>
 */
public class SolicitudOrden {

    public String codigo;
    public String comprador;
    public List<Item> items = new ArrayList<>();

    public static class Item {
        @SerializedName("item_id")
        public int itemId;
        public String insumo;
        public String unidad;
        public BigDecimal cantidad;
    }
}
