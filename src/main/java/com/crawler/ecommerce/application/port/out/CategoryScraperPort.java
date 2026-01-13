package com.crawler.ecommerce.application.port.out;

import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;


/**
 * Puerto de salida para scraping de categorías.
 *
 * Desacopla completamente la aplicación de implementación técnica:
 * - Jsoup para parsing HTML estático
 * - Selenium para lazy loading (Paris.cl)
 * - Estrategias específicas por marketplace
 *
 * Define un contrato limpio que CrawlCategoryService puede usar
 * sin conocer detalles de infraestructura HTTP, selectores CSS,
 * o manejo de JavaScript.
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — PATRÓN ADAPTER
 *
 * Este puerto sigue el patrón Adapter de Hexagonal Architecture:
 *
 * - CONTRATO ESTABLE: La aplicación depende solo de esta interfaz
 * - IMPLEMENTACIÓN VARIABLE: Cada marketplace tiene su propia estrategia
 * - PRINCIPIO DE DEPENDENCIA: Application layer no depende de detalles
 * - TESTING FACILITADO: Facilita mocks y pruebas unitarias
 *
 * Los métodos retornan DTOs (ScrapedCategoryPage) que contienen
 * datos estructurados listos para mapeo al dominio, evitando
 * que la aplicación conozca estructuras HTML específicas.
 * ------------------------------------------------------------------------
 */
public interface CategoryScraperPort {

    /**
     * Extrae metadata + productos desde página de categoría/listado.
     *
     * @param categoryUrl URL página categoría (?page=2 soportado)
     * @return ScrapedCategoryPage completa o null si falla
     */
    ScrapedCategoryPage scrapeCategoryPage(String categoryUrl);

    /**
     * Detecta total páginas desde página 1 (planificación crawling).
     *
     * @param categoryUrl URL página 1 de categoría
     * @return totalPages >= 1
     */
    int detectTotalPages(String categoryUrl);
}
