package com.crawler.ecommerce.infrastructure.scraper.product.strategy;

import com.crawler.ecommerce.domain.model.MarketplaceSource;

/**
 * Interface para estrategias de scraping de productos.
 * Cada implementación = 1 e-commerce (MercadoLibre, Paris.cl, etc.).
 *
 * Separa lógica específica por sitio del ProductScraper (orquestador).
 *
 * extractFromListing(): Para páginas de categoría/listado (múltiples productos).
 * extractFromDetail(): Para fichas individuales (1 producto detallado).
 */
public interface ProductScrapingStrategy {

    /**
     * Extrae 1 producto desde Element HTML de listado/categoría.
     *
     * @param item Element individual del producto en página de resultados
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return ScrapedProduct completo o null si falla validación (SKU, nombre, link)
     */
    com.crawler.ecommerce.infrastructure.dto.ScrapedProduct extractFromListing(
            org.jsoup.nodes.Element item,
            MarketplaceSource source);

    /**
     * Extrae 1 producto desde página completa de ficha/detalle.
     *
     * @param doc Document HTML completo de la ficha individual
     * @param productUrl URL original solicitada
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return ScrapedProduct detallado o null si no encuentra SKU
     */
    com.crawler.ecommerce.infrastructure.dto.ScrapedProduct extractFromDetail(
            org.jsoup.nodes.Document doc,
            String productUrl,
            MarketplaceSource source);
}
