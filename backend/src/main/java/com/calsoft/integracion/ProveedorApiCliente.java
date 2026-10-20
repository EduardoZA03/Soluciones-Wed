package com.calsoft.integracion;

import com.calsoft.modelo.OrdenCompra;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente de la API REST del proveedor (interoperabilidad, HU-07).
 *
 * Envía la orden como JSON por HTTP POST, con la API key en la cabecera
 * X-Api-Key. Cualquier fallo (sin conexión, tiempo agotado, HTTP 4xx/5xx,
 * JSON inválido) se lanza como ErrorProveedor para registrarlo en la orden.
 */
@Component
public class ProveedorApiCliente {

    public static final String COMPRADOR = "La Crianza Arequipeña";

    /** Error al comunicarse con el proveedor. */
    public static class ErrorProveedor extends Exception {
        private static final long serialVersionUID = 1L;

        public ErrorProveedor(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private final RestClient http;

    public ProveedorApiCliente(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(5));
        tiempos.setReadTimeout(Duration.ofSeconds(10));
        this.http = builder.requestFactory(tiempos).build();
    }

    public RespuestaProveedor enviarOrden(String url, String apiKey, OrdenCompra orden) throws ErrorProveedor {
        SolicitudOrden solicitud = new SolicitudOrden(orden.getCodigo(), COMPRADOR, orden.getItems().stream()
                .map(it -> new SolicitudOrden.Item(it.getId(), it.getInsumo().getNombre(),
                        it.getInsumo().getUnidad(), it.getCantidad()))
                .toList());
        try {
            RespuestaProveedor r = http.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Api-Key", apiKey == null ? "" : apiKey)
                    .body(solicitud)
                    .retrieve()
                    .body(RespuestaProveedor.class);
            if (r == null) {
                throw new ErrorProveedor("El proveedor devolvió una respuesta vacía", null);
            }
            return r;
        } catch (RestClientResponseException e) {
            throw new ErrorProveedor("El proveedor respondió HTTP " + e.getStatusCode().value() + ": "
                    + recortar(e.getResponseBodyAsString()), e);
        } catch (RestClientException | IllegalArgumentException e) {
            throw new ErrorProveedor("No se pudo conectar con el proveedor: " + recortar(e.getMessage()), e);
        }
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() > 200 ? texto.substring(0, 200) + "…" : texto;
    }
}
