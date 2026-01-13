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
 * Strategy específica para Falabella.cl (Chile) con anti-bot protection.
 *
 * Implementa scraping adaptado a las características particulares de Falabella.cl:
 * - Paginación tradicional explícita (?page=N)
 * - Anti-bot protection con URLs dinámicas
 * - 48 productos por página (consistente)
 * - Tradeoffs con SKU generation y URLs hardcodeadas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — STRATEGY CONCRETA
 *
 * Esta implementación sigue el patrón Strategy con tradeoffs particulares:
 *
 * - ANTI-BOT HANDLING: Maneja protección con headers y timeouts
 * - URL TRADEOFFS: URLs dinámicas requieren hardcode de fallback
 * - SKU GENERATION: SKUs sintéticos basados en hash + timestamp
 * - CONFIGURACIÓN YAML: Selectores configurables para adaptación
 *
 * Las características particulares manejadas son:
 * - URLs de producto dinámicas (no disponibles en listados)
 * - Estructura HTML que cambia frecuentemente (requiere mantenimiento)
 * - Protección anti-bot que requiere headers específicos
 * - Detección de paginación explícita (no automática)
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FalabellaCategoryStrategy implements CategoryScrapingStrategy {

    // Extractors inyectados (SRP)
    private final CategoryTitleExtractor titleExtractor;
    private final PaginationExtractor paginationExtractor;
    private final ItemsExtractor itemsExtractor;
    private final ProductListExtractor productListExtractor;

    // Selectores configurables (yml)
    @Value("${app.scraper.falabella.selectors.category.title:[data-testid=\"search-results-title\"]}")
    private String categoryTitleSelector;

    @Value("${app.scraper.falabella.selectors.category.pagination:div.pagination a.page}")
    private String paginationSelector;

    @Value("${app.scraper.falabella.selectors.category.total-products:.results-count}")
    private String totalProductsSelector;

    @Value("${app.scraper.falabella.selectors.category.items:[data-testid=\"pod\"]}")
    private String itemsSelector;

    @Value("${app.scraper.falabella.selectors.products-per-page:48}")
    private int productsPerPage;

    @Override
    public MarketplaceSource source() {
        return MarketplaceSource.FALABELLA;
    }

    @Override
    public boolean matchesUrl(String url) {
        return url.toLowerCase().contains("falabella.com");
    }

    @Override
    public ScrapedCategoryPage extractCategoryPage(Document doc, String categoryUrl) {
        log.info("Scraping [{}] → {}", source(), categoryUrl);

        // 1. Título categoría
        String name = titleExtractor.extract(doc, categoryTitleSelector);
        if (name == null || name.isBlank()) {
            log.warn("Título categoría no encontrado, usando fallback");
            name = "Categoría Falabella.cl";
        }
        log.info("Título categoría: {}", name);

        // 2. Extraer items de productos
        log.info("Buscando items con selector: {}", itemsSelector);
        var items = itemsExtractor.extractItems(doc, itemsSelector);
        int productsPerPage = items.size();
        log.info("Items encontrados: {} (selector: {})", productsPerPage, itemsSelector);

        if (items.isEmpty()) {
            log.error("NO SE ENCONTRARON ITEMS CON SELECTOR: {}", itemsSelector);
            log.debug("Detectando páginas para: {}", categoryUrl);
            // Intentar selectores alternativos para debug
            log.info("   - [data-testid=\"pod\"]: {}", doc.select("[data-testid=\"pod\"]").size());
            log.info("   - .product-card: {}", doc.select(".product-card").size());
            log.info("   - [data-testid*=\"product\"]: {}", doc.select("[data-testid*=\"product\"]").size());
        }

        // 3. Extraer productos desde items
        var products = productListExtractor.extractProducts(items, MarketplaceSource.FALABELLA);
        log.info("Productos extraídos: {} de {} items", products.size(), productsPerPage);

        // 4. Para Falabella: detectar paginación real
        int totalPages = paginationExtractor.extractTotalPages(doc, paginationSelector);
        if (totalPages <= 0) {
            log.debug("Sin paginación detectada, usando página 1");
            totalPages = 1;
        }
        log.debug("Páginas detectadas: {}", totalPages);

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
