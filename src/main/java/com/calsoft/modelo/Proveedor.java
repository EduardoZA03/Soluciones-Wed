package com.calsoft.modelo;

/** Proveedor de insumos, con los datos para conectarse a su API. */
public class Proveedor {

    private int id;
    private String nombre;
    private String ruc;
    private String telefono;
    private String email;
    private String apiUrl;
    private String apiKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /** true si usa el proveedor simulado incluido en CalSoft. */
    public boolean isSimulado() {
        return apiUrl == null || apiUrl.isBlank();
    }

    /** La API key con solo los 4 últimos caracteres visibles. */
    public String getApiKeyOculta() {
        if (apiKey == null || apiKey.length() <= 4) {
            return apiKey == null ? "" : "••••";
        }
        return "••••" + apiKey.substring(apiKey.length() - 4);
    }
}
