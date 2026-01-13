package com.crawler.ecommerce.infrastructure.scraper.category.strategy;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import org.jsoup.nodes.Document;

/**
 * Interfaz Strategy para scraping de categorías/listados por marketplace.
 *
 * Define el contrato que cada implementación específica debe cumplir:
 * - Identificación única del marketplace soportado
 * - Verificación de compatibilidad de URLs
 * - Extracción de metadata y productos de una página específica
 *
 * Cada strategy representa un marketplace con características particulares:
 * - Paris.cl: Scroll infinito, requiere data attributes específicos
 * - MercadoLibre: Paginación tradicional, estructura estable
 * - Falabella: Anti-bot protection, URLs dinámicas
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
 *
 * Las operaciones están diseñadas para:
 * - Extracción de una página específica (no maneja paginación)
 * - Retorno estructurado (ScrapedCategoryPage) para orquestador
 * - Manejo de errores específicos por marketplace
 * - Configuración via YAML para selectores CSS
 * ------------------------------------------------------------------------
 */

public interface CategoryScrapingStrategy {

    /**
     * Identifica el marketplace de esta strategy.
     * @return PARIS, MERCADO_LIBRE, FALABELLA
     */
    MarketplaceSource source();

    /**
     * Verifica si la URL es compatible con esta estrategia.
     * @param url URL a verificar
     * @return true si la URL es compatible, false en caso contrario
     */
    boolean matchesUrl(String url);

    /**
     * Extrae metadata categoría + productos página ESPECÍFICA.
     * Soporta paginación (?page=2).
     *
     * @param doc HTML página (pag 1 detecta total, pag N extrae productos)
     * @param categoryUrl URL original (?page=N incluida)
     * @return Metadata + productos página
     */
    ScrapedCategoryPage extractCategoryPage(Document doc,
                                            String categoryUrl);

}
