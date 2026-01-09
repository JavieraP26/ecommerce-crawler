package com.crawler.ecommerce.application.port.in;

/**
 * Use case crawling categoría completa (paginación).
 * Maneja todas páginas hasta completado.
 */
public interface CrawlCategoryUseCasePort {

    /**
     * Crawlea categoría completa.
     * Para cada página → crawlProduct batch
     */
    void crawlCategory(String categoryUrl);

    /**
     * Crawlea página específica de categoría.
     * Útil para test/debug paginación.
     */
    void crawlCategoryPage(String categoryUrl, int pageNumber);

}
