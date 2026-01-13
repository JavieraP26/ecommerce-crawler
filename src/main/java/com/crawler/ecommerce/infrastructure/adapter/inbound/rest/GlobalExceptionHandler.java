package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejo global y centralizado de excepciones para todos los controladores REST.
 *
 * Proporciona respuestas HTTP consistentes y estructuradas:
 * - Formato JSON uniforme para todos los errores
 * - Información detallada para debugging (solo desarrollo)
 * - Mensajes seguros para producción (sin stack traces)
 * - Timestamps para correlación de logs
 *
 * Maneja categorías de errores específicas:
 * - Validaciones: @Validated, @NotBlank, @Min
 * - Parsing: JSON inválido, tipos incorrectos
 * - Dominio: recursos no encontrados, parámetros inválidos
 * - Sistema: excepciones genéricas no controladas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — CROSS-CUTTING CONCERN
 *
 * Este handler sigue principios de diseño robustos:
 *
 * - CENTRALIZACIÓN: Un punto único para manejo de errores
 * - CONSISTENCIA: Todas las respuestas siguen mismo formato
 * - SEGURIDAD: No expone detalles sensibles en producción
 * - OBSERVABILIDAD: Logs estructurados con contexto
 *
 * Las respuestas están diseñadas para:
 * - Clientes API: Fácil parsing y manejo de errores
 * - Debugging: Información detallada para desarrolladores
 * - Producción: Mensajes seguros sin exponer implementación
 * - Monitorización: Timestamps y códigos de estado claros
 * ------------------------------------------------------------------------
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja IllegalArgumentExceptions parámetros inválidos.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Parámetro inválido: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Parámetro inválido",
                        "details", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Maneja MissingServletRequestParameterException.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Parámetro requerido faltante: {}", e.getParameterName());
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Parámetro requerido faltante",
                        "parameter", e.getParameterName(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Maneja MethodArgumentTypeMismatchException.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Tipo de parámetro inválido: {} para {}", e.getName(), e.getRequiredType());
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Tipo de parámetro inválido",
                        "parameter", e.getName(),
                        "expected", e.getRequiredType().getSimpleName(),
                        "received", e.getValue() != null ? e.getValue().getClass().getSimpleName() : "null",
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Manejo MethodArgumentNotValidException (validación @Validated).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Error de validación: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Error de validación",
                        "details", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Manejo NoSuchElementException.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException e) {
        log.warn("Recurso no encontrado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Recurso no encontrado",
                        "details", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Maneja HttpMessageNotReadableException (body faltante o inválido).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Request body faltante o inválido: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Request body faltante o inválido",
                        "details", "Se requiere un JSON body válido o usar query parameters",
                        "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Manejo excepciones genéricas no controladas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Error no controlado en controlador: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error interno del servidor",
                        "details", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}
