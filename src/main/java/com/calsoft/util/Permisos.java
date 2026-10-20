package com.calsoft.util;

import java.util.Map;
import java.util.Set;

/**
 * Qué roles pueden entrar a cada módulo (HU-01).
 * El filtro de autenticación bloquea el acceso y la cabecera oculta
 * del menú los módulos que el rol no puede usar.
 *
 * La clave es el primer segmento de la URL: /pedidos → "pedidos".
 * Las rutas que no aparecen aquí (login, logout, inicio) son libres
 * para cualquier usuario con sesión.
 */
public final class Permisos {

    public static final String ADMIN = "Administrador";
    public static final String ALMACEN = "Almacén";
    public static final String COCINA = "Cocina";
    public static final String MESERO = "Mesero";
    public static final String EMPLEADO = "Empleado";

    private static final Map<String, Set<String>> MODULOS = Map.ofEntries(
            Map.entry("pedidos", Set.of(ADMIN, MESERO, EMPLEADO)),
            Map.entry("reservas", Set.of(ADMIN, MESERO, EMPLEADO)),
            Map.entry("cocina", Set.of(ADMIN, COCINA)),
            Map.entry("inventario", Set.of(ADMIN, ALMACEN, COCINA)),
            Map.entry("movimientos", Set.of(ADMIN, ALMACEN)),
            Map.entry("carta", Set.of(ADMIN, COCINA)),
            Map.entry("alertas", Set.of(ADMIN, ALMACEN)),
            Map.entry("prediccion", Set.of(ADMIN)),
            Map.entry("compras", Set.of(ADMIN, ALMACEN)),
            Map.entry("recepcion", Set.of(ADMIN, ALMACEN)),
            Map.entry("qr", Set.of(ADMIN, ALMACEN)),
            Map.entry("proveedores", Set.of(ADMIN)),
            Map.entry("usuarios", Set.of(ADMIN)),
            Map.entry("reportes", Set.of(ADMIN)));

    private Permisos() {
    }

    public static boolean permitido(String rol, String modulo) {
        Set<String> roles = MODULOS.get(modulo);
        return roles == null || roles.contains(rol);
    }

    /** Solo el administrador aprueba órdenes de compra (HU-06). */
    public static boolean puedeAprobar(String rol) {
        return ADMIN.equals(rol);
    }
}
