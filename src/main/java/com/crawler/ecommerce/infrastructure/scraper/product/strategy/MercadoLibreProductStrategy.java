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

/**
 * Strategy específica Mercado Libre.
 * Coordina 5 extractors inyectados.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MercadoLibreProductStrategy implements ProductScrapingStrategy {

    // Extractors inyectados
    private final SkuExtractor skuExtractor;
    private final TitleExtractor titleExtractor;
    private final PriceExtractor priceExtractor;
    private final ImageExtractor imageExtractor;
    private final AvailabilityExtractor availabilityExtractor;

    // Selectores inyectados (yml)
    @Value("${app.scraper.mercadolibre.selectors.name:.ui-search-item__title}")
    private String listingNameSelector;

    @Value("${app.scraper.mercadolibre.selectors.price:.andes-money-amount__fraction}")
    private String listingPriceSelector;

    @Value("${app.scraper.mercadolibre.selectors.product-name:h1.ui-pdp-title}")
    private String detailNameSelector;

    @Value("${app.scraper.mercadolibre.selectors.product-current-price:.ui-pdp-price__second-line .andes-money-amount__fraction}")
    private String detailCurrentPriceSelector;

    @Value("${app.scraper.mercadolibre.selectors.product-previous-price:.andes-money-amount--previous .andes-money-amount__fraction}")
    private String detailPreviousPriceSelector;

    @Value("${app.scraper.mercadolibre.selectors.product-images:figure.ui-pdp-gallery__figure img}")
    private String productImagesSelector;

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
                .name(titleExtractor.extract(item, listingNameSelector))
                .currentPrice(priceExtractor.extract(item, listingPriceSelector))
                .previousPrice(priceExtractor.extractPrevious(item))
                .images(imageExtractor.extractFromListing(item, "img[src]"))  // Configurable futuro
                .available(availabilityExtractor.extractFromListing(item))
                .sourceUrl(linkElement.attr("href"))
                .source(MarketplaceSource.MERCADO_LIBRE)
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
        String name = titleExtractor.extract(doc, detailNameSelector, "h1.ui-pdp-variations__subtitle");

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
                .source(MarketplaceSource.MERCADO_LIBRE)
                .build();
    }
}
