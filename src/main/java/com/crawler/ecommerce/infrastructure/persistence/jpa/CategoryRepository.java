package com.crawler.ecommerce.infrastructure.persistence.jpa;

import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository para operaciones CRUD de {@link Category}.
 * Implementa persistencia para CategoryRepositoryPort.
 * Métodos siguen naming conventions de Spring Data JPA.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca categoría por sourceUrl única (indexada).
     *
     * @param sourceUrl URL completa de categoría
     * @return Categoría si existe
     */
    Optional<Category> findBySourceUrl(String sourceUrl);

    /**
     * Lista categorías por estado específico.
     *
     * @param status Estado de crawling
     * @return Lista categorías filtradas
     */
    List<Category> findAllByStatus(CategoryStatus status);

    /**
     * Lista categorías pendientes (ACTIVA) por source.
     *
     * @param source Sitio origen
     * @return Categorías listas para crawling
     */
    List<Category> findAllBySourceAndStatus(String source, CategoryStatus status);

    /**
     * Actualiza totalPages de categoría específica.
     * * Utiliza JPQL UPDATE para performance.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.totalPages = :totalPages WHERE c.sourceUrl = :sourceUrl")
    int updateTotalPages(@Param("sourceUrl") String sourceUrl, @Param("totalPages") int totalPages);

    /**
     * Actualiza estado de crawling de categoría.
     *
     * @param sourceUrl Categoría objetivo
     * @param status Nuevo estado
     * @return Filas afectadas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.status = :status WHERE c.sourceUrl = :sourceUrl")
    int updateStatus(@Param("sourceUrl") String sourceUrl, @Param("status") CategoryStatus status);

    /**
     * Marca categoría como crawling completado.
     *
     * @param sourceUrl Categoría
     * @param completedAt Timestamp finalización
     * @return Filas afectadas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.status = 'PAGINACION_COMPLETA', c.lastCrawledAt = :completedAt WHERE c.sourceUrl = :sourceUrl")
    int markCrawlingComplete(@Param("sourceUrl") String sourceUrl, @Param("completedAt") LocalDateTime completedAt);

    /**
     * Cuenta categorías por source.
     *
     * @param source Sitio origen
     * @return Total categorías
     */
    long countBySource(String source);
}
