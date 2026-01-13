package com.crawler.ecommerce.domain.model;

import lombok.Getter;

/**
 * Fuentes de e-commerce soportadas por el crawler.
 *
 * Define los marketplaces disponibles para extracción de datos:
 * - Identifica únicamente cada sitio para evitar colisiones de SKUs
 * - Facilita routing de estrategias de scraping específicas
 * - Soporta configuración diferenciada por marketplace
 *
 * Cada marketplace tiene características particulares:
 * - MercadoLibre: Paginación tradicional, estructura estable
 * - Paris.cl: Scroll infinito, requiere Selenium
 * - Falabella: Anti-bot protection, URLs dinámicas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — VALUE OBJECT
 *
 * Este enum sigue principios de Domain-Driven Design:
 *
 * - TIPO SEGURO: El compilador garantiza valores válidos
 * - IDENTIFICADOR ÚNICO: Cada valor representa un marketplace distinto
 * - EXTENSIBILIDAD: Fácil agregar nuevos marketplaces
 *
 * El campo value facilita persistencia y serialización:
 * - Almacenamiento en base de datos como VARCHAR
 * - Serialización JSON para APIs REST
 * - Configuración YAML por marketplace
 * ------------------------------------------------------------------------
 */

@Getter
public enum MarketplaceSource {

    /**
     * MercadoLibre.
     * URLs: listado.mercadolibre.com, /p/MLAXXXXXX
     */
    MERCADO_LIBRE("MERCADO_LIBRE"),

    /**
     * Paris Chile.
     * URLs: paris.cl/categoria, paris.cl/producto
     */
    PARIS("PARIS"),

    /**
     * Falabella Chile (3er sitio).
     * URLs: falabella.com/categoria, falabella.com/producto
     */
    FALABELLA("FALABELLA");

    private final String value;

    MarketplaceSource(String value) {
        this.value = value;
    }

    /**
     * Valor para persistencia en BD.
     * @return Código para PostgreSQL VARCHAR column
     */
    public String getValue() {
        return value;
    }
}
