package com.calsoft.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Objetos de transferencia (DTO): lo que el frontend envía y recibe.
 * Las anotaciones validan los datos antes de llegar a la lógica.
 */
public final class Dtos {

    private Dtos() {
    }

    // ---------- Autenticación ----------
    public record LoginRequest(@NotBlank(message = "Usuario y clave son obligatorios") String usuario,
                               @NotBlank(message = "Usuario y clave son obligatorios") String clave) { }

    public record LoginResponse(String token, Integer id, String usuario, String rol, List<String> modulos) { }

    public record Mensaje(String mensaje) { }

    // ---------- Insumos ----------
    public record InsumoRequest(
            @NotBlank(message = "El nombre del insumo es obligatorio") @Size(max = 100) String nombre,
            @NotNull(message = "Elige una categoría") Integer categoriaId,
            @PositiveOrZero(message = "El stock debe ser >= 0") BigDecimal stock,
            @NotBlank(message = "La unidad es obligatoria") @Size(max = 20) String unidad,
            @NotNull(message = "El stock mínimo es obligatorio") @PositiveOrZero(message = "El stock mínimo debe ser >= 0") BigDecimal stockMinimo,
            @NotNull(message = "El precio es obligatorio") @PositiveOrZero(message = "El precio debe ser >= 0") BigDecimal precio,
            LocalDate fechaVencimiento,
            Integer proveedorId) { }

    public record MovimientoRequest(
            @NotBlank(message = "Elige el tipo de movimiento") String tipo,
            @NotNull(message = "La cantidad es obligatoria") @PositiveOrZero(message = "La cantidad debe ser >= 0") BigDecimal cantidad,
            @Size(max = 200) String motivo) { }

    // ---------- Carta ----------
    public record PlatoRequest(@NotBlank(message = "El nombre del plato es obligatorio") @Size(max = 100) String nombre,
                               @NotNull @PositiveOrZero(message = "El precio debe ser >= 0") BigDecimal precio,
                               boolean activo) { }

    public record IngredienteRequest(@NotNull(message = "Elige un insumo") Integer insumoId,
                                     @NotNull @Positive(message = "La cantidad por porción debe ser mayor que cero") BigDecimal cantidad) { }

    // ---------- POS y reservas ----------
    public record PedidoRequest(@NotBlank(message = "El cliente es obligatorio") @Size(max = 100) String cliente,
                                @NotBlank(message = "El mesero es obligatorio") @Size(max = 100) String mesero,
                                @NotNull(message = "Elige un plato de la carta") Integer platoId,
                                @Min(value = 1, message = "La cantidad debe ser un entero positivo") int cantidad,
                                @Min(value = 1, message = "La mesa debe ser un entero positivo") int mesa,
                                String estado) { }

    public record EstadoRequest(@NotBlank String estado) { }

    public record ReservaRequest(@NotBlank(message = "El cliente es obligatorio") @Size(max = 100) String cliente,
                                 @Size(max = 30) String telefono,
                                 @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
                                 @NotNull(message = "La hora es obligatoria") LocalTime hora,
                                 @Min(value = 1, message = "Personas debe ser un entero positivo") int personas,
                                 @Min(value = 1, message = "La mesa debe ser un entero positivo") int mesa,
                                 String estado) { }

    // ---------- Usuarios ----------
    public record UsuarioRequest(@NotBlank(message = "Usuario y clave son obligatorios") @Size(max = 50) String usuario,
                                 @NotBlank(message = "Usuario y clave son obligatorios")
                                 @Size(min = 4, message = "La clave debe tener al menos 4 caracteres") String clave,
                                 String rol) { }

    public record UsuarioCambio(String rol,
                                @Size(min = 4, message = "La clave debe tener al menos 4 caracteres") String clave) { }

    // ---------- Órdenes de compra ----------
    public record OrdenNueva(@NotNull(message = "Elige un proveedor") Integer proveedorId) { }

    public record OrdenItemNuevo(@NotNull(message = "Elige un insumo") Integer insumoId,
                                 @NotNull @Positive(message = "La cantidad debe ser mayor que cero") BigDecimal cantidad) { }

    public record OrdenItemCambio(@NotNull Integer id,
                                  @NotNull @Positive(message = "Las cantidades deben ser mayores que cero") BigDecimal cantidad,
                                  @NotNull @PositiveOrZero(message = "Los precios deben ser >= 0") BigDecimal precioUnitario) { }

    public record OrdenItemsCambio(@NotEmpty @Valid List<OrdenItemCambio> items) { }

    /** recibido: id del ítem → cantidad recibida. */
    public record Recepcion(@NotNull Map<Integer, BigDecimal> recibido, @Size(max = 200) String observacion) { }

    public record ResultadoEnvio(boolean exito, String mensaje) { }

    public record Sugerencia(@NotNull Integer insumoId,
                             @NotNull @Positive(message = "Las cantidades marcadas deben ser mayores que cero") BigDecimal cantidad) { }

    public record Sugerencias(@NotEmpty(message = "Marca al menos un insumo") @Valid List<Sugerencia> items) { }

    public record IdsCreados(List<Integer> ids) { }

    // ---------- Vistas ----------
    public record AlertaVista(Integer id, Integer insumoId, String insumo, String tipo, String motivo, String mensaje,
                              String estado, OffsetDateTime creadoEn, OffsetDateTime atendidaEn, String atendidaPor,
                              Integer ordenId, String ordenCodigo) { }

    public record Conteo(String nombre, BigDecimal cantidad) { }
}
