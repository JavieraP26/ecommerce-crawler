package com.crawler.ecommerce.domain.model;

/**
 * Estados posibles de una categoría durante el crawling.
 */
public enum CategoryStatus {

    /**
     * Categoría con productos disponibles para crawling.
     */
    ACTIVA,

    /**
     * Todas las páginas de la categoría han sido procesadas.
     */
    PAGINACION_COMPLETA,

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
