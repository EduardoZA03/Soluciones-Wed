package com.calsoft.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formato de cantidades y montos para los mensajes. */
public final class Numeros {

    private Numeros() {
    }

    /** 8.000 → "8", 0.250 → "0.25". */
    public static String texto(BigDecimal n) {
        if (n == null) {
            return "";
        }
        BigDecimal s = n.stripTrailingZeros();
        return (s.scale() < 0 ? s.setScale(0) : s).toPlainString();
    }

    /** 18 → "18.00". */
    public static String soles(BigDecimal n) {
        return (n == null ? BigDecimal.ZERO : n).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
