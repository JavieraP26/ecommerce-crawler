package com.crawler.ecommerce.application.port.in;

import java.util.List;

/**
 * Use case para crawling individual y batch de productos.
 *
 * Orquesta el proceso completo de extracción de productos:
 * - Scraping de fichas individuales o listados
 * - Validación y normalización de datos extraídos
 * - Deduplicación por SKU + source
 * - Persistencia con upsert (crear o actualizar)
 *
 * Soporta dos modos de operación:
 * - Individual: Procesamiento detallado con logging completo
 * - Batch: Procesamiento masivo con tolerancia a fallos
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DISEÑO ORIENTADO A CASOS DE USO
 *
 * Este Use Case Port define el contrato público del caso de uso
 * sin exponer detalles de implementación:
 *
 * - FLEXIBILIDAD: Soporta tanto crawling individual como batch
 * - ROBUSTEZ: El batch processing continúa ante fallos individuales
 * - DESACOPLAMIENTO: Application layer no conoce detalles de scraping
 * - SEMÁNTICA CLARA: Métodos que expresan intenciones del dominio
 *
 * El método batch retorna int para métricas operativas,
 * mientras el individual retorna void (efecto colateral en BD).
 * ------------------------------------------------------------------------
 */
public interface CrawlProductUseCasePort {

    /**
     * Crawlea producto desde URL.
     * scraper → validación → repository.save()
     */
    void crawlProduct(String productUrl);

    /**
     * Crawlea múltiples productos (test/debug).
     * Retorna cantidad de productos guardados exitosamente.
     */
    int crawlProducts(List<String> productUrls);

}
