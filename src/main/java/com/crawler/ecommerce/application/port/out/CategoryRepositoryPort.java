package com.crawler.ecommerce.application.port.out;


import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;
import com.crawler.ecommerce.domain.model.MarketplaceSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de categorías crawleadas.
 *
 * Este port expone operaciones orientadas al dominio de crawling.
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DISEÑO ORIENTADO A CASOS DE USO
 *
 * Este RepositoryPort define métodos específicos para:
 * - tracking de estado de crawling,
 * - control de paginación detectada,
 * - consultas operativas para batch/scheduler.
 *
 * Aunque algunos métodos podrían modelarse como save() sobre la entidad
 * completa, se opta por comandos específicos por razones pragmáticas:
 *
 * - EFICIENCIA: evita rehidratar entidades completas en procesos batch.
 * - CLARIDAD DE INTENCIÓN: expresa explícitamente acciones del dominio
 *   (ej: "marcar crawling completado").
 * - ALINEACIÓN CON CASOS DE USO: el port refleja lo que la aplicación
 *   necesita del almacenamiento, no la estructura de la base de datos.
 *
 * REFACTOR FUTURO:
 * Si el dominio evoluciona hacia flujos más complejos o transaccionales,
 * estas operaciones podrán agruparse bajo comandos de más alto nivel.
 * ------------------------------------------------------------------------
 */


public interface CategoryRepositoryPort {

    /**
     * Persiste o actualiza categoría por sourceUrl única.
     * Use case debe verificar existencia antes
     *
     * @param category Categoría crawleada
     * @return Categoría persistida
     */
    Category save(Category category);

    /**
     * Busca categoría por sourceUrl (paginación).
     *
     * @param sourceUrl URL completa categoría
     * @return Categoría si existe
     */
    Optional<Category> findBySourceUrl(String sourceUrl);

    /**
     * Categorías pendientes por estado (para scheduler).
     *
     * @param status Estado deseado (ej: ACTIVA)
     * @return Lista para procesar
     */
    List<Category> findAllByStatus(CategoryStatus status);

    /**
     * Categorías pendientes por source.
     *
     * @param source "MercadoLibre", "Paris"
     * @return Categorías ACTIVA del sitio
     */
    List<Category> findPendingCategories(MarketplaceSource source);

    /**
     * Actualiza totalPages detectado en paginación.
     *
     * @param sourceUrl Categoría
     * @param totalPages Total detectado
     */
    void updateTotalPages(String sourceUrl, int totalPages);

    /**
     * Actualiza estado de crawling.
     *
     * @param sourceUrl Categoría
     * @param status Nuevo estado
     */
    void updateStatus(String sourceUrl, CategoryStatus status);

    /**
     * Marca crawling completado.
     *
     * @param sourceUrl Categoría
     * @param status Estado final (ej: CategoryStatus.COMPLETA)
     * @param completedAt Timestamp finalización
     */
    void markCrawlingComplete(String sourceUrl, CategoryStatus status, LocalDateTime completedAt);

    /**
     * Conteo categorías por source (dashboard).
     *
     * @param source Sitio origen
     * @return Total categorías
     */
    long countBySource(MarketplaceSource source);


}
