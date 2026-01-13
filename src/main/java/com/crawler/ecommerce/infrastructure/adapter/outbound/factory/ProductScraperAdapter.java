package com.crawler.ecommerce.infrastructure.adapter.outbound.factory;

import com.crawler.ecommerce.application.port.out.ProductScraperPort;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.ProductScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador que implementa ProductScraperPort usando infraestructura de scraping.
 *
 * Convierte el contrato de aplicación (ProductScraperPort) en llamadas
 * a la implementación concreta de infraestructura (ProductScraper):
 * - Delega scraping de productos individuales (fichas)
 * - Delega scraping de listados (categorías/búsquedas)
 * - Mantiene desacoplamiento completo entre capas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER OUTBOUND
 *
 * Este adaptador sigue el patrón Adapter de Hexagonal Architecture:
 *
 * - CONTRATO ESTABLE: Application layer depende solo del puerto
 * - IMPLEMENTACIÓN VARIABLE: Infraestructura puede cambiar sin afectar aplicación
 * - INVERSIÓN DE DEPENDENCIAS: Infraestructura depende del contrato
 * - DELEGACIÓN PURA: Métodos directos sin lógica adicional
 *
 * El adaptador permite:
 * - Testing unitario con mocks del puerto
 * - Cambio de implementación de scraping sin modificar aplicación
 * - Múltiples estrategias por marketplace (Jsoup, Selenium)
 * - Inyección de dependencias limpia con Spring
 *
 * Soporta los dos modos de scraping:
 * - Individual: Fichas completas de productos
 * - Batch: Listados de categorías o resultados de búsqueda
 * ------------------------------------------------------------------------
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
