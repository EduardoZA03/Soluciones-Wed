package com.calsoft.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formato de cantidades y montos para las vistas. */
public final class Numeros {

    private Numeros() {
    }

    /** Cantidad sin ceros sobrantes: 8.000 → "8", 0.250 → "0.25". */
    public static String texto(BigDecimal n) {
        if (n == null) {
            return "";
        }
        BigDecimal s = n.stripTrailingZeros();
        return (s.scale() < 0 ? s.setScale(0) : s).toPlainString();
    }

    /** Monto con dos decimales: 18 → "18.00". */
    public static String soles(BigDecimal n) {
        return (n == null ? BigDecimal.ZERO : n).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
