package com.crawler.ecommerce.infrastructure.adapter.outbound.factory;

import com.crawler.ecommerce.application.port.out.ProductScraperPort;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.ProductScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador que implementa ProductScraperPort usando la infraestructura de scraping.
 * 
 * Convierte la dependencia concreta (ProductScraper con Jsoup/estrategias)
 * en el contrato de aplicación (ProductScraperPort).
 * 
 * Patrón Adapter: Permite que la capa de aplicación use scraping
 * sin depender de detalles de implementación específicos.
 * 
 * Clean Architecture: La capa de aplicación solo conoce interfaces,
 * no clases concretas de infraestructura.
 */
@Component
@RequiredArgsConstructor
public class ProductScraperAdapter implements ProductScraperPort {

    private final ProductScraper productScraper;

    /**
     * Delega el scraping de producto individual a la infraestructura.
     * 
     * @param productUrl URL completa de la ficha del producto
     * @return ScrapedProduct con datos extraídos o null si falla
     */
    @Override
    public ScrapedProduct scrapeProduct(String productUrl) {
        return productScraper.scrapeProduct(productUrl);
    }

    /**
     * Delega el scraping de página de listados a la infraestructura.
     * 
     * @param pageUrl URL de la página de listado/categoría
     * @return Lista de productos válidos (con SKU no nulo)
     */
    @Override
    public List<ScrapedProduct> scrapeProductsPage(String pageUrl) {
        return productScraper.scrapeProductsPage(pageUrl);
    }
}
