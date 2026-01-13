package com.crawler.ecommerce.domain.model;

/**
 * Estados posibles de una categoría durante el ciclo de vida del crawling.
 *
 * Define la máquina de estados para controlar el proceso de extracción:
 * - Flujo normal: ACTIVA → COMPLETA
 * - Flujo de error: ACTIVA → ERROR_CRAWLING → ACTIVA (retry)
 * - Flujo de detección: ACTIVA → SIN_PRODUCTOS
 * - Flujo de mantenimiento: CUALQUIERA → ESTRUCTURA_CAMBIO
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — VALUE OBJECT
 *
 * Este enum sigue principios de Domain-Driven Design:
 *
 * - INMUTABILIDAD: Los estados son constantes y no tienen comportamiento mutable
 * - SIGNIFICADO DEL DOMINIO: Cada estado representa un concepto del negocio
 * - VALIDACIONES INCORPORADAS: El compilador garantiza valores válidos
 *
 * Los estados están diseñados para soportar:
 * - Scheduler de reintentos automáticos
 * - Dashboard de monitoreo de estado
 * - Lógica de recuperación ante errores
 * - Detección de cambios estructurales en los sitios
 * ------------------------------------------------------------------------
 */
public enum CategoryStatus {

    /**
     * Categoría con productos disponibles para crawling.
     */
    ACTIVA,

    /**
     * Todas las páginas de la categoría han sido procesadas.
     */
    COMPLETA,

    /**
     * Categoría sin productos o vacía.
     */
    SIN_PRODUCTOS,

    /**
     * Error durante crawling (timeout, bloqueo IP, captcha).
     */
    ERROR_CRAWLING,

    /**
     * Cambios detectados en estructura HTML (scrapers rotos).
     */
    ESTRUCTURA_CAMBIO;
}
