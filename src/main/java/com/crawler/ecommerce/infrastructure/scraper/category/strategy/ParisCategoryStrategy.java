package com.crawler.ecommerce.infrastructure.scraper.category.strategy;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import com.crawler.ecommerce.infrastructure.scraper.category.extract.CategoryTitleExtractor;
import com.crawler.ecommerce.infrastructure.scraper.category.extract.ItemsExtractor;
import com.crawler.ecommerce.infrastructure.scraper.category.extract.PaginationExtractor;
import com.crawler.ecommerce.infrastructure.scraper.category.extract.ProductListExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Strategy específica para Paris.cl con scroll infinito.
 *
 * Implementa scraping adaptado a las características particulares de Paris.cl:
 * - Scroll infinito con data attributes (data-cnstrc-item-id)
 * - Selectores CSS específicos y configurables via YAML
 * - Detección automática de página actual desde URL
 * - totalPages = 1 (siempre scroll infinito)
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — STRATEGY CONCRETA
 *
 * Esta implementación sigue el patrón Strategy con características particulares:
 *
 * - MARKETPLACE ESPECÍFICO: Lógica particular para Paris.cl
 * - SCROLL INFINITO: totalPages siempre = 1, maneja scroll dinámico
 * - DATA ATTRIBUTES: Usa data-cnstrc-item-id para identificación única
 * - CONFIGURACIÓN YAML: Selectores configurables para adaptación a cambios
 * - INTEGRACIÓN SELENIUM: Preparado para scroll con SeleniumScraperService
 *
 * Las características particulares manejadas son:
 * - Items dinámicos que aparecen con scroll (no paginación tradicional)
 * - Selectores que cambian frecuentemente en Paris.cl
 * - Detección de página actual desde parámetros URL
 * - Logging detallado para debugging de scroll infinito
 * ------------------------------------------------------------------------
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class ParisCategoryStrategy implements CategoryScrapingStrategy {

    // Extractors inyectados (SRP)
    private final CategoryTitleExtractor titleExtractor;
    private final PaginationExtractor paginationExtractor;
    private final ItemsExtractor itemsExtractor;
    private final ProductListExtractor productListExtractor;

    // Selectores configurables (yml)
    @Value("${app.scraper.paris.selectors.category-title:.category-title, h1.category-name}")
    private String categoryTitleSelector;

    @Value("${app.scraper.paris.selectors.pagination:.pagination-total, .page-info}")
    private String paginationSelector;

    @Value("${app.scraper.paris.selectors.total-products:.ui-text-caption-mobile}")
    private String totalProductsSelector;

    @Value("${app.scraper.paris.selectors.items-selector}")
    private String itemsSelector;

    @Value("${app.scraper.paris.max-scrolls:50}")
    private int maxScrolls;

    @Override
    public MarketplaceSource source() {
        return MarketplaceSource.PARIS;
    }

    @Override
    public boolean matchesUrl(String url) {
        return url.toLowerCase().contains("paris.cl");
    }

    @Override
    public ScrapedCategoryPage extractCategoryPage(Document doc, String categoryUrl) {
        log.info("Scraping [{}] → {}", source(), categoryUrl);

        // 1. Título categoría
        String name = titleExtractor.extract(doc, categoryTitleSelector);
        if (name == null || name.isBlank()) {
            log.warn("Título categoría no encontrado, usando fallback");
            name = "Categoría Paris.cl";
        }
        log.info("Título categoría: {}", name);

        // 2. Extraer items de productos
        log.info("Buscando items con selector: {}", itemsSelector);
        var items = itemsExtractor.extractItems(doc, itemsSelector);
        int productsPerPage = items.size();
        log.info("Items encontrados: {} (selector: {})", productsPerPage, itemsSelector);

        if (items.isEmpty()) {
            log.error("NO SE ENCONTRARON ITEMS CON SELECTOR: {}", itemsSelector);
            log.info("Intentando selectores alternativos...");
            // Intentar selectores alternativos para debug
            log.info("   - [data-cnstrc-item-id]: {}", doc.select("[data-cnstrc-item-id]").size());
            log.info("   - [role='gridcell']: {}", doc.select("[role='gridcell']").size());
            log.info("   - a[id^='product-']: {}", doc.select("a[id^='product-']").size());
        }

        // 3. Extraer productos desde items
        var products = productListExtractor.extractProducts(items, MarketplaceSource.PARIS);
        log.info("Productos extraídos: {} de {} items", products.size(), productsPerPage);

        // 4. Para Paris.cl: siempre totalPages = 1 (scroll infinito)
        int totalPages = 1;

        return ScrapedCategoryPage.builder()
                .name(name)
                .breadcrumb(source().name() + " > " + name)
                .totalPages(totalPages)
                .productsPerPage(productsPerPage)
                .currentPage(extractCurrentPage(doc))
                .products(products)
                .build();
    }

    /**
     * Detecta página actual desde URL o DOM.
     */
    private int extractCurrentPage(Document doc) {
        // URL param ?page=2
        String url = doc.baseUri();
        var matcher = java.util.regex.Pattern.compile("[?&]page=(\\d+)").matcher(url);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1; // Página 1 por defecto
    }

}
