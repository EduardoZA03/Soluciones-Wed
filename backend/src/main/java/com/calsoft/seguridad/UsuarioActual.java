package com.calsoft.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Datos del usuario que hace la petición (sacados del token JWT). */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static String nombre() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null ? "sistema" : a.getName();
    }

    public static Integer id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null || !(a.getDetails() instanceof Integer i) ? null : i;
    }

    public static String rol() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null || a.getAuthorities().isEmpty() ? null : a.getAuthorities().iterator().next().getAuthority();
    }

    public static boolean esAdministrador() {
        return Roles.ADMIN.equals(rol());
    }
}
