package com.calsoft.modelo;

import com.calsoft.util.Permisos;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Usuario del sistema. La clave (hash) nunca se envía a las vistas.
 * Es Serializable porque se guarda en la sesión HTTP.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final List<String> ROLES = List.of(Permisos.ADMIN, Permisos.ALMACEN, Permisos.COCINA,
            Permisos.MESERO, Permisos.EMPLEADO);

    private int id;
    private String usuario;
    private String rol;
    private LocalDateTime creadoEn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    /** true si el rol del usuario puede entrar al módulo. Uso en JSP: ${usuario.puede('pedidos')} */
    public boolean puede(String modulo) {
        return Permisos.permitido(rol, modulo);
    }

    public boolean isPuedeAprobar() {
        return Permisos.puedeAprobar(rol);
    }

    /** Fecha de creación en formato yyyy-MM-dd, para mostrar en tablas. */
    public String getCreadoEnTexto() {
        return creadoEn == null ? "" : creadoEn.toLocalDate().toString();
    }
}
