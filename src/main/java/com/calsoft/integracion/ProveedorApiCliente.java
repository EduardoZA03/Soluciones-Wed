package com.calsoft.integracion;

import com.calsoft.modelo.OrdenCompra;
import com.calsoft.modelo.OrdenItem;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente de la API REST del proveedor (interoperabilidad, HU-07).
 *
 * Envía la orden como JSON por HTTP POST, con la API key en la cabecera
 * X-Api-Key, y devuelve la confirmación del proveedor. Cualquier fallo
 * (sin conexión, tiempo agotado, HTTP 4xx/5xx, JSON inválido) se lanza
 * como IOException para que quede registrado en la orden.
 */
public class ProveedorApiCliente {

    public static final String COMPRADOR = "La Crianza Arequipeña";

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public RespuestaProveedor enviarOrden(String url, String apiKey, OrdenCompra orden)
            throws IOException, InterruptedException {
        SolicitudOrden solicitud = new SolicitudOrden();
        solicitud.codigo = orden.getCodigo();
        solicitud.comprador = COMPRADOR;
        for (OrdenItem it : orden.getItems()) {
            SolicitudOrden.Item item = new SolicitudOrden.Item();
            item.itemId = it.getId();
            item.insumo = it.getInsumo();
            item.unidad = it.getUnidad();
            item.cantidad = it.getCantidad();
            solicitud.items.add(item);
        }

        HttpRequest peticion;
        try {
            peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .header("X-Api-Key", apiKey == null ? "" : apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(solicitud), StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new IOException("URL de la API inválida: " + url, e);
        }

        HttpResponse<String> respuesta = HTTP.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (respuesta.statusCode() / 100 != 2) {
            throw new IOException("El proveedor respondió HTTP " + respuesta.statusCode() + ": " + recortar(respuesta.body()));
        }
        try {
            RespuestaProveedor r = GSON.fromJson(respuesta.body(), RespuestaProveedor.class);
            if (r == null) {
                throw new IOException("El proveedor devolvió una respuesta vacía");
            }
            return r;
        } catch (JsonParseException e) {
            throw new IOException("Respuesta del proveedor no es JSON válido: " + recortar(respuesta.body()), e);
        }
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() > 200 ? texto.substring(0, 200) + "…" : texto;
    }
}
