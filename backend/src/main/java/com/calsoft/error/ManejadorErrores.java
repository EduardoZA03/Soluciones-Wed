package com.calsoft.error;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones en respuestas JSON {"mensaje": "..."} con el
 * código HTTP adecuado, para que el frontend muestre un mensaje claro.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = Logger.getLogger(ManejadorErrores.class.getName());

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, String>> validacion(ValidacionException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Errores de @Valid (@NotBlank, @Pattern, ...): se devuelve el primero. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getDefaultMessage()).orElse("Datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato de datos inválido");
    }

    /** Clave duplicada o registro en uso por otra tabla. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT, "No se pudo guardar: el registro está duplicado o en uso por otros datos");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> general(Exception e) {
        LOG.log(Level.SEVERE, "Error no controlado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor");
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
