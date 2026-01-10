package com.crawler.ecommerce.application.service;

import com.crawler.ecommerce.application.port.in.CrawlCategoryUseCasePort;
import com.crawler.ecommerce.application.port.out.CategoryRepositoryPort;
import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.domain.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso de crawling de categorías.
 * Orquesta extracción de categorías, detección de paginación y persistencia.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlCategoryService implements CrawlCategoryUseCasePort {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    // TODO: Inyectar ScraperService cuando exista

    /**
     * Crawlea página específica de categoría.
     * Método público para permitir retry/debug de páginas individuales.
     * Útil cuando falla crawling parcial y se requiere reprocesar solo una página.
     */
    @Override
    public void crawlCategory(String categoryUrl) {
        log.info("Iniciando crawling de categoría completa: {}", categoryUrl);

        // TODO: Extraer categoría con scraper
        // Category category = scraperService.scrapeCategory(categoryUrl);

        // TODO: Detectar totalPages
        // int totalPages = scraperService.detectTotalPages(categoryUrl);

        // TODO: Verificar si categoría existe
        // Optional<Category> existing = categoryRepositoryPort.findBySourceUrl(categoryUrl);
        // if (existing.isPresent()) {
        //     log.info("Categoría ya existe, actualizando paginación");
        //     categoryRepositoryPort.updateTotalPages(categoryUrl, totalPages);
        //     return;
        // }

        // TODO: Persistir nueva categoría
        // categoryRepositoryPort.save(category);

        // TODO: Crawlear todas las páginas
        // for (int page = 1; page <= totalPages; page++) {
        //     crawlCategoryPage(categoryUrl, page);
        // }

        log.info("Categoría crawleada exitosamente (STUB)");
    }

    @Override
    public void crawlCategoryPage(String categoryUrl, int pageNumber) {
        log.info("Crawling página {} de categoría: {}", pageNumber, categoryUrl);

        // TODO: Construir URL paginada
        // String pagedUrl = categoryUrl + "?page=" + pageNumber;

        // TODO: Extraer productos de la página
        // List<Product> products = scraperService.scrapeProductsFromPage(pagedUrl);

        // TODO: Persistir productos batch
        // productRepositoryPort.saveAll(products);

        log.info("Página {} crawleada exitosamente (STUB)", pageNumber);
    }
}
