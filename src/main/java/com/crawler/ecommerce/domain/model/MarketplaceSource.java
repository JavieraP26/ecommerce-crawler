package com.crawler.ecommerce.domain.model;

import lombok.Getter;

/*
 * Fuentes de e-commerce soportadas por el crawler.
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
