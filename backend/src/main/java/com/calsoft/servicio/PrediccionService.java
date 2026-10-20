package com.calsoft.servicio;

import com.calsoft.modelo.Insumo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.calsoft.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo de IA de predicción de reposición (HU-05).
 *
 * Modelo: suavizado exponencial con estacionalidad semanal (variante simple
 * de Holt-Winters). Aprende de las salidas de las últimas 8 semanas:
 *
 *  1. ESTACIONALIDAD: cuánto se consume cada día de la semana respecto al
 *     promedio (p. ej. el sábado se vende 1.5 veces más que un día normal).
 *  2. NIVEL: el consumo "base" diario sin el efecto del día de la semana,
 *     actualizado día a día con más peso a lo reciente (ALFA = 0.3).
 *  3. PRONÓSTICO: nivel × factor del día, sumado sobre el periodo pedido.
 *
 * Cantidad sugerida = consumo estimado + stock mínimo − stock actual (hacia arriba).
 * La confianza sale del error que tuvo el modelo al "predecir" el pasado.
 */
@Service
public class PrediccionService {

    public static final int DIAS_HISTORIA = 56;
    private static final double ALFA = 0.3;
    private static final double FACTOR_MINIMO = 0.05;

    /** Resultado del modelo para un insumo. */
    public record Prediccion(Insumo insumo, int horizonteDias, int diasConDatos, BigDecimal consumoDiario,
                             BigDecimal consumoEstimado, BigDecimal sugerido, Integer diasCobertura,
                             String confianza, String tendencia, BigDecimal costoSugerido) {

        @JsonProperty("conviene")
        public boolean isConviene() {
            return sugerido.signum() > 0;
        }
    }

    private final InsumoRepository insumoRepo;
    private final JdbcTemplate jdbc;

    public PrediccionService(InsumoRepository insumoRepo, JdbcTemplate jdbc) {
        this.insumoRepo = insumoRepo;
        this.jdbc = jdbc;
    }

    /** Predicción para todos los insumos; primero los que conviene reponer. */
    @Transactional(readOnly = true)
    public List<Prediccion> predecir(int horizonte) {
        Map<Integer, double[]> series = consumoDiario();
        List<Prediccion> lista = new ArrayList<>();
        for (Insumo i : insumoRepo.findAllByOrderByNombreAsc()) {
            lista.add(calcular(i, series.get(i.getId()), horizonte));
        }
        lista.sort((a, b) -> a.isConviene() != b.isConviene()
                ? (a.isConviene() ? -1 : 1)
                : Integer.compare(cobertura(a), cobertura(b)));
        return lista;
    }

    /** Cantidad sugerida para un insumo (al crear una orden desde una alerta). */
    public BigDecimal sugeridoPara(Insumo insumo, int horizonte) {
        return calcular(insumo, consumoDiario().get(insumo.getId()), horizonte).sugerido();
    }

    private static int cobertura(Prediccion p) {
        return p.diasCobertura() == null ? Integer.MAX_VALUE : p.diasCobertura();
    }

    /** Serie histórica: insumoId → consumo de cada uno de los últimos 56 días (sin hoy). */
    Map<Integer, double[]> consumoDiario() {
        LocalDate inicio = LocalDate.now().minusDays(DIAS_HISTORIA);
        Map<Integer, double[]> series = new HashMap<>();
        jdbc.query("SELECT insumo_id, creado_en::date AS dia, SUM(cantidad) AS total FROM movimientos "
                + "WHERE tipo = 'SALIDA' AND creado_en >= CURRENT_DATE - " + DIAS_HISTORIA
                + " AND creado_en < CURRENT_DATE GROUP BY insumo_id, creado_en::date", rs -> {
                    int indice = (int) (rs.getDate("dia").toLocalDate().toEpochDay() - inicio.toEpochDay());
                    if (indice >= 0 && indice < DIAS_HISTORIA) {
                        series.computeIfAbsent(rs.getInt("insumo_id"), k -> new double[DIAS_HISTORIA])[indice] =
                                rs.getBigDecimal("total").doubleValue();
                    }
                });
        return series;
    }

    Prediccion calcular(Insumo p, double[] y, int horizonte) {
        int n = DIAS_HISTORIA;
        if (y == null) {
            y = new double[n];
        }
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(n);

        double total = 0;
        int diasConDatos = 0;
        for (double v : y) {
            total += v;
            if (v > 0) {
                diasConDatos++;
            }
        }
        double media = total / n;

        double nivel;
        double[] factor = new double[7];
        Arrays.fill(factor, 1.0);
        String confianza;
        String tendencia = "Estable";

        if (diasConDatos < 7 || media == 0) {
            nivel = media; // sin histórico suficiente: promedio simple y confianza baja
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
                errorAbs += Math.abs(y[i] - nivel * f); // lo que el modelo esperaba ese día
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
        BigDecimal sugerido = BigDecimal.valueOf((long) Math.ceil(
                Math.max(0, estimado + p.getStockMinimo().doubleValue() - stock)));

        return new Prediccion(p, horizonte, diasConDatos, redondear(nivel), redondear(estimado), sugerido,
                nivel > 0 ? (int) Math.floor(stock / nivel) : null, confianza, tendencia,
                sugerido.multiply(p.getPrecio()).setScale(2, RoundingMode.HALF_UP));
    }

    private static BigDecimal redondear(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
