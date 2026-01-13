package com.crawler.ecommerce.infrastructure.scraper.product.strategy;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.extract.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Strategy específica para Falabella.cl con coordinación de extractors.
 *
 * Implementa scraping adaptado a las características particulares de Falabella.cl:
 * - Coordina 5 extractors especializados (SKU, título, precio, imágenes, disponibilidad)
 * - Selectores CSS específicos y configurables via YAML
 * - Maneja tradeoffs con URLs dinámicas y SKU generation
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — STRATEGY CONCRETA
 *
 * Esta implementación sigue el patrón Strategy con características particulares:
 *
 * - COORDINACIÓN: Orquesta múltiples extractors especializados
 * - CONFIGURACIÓN YAML: Selectores configurables para adaptación a cambios
 * - TRADEOFF HANDLING: Maneja decisiones de diseño específicas
 * - MARKETPLACE ABSTRACTION: Aísla lógica particular de Falabella.cl
 * - EXTRACTORS INTEGRADOS: Reutilización de componentes modulares
 *
 * Las características particulares manejadas son:
 * - URLs de producto dinámicas (requieren validación especial)
 * - Estructura HTML que cambia frecuentemente (requiere mantenimiento)
 * - Protección anti-bot que requiere headers específicos
 * - Detección de disponibilidad con keywords específicas
 * - Generación de SKUs sintéticos para productos sin identificadores
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FalabellaProductStrategy implements ProductScrapingStrategy {

    // Extractors inyectados
    private final SkuExtractor skuExtractor;
    private final TitleExtractor titleExtractor;
    private final PriceExtractor priceExtractor;
    private final ImageExtractor imageExtractor;
    private final AvailabilityExtractor availabilityExtractor;

    // Selectores inyectados (yml)
    @Value("${app.scraper.falabella.selectors.product-name:.pod-title, h1.product-title}")
    private String detailNameSelector;

    @Value("${app.scraper.falabella.selectors.product-current-price:[data-testid='price-current'], .price-current}")
    private String detailCurrentPriceSelector;

    @Value("${app.scraper.falabella.selectors.product-previous-price:.price-previous, .price-old}")
    private String detailPreviousPriceSelector;

    @Value("${app.scraper.falabella.selectors.product-images:.product-gallery img, .product-images img}")
    private String productImagesSelector;

    @Value("${app.scraper.falabella.selectors.item:[data-testid='pod']}")
    private String itemSelector;

    @Override
    public ScrapedProduct extractFromListing(Element item, MarketplaceSource source) {
        // SKU (igual original)
        String sku = skuExtractor.extractFromListing(item);
        if (sku == null) return null;

        // Link validation (igual original)
        var linkElement = item.selectFirst("a[href]");
        if (linkElement == null) {
            log.warn("Elemento incompleto, SKU: {}", sku);
            return null;
        }

        return ScrapedProduct.builder()
                .sku(sku)
                .name(titleExtractor.extract(item, ".pod-title, h1, h2, h3, .product-name"))
                .currentPrice(priceExtractor.extract(item, "[data-testid='price-current'], .price-current, .price"))
                .previousPrice(priceExtractor.extractPrevious(item))
                .images(imageExtractor.extractFromListing(item, "img"))
                .available(availabilityExtractor.extractFromListing(item))
                .sourceUrl(linkElement.attr("href"))
                .source(source)
                .build();
    }

    @Override
    public ScrapedProduct extractFromDetail(Document doc, String productUrl, MarketplaceSource source) {
        // SKU con fallback
        String sku = skuExtractor.extractFromUrl(productUrl);
        if (sku == null) {
            sku = skuExtractor.extractFromDom(doc);
        }
        if (sku == null) {
            log.error("No SKU en ficha: {}", productUrl);
            return null;
        }

        // Name con fallback
        String name = titleExtractor.extract(doc, detailNameSelector, "h1", "h2", ".product-title");

        // Precios
        BigDecimal previousPrice = priceExtractor.extract(doc, detailPreviousPriceSelector);
        BigDecimal currentPrice = priceExtractor.extract(doc, detailCurrentPriceSelector);

        // Imágenes + disponible
        List<String> images = imageExtractor.extractFromDetail(doc, productImagesSelector);
        boolean available = availabilityExtractor.extractFromDetail(doc);

        return ScrapedProduct.builder()
                .sku(sku)
                .name(name)
                .currentPrice(currentPrice)
                .previousPrice(previousPrice)
                .images(images)
                .available(available)
                .sourceUrl(productUrl)
                .source(source)
                .build();
    }

    @Override
    public MarketplaceSource source() {
        return MarketplaceSource.FALABELLA;
    }

    @Override
    public boolean matchesUrl(String url) {
        return url.toLowerCase().contains("falabella.com");
    }

    @Override
    public List<ScrapedProduct> scrapeProductsPage(Document doc) {
        return doc.select(itemSelector).stream()
                .map(item -> extractFromListing(item, source()))
                .filter(Objects::nonNull)
                .toList();
    }
}
