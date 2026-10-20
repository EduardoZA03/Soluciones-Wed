package com.calsoft;

import com.calsoft.config.BaseDeDatos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de CalSoft.
 * Antes de iniciar Spring crea la base de datos "restaurante" si no existe.
 */
@SpringBootApplication
public class CalSoftApplication {

    public static void main(String[] args) {
        BaseDeDatos.crearSiNoExiste();
        SpringApplication.run(CalSoftApplication.class, args);
    }
}
