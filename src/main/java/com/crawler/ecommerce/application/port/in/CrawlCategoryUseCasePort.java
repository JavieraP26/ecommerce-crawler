package com.crawler.ecommerce.application.port.in;

/**
 * Use case para crawling completo de categorías con paginación.
 *
 * Orquesta el proceso completo de crawling de una categoría:
 * - Detección automática de marketplace desde URL
 * - Scraping de metadata de categoría (nombre, breadcrumb, totalPages)
 * - Procesamiento iterativo de todas las páginas
 * - Coordinación con ProductRepository para persistencia batch
 *
 * Maneja casos especiales:
 * - Paris.cl: Solo primera página (scroll infinito)
 * - Otros marketplaces: Paginación tradicional
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DISEÑO ORIENTADO A CASOS DE USO
 *
 * Este Use Case Port define el contrato público del caso de uso
 * sin exponer detalles de implementación:
 *
 * - SIMPLICIDAD: Métodos simples que reflejan intenciones del dominio
 * - DESACOPLAMIENTO: Application layer no depende de infraestructura
 * - EXTENSIBILIDAD: Fácil agregar nuevos marketplaces sin modificar el contrato
 *
 * Los métodos retornan void ya que el resultado principal es el efecto
 * colateral en la base de datos (productos guardados) y el estado de la categoría.
 * ------------------------------------------------------------------------
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
