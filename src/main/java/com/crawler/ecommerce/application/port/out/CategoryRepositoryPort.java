package com.crawler.ecommerce.application.port.out;


import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones CRUD de {@link Category}.
 * Soporta tracking de paginación y estado de crawling.
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
    List<Category> findPendingCategories(String source);

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
     * @param completedAt Timestamp finalización
     */
    void markCrawlingComplete(String sourceUrl, LocalDateTime completedAt);

    /**
     * Conteo categorías por source (dashboard).
     *
     * @param source Sitio origen
     * @return Total categorías
     */
    long countBySource(String source);


}
