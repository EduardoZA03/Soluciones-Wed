package com.calsoft.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tabla maestra: proveedor de insumos, con los datos de conexión a su API. */
@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 100, message = "La razón social admite hasta 100 caracteres")
    private String nombre;

    @Pattern(regexp = "^$|\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @Size(max = 30)
    private String telefono;

    @Email(message = "Email inválido")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^$|https?://.+", message = "La URL de la API debe empezar con http:// o https://")
    @Size(max = 255)
    @Column(name = "api_url")
    private String apiUrl;

    /** Nunca se devuelve en el JSON: solo se puede escribir. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 100)
    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = OffsetDateTime.now();
    }

    /** true si usa el proveedor simulado incluido en el backend. */
    public boolean isSimulado() {
        return apiUrl == null || apiUrl.isBlank();
    }

    /** La API key con solo los 4 últimos caracteres visibles. */
    public String getApiKeyOculta() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        return apiKey.length() <= 4 ? "••••" : "••••" + apiKey.substring(apiKey.length() - 4);
    }
}
