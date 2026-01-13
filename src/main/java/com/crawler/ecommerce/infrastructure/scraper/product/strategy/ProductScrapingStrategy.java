package com.crawler.ecommerce.infrastructure.scraper.product.strategy;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Interfaz Strategy para scraping de productos por marketplace.
 *
 * Define el contrato que cada implementación específica debe cumplir:
 * - Identificación única del marketplace soportado
 * - Verificación de compatibilidad de URLs
 * - Extracción de productos individuales y de listados
 * - Soporte para scraping de páginas completas de productos
 *
 * Cada strategy representa un marketplace con características particulares:
 * - MercadoLibre: Estructura estable, formato MLA\d+, precios con descuentos
 * - Paris.cl: Data attributes, selectores específicos, scroll infinito
 * - Falabella: Anti-bot protection, URLs dinámicas, SKU generation
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — STRATEGY PATTERN
 *
 * Esta interfaz sigue el patrón Strategy de GoF:
 *
 * - CONTRATO ESTABLE: Define operaciones que todas las estrategias deben implementar
 * - EXTENSIBILIDAD: Fácil agregar nuevos marketplaces
 * - INTERCAMBIABILIDAD: Estrategias pueden intercambiarse en runtime
 * - DESACOPLAMIENTO: Aísla lógica específica de cada marketplace
 * - DUALIDAD SOPORTE: Listing vs Detail según contexto de uso
 *
 * Las operaciones están diseñadas para:
 * - Extracción de productos individuales (fichas completas)
 * - Extracción desde listados (múltiples productos en categoría)
 * - Scraping de páginas completas de productos (paginación)
 * - Retorno estructurado (ScrapedProduct) para orquestador
 * - Manejo de errores específicos por marketplace
 * ------------------------------------------------------------------------
 */
public interface ProductScrapingStrategy {

    /**
     * Identifica el marketplace de esta strategy.
     * @return MERCADO_LIBRE, PARIS, FALABELLA
     */
    MarketplaceSource source();

    /**
     * Verifica si la URL es compatible con esta estrategia.
     * @param url URL a verificar
     * @return true si la URL es compatible, false en caso contrario
     */
    boolean matchesUrl(String url);

    /**
     * Extrae 1 producto desde Element HTML de listado/categoría.
     *
     * @param item Element individual del producto en página de resultados
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return ScrapedProduct completo o null si falla validación (SKU, nombre, link)
     */
    ScrapedProduct extractFromListing(
            Element item,
            MarketplaceSource source);

    /**
     * Extrae 1 producto desde página completa de ficha/detalle.
     *
     * @param doc Document HTML completo de la ficha individual
     * @param productUrl URL original solicitada
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return ScrapedProduct detallado o null si no encuentra SKU
     */
    ScrapedProduct extractFromDetail(
            Document doc,
            String productUrl,
            MarketplaceSource source);

    List<ScrapedProduct> scrapeProductsPage(Document doc);
}
