package com.calsoft.seguridad;

import java.util.List;
import java.util.Map;

/**
 * Roles del sistema y módulos que ve cada uno (HU-01).
 * El backend aplica los permisos en SecurityConfig; esta lista se envía al
 * frontend al iniciar sesión para que el menú muestre solo lo permitido.
 */
public final class Roles {

    public static final String ADMIN = "Administrador";
    public static final String ALMACEN = "Almacén";
    public static final String COCINA = "Cocina";
    public static final String MESERO = "Mesero";
    public static final String EMPLEADO = "Empleado";

    public static final List<String> TODOS = List.of(ADMIN, ALMACEN, COCINA, MESERO, EMPLEADO);

    private static final Map<String, List<String>> MODULOS = Map.of(
            ADMIN, List.of("dashboard", "pedidos", "cocina", "reservas", "insumos", "categorias", "carta",
                    "alertas", "prediccion", "kardex", "compras", "recepcion", "proveedores", "usuarios", "reportes"),
            ALMACEN, List.of("dashboard", "insumos", "categorias", "alertas", "kardex", "compras", "recepcion"),
            COCINA, List.of("dashboard", "cocina", "insumos", "carta"),
            MESERO, List.of("dashboard", "pedidos", "reservas"),
            EMPLEADO, List.of("dashboard", "pedidos", "reservas"));

    private Roles() {
    }

    public static List<String> modulosDe(String rol) {
        return MODULOS.getOrDefault(rol, List.of("dashboard"));
    }
}
