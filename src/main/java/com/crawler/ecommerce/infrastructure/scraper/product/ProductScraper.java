package com.crawler.ecommerce.infrastructure.scraper.product;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.strategy.ProductScrapingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Orquestador principal de scraping de productos.
 *
 * SRP: SOLO coordina (no extrae datos).
 * Usa Strategy + Extractors inyectados.
 * Configurable vía yml (itemSelector).
 * Anti-bot: headers, timeout, redirects.
 *
 * Métodos públicos = API estable (TestScraperController).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductScraper {

    private final ProductScrapingStrategy mercadoLibreStrategy;  // MercadoLibreProductStrategy

    @Value("${app.scraper.mercadolibre.source:MERCADO_LIBRE}")
    private MarketplaceSource source;

    @Value("${app.scraper.mercadolibre.selectors.item:li.ui-search-result}")
    private String itemSelector;

    /**
     * API pública: Extrae 1 producto desde ficha individual.
     * Llama strategy + extractors.
     */
    public ScrapedProduct scrapeProduct(String productUrl) {
        try {
            log.info("Scraping ficha: {}", productUrl);
            Document doc = connect(productUrl);
            return mercadoLibreStrategy.extractFromDetail(doc, productUrl, source);
        } catch (Exception e) {
            log.error("Error scrapeProduct {}: {}", productUrl, e.getMessage());
            return null;
        }
    }

    /**
     * API pública: Extrae múltiples productos desde listado.
     * Llama strategy + extractors.
     */
    public List<ScrapedProduct> scrapeProductsPage(String pageUrl) {
        try {
            log.info("Scraping listado: {}", pageUrl);
            Document doc = connect(pageUrl);

            return doc.select(itemSelector).stream()
                    .map(item -> mercadoLibreStrategy.extractFromListing(item, source))
                    .filter(Objects::nonNull)  // Filtra null SKU (igual original)
                    .toList();
        } catch (Exception e) {
            log.error("Error scrapeProductsPage {}: {}", pageUrl, e.getMessage());
            return List.of();
        }
    }

    /**
     * Conexión Jsoup reutilizable.
     * Headers anti-bot, timeout, redirects.
     */
    private Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .followRedirects(true)
                .get();
    }
}
