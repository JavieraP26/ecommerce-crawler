package com.crawler.ecommerce.infrastructure.adapter.outbound.factory;

import com.crawler.ecommerce.application.port.out.CategoryScraperPort;
import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import com.crawler.ecommerce.infrastructure.scraper.category.CategoryScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa CategoryScraperPort usando infraestructura de scraping.
 *
 * Convierte el contrato de aplicación (CategoryScraperPort) en llamadas
 * a la implementación concreta de infraestructura (CategoryScraper):
 * - Delega scraping de páginas de categorías
 * - Delega detección de paginación
 * - Mantiene desacoplamiento entre capas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER OUTBOUND
 *
 * Este adaptador sigue el patrón Adapter de Hexagonal Architecture:
 *
 * - CONTRATO ESTABLE: Application layer depende solo del puerto
 * - IMPLEMENTACIÓN VARIABLE: Infraestructura puede cambiar sin afectar aplicación
 * - INVERSIÓN DE DEPENDENCIAS: Infraestructura depende del contrato
 * - DELEGACIÓN SIMPLE: Métodos directos sin lógica adicional
 *
 * El adaptador permite:
 * - Testing unitario con mocks del puerto
 * - Cambio de implementación de scraping sin modificar aplicación
 * - Múltiples estrategias de scraping por marketplace
 * - Inyección de dependencias limpia con Spring
 * ------------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
public class CategoryScraperAdapter implements CategoryScraperPort {

    private final CategoryScraper categoryScraper;

    /**
     * Delega a infra (mismo contrato).
     */
    @Override
    public ScrapedCategoryPage scrapeCategoryPage(String categoryUrl) {
        return categoryScraper.scrapeCategoryPage(categoryUrl);
    }

    /**
     * Delega detección paginación.
     */
    @Override
    public int detectTotalPages(String categoryUrl) {
        return categoryScraper.detectTotalPages(categoryUrl);
    }
}
