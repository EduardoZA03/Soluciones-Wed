package com.calsoft.servicio;

import com.calsoft.config.Conexion;
import com.calsoft.dao.InventarioDAO;
import com.calsoft.dao.MovimientoDAO;
import com.calsoft.modelo.Prediccion;
import com.calsoft.modelo.Producto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Módulo de IA de predicción de reposición (HU-05).
 *
 * Modelo: suavizado exponencial con estacionalidad semanal (variante simple
 * de Holt-Winters). Aprende de las salidas de las últimas 8 semanas:
 *
 *  1. ESTACIONALIDAD: cuánto se consume cada día de la semana respecto al
 *     promedio (p. ej. el sábado se vende 1.5 veces más que un día normal).
 *  2. NIVEL: el consumo "base" diario, quitando el efecto del día de la semana.
 *     Se actualiza día a día dando más peso a lo reciente (ALFA = 0.3).
 *  3. PRONÓSTICO: nivel × factor del día, sumado sobre el periodo pedido.
 *
 * Cantidad sugerida = consumo estimado + stock mínimo − stock actual (redondeado
 * hacia arriba). La confianza se mide con el error que tuvo el modelo al
 * "predecir" el pasado (error absoluto medio relativo).
 */
public class PrediccionService {

    public static final int DIAS_HISTORIA = 56;
    private static final double ALFA = 0.3;
    private static final double FACTOR_MINIMO = 0.05;

    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    /** Predicción para todos los insumos en los próximos {@code horizonte} días. */
    public List<Prediccion> predecir(int horizonte) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            Map<Integer, double[]> series = movimientoDAO.consumoDiario(cn, DIAS_HISTORIA);
            List<Prediccion> lista = new ArrayList<>();
            for (Producto p : inventarioDAO.listar(cn)) {
                lista.add(calcular(p, series.get(p.getId()), horizonte));
            }
            return lista;
        }
    }

    /** Cantidad sugerida para un insumo (usada al crear una orden desde una alerta). */
    public BigDecimal sugeridoPara(Connection cn, Producto p, int horizonte) throws SQLException {
        Map<Integer, double[]> series = movimientoDAO.consumoDiario(cn, DIAS_HISTORIA);
        return calcular(p, series.get(p.getId()), horizonte).getSugerido();
    }

    Prediccion calcular(Producto p, double[] y, int horizonte) {
        int n = DIAS_HISTORIA;
        if (y == null) {
            y = new double[n];
        }
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(n);

        Prediccion pr = new Prediccion();
        pr.setInsumo(p);
        pr.setHorizonteDias(horizonte);

        double total = 0;
        int diasConDatos = 0;
        for (double v : y) {
            total += v;
            if (v > 0) {
                diasConDatos++;
            }
        }
        pr.setDiasConDatos(diasConDatos);
        double media = total / n;

        double nivel;
        double[] factor = new double[7];
        java.util.Arrays.fill(factor, 1.0);
        String confianza;
        String tendencia = "Estable";

        if (diasConDatos < 7 || media == 0) {
            // Sin histórico suficiente: se usa el promedio simple y se avisa con confianza baja.
            nivel = media;
            confianza = "Baja";
            tendencia = "Sin datos";
        } else {
            // 1) Factores estacionales por día de la semana (0 = lunes).
            double[] suma = new double[7];
            int[] cuenta = new int[7];
            for (int i = 0; i < n; i++) {
                int dia = inicio.plusDays(i).getDayOfWeek().getValue() - 1;
                suma[dia] += y[i];
                cuenta[dia]++;
            }
            for (int d = 0; d < 7; d++) {
                factor[d] = cuenta[d] == 0 ? 1.0 : (suma[d] / cuenta[d]) / media;
            }

            // 2) Nivel con suavizado exponencial sobre la serie sin estacionalidad.
            double inicial = 0;
            for (int i = 0; i < 7; i++) {
                inicial += y[i] / Math.max(factor[inicio.plusDays(i).getDayOfWeek().getValue() - 1], FACTOR_MINIMO);
            }
            nivel = inicial / 7;
            double errorAbs = 0;
            double reales = 0;
            for (int i = 7; i < n; i++) {
                double f = factor[inicio.plusDays(i).getDayOfWeek().getValue() - 1];
                double pronosticoDelDia = nivel * f; // lo que el modelo esperaba ese día
                errorAbs += Math.abs(y[i] - pronosticoDelDia);
                reales += y[i];
                if (f >= FACTOR_MINIMO) {
                    nivel = ALFA * (y[i] / f) + (1 - ALFA) * nivel;
                }
            }

            // 3) Confianza según el error histórico del modelo.
            double error = reales == 0 ? 1 : errorAbs / reales;
            confianza = error < 0.25 ? "Alta" : error < 0.45 ? "Media" : "Baja";

            // Tendencia: últimas 2 semanas contra las 2 anteriores.
            double reciente = 0;
            double anterior = 0;
            for (int i = n - 14; i < n; i++) {
                reciente += y[i];
            }
            for (int i = n - 28; i < n - 14; i++) {
                anterior += y[i];
            }
            if (anterior > 0) {
                double ratio = reciente / anterior;
                tendencia = ratio > 1.1 ? "Al alza" : ratio < 0.9 ? "A la baja" : "Estable";
            }
        }

        double estimado = 0;
        for (int h = 0; h < horizonte; h++) {
            estimado += nivel * factor[hoy.plusDays(h).getDayOfWeek().getValue() - 1];
        }

        double stock = p.getStock().doubleValue();
        double sugerido = Math.ceil(Math.max(0, estimado + p.getStockMinimo().doubleValue() - stock));

        pr.setConsumoDiario(redondear(nivel));
        pr.setConsumoEstimado(redondear(estimado));
        pr.setSugerido(BigDecimal.valueOf((long) sugerido));
        pr.setDiasCobertura(nivel > 0 ? (int) Math.floor(stock / nivel) : null);
        pr.setConfianza(confianza);
        pr.setTendencia(tendencia);
        return pr;
    }

    private static BigDecimal redondear(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
