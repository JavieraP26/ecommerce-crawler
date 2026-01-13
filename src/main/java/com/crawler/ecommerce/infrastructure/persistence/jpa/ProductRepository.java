package com.crawler.ecommerce.infrastructure.persistence.jpa;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository para operaciones CRUD y específicas de {@link Product}.
 *
 * Implementa persistencia para ProductRepositoryPort con optimizaciones avanzadas:
 * - Operaciones CRUD estándar mediante JpaRepository
 * - Consultas específicas del dominio de crawling
 * - Queries nativas JPQL para performance crítica
 * - EntityGraph para evitar N+1 en relaciones
 * - Índices optimizados para deduplicación y búsquedas
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — REPOSITORY JPA
 *
 * Este repository sigue principios de diseño robustos:
 *
 * - CONVENCIONES SPRING DATA: Métodos derivados automáticamente
 * - CONSULTAS OPTIMIZADAS: JPQL nativo para operaciones específicas
 * - ÍNDICES APROVECHADOS: Usa idx_product_sku, idx_product_source, etc.
 * - EVITACIÓN N+1: EntityGraph para precargar relaciones
 * - TRANSACCIONES DECLARATIVAS: @Transactional en operaciones de modificación
 *
 * Las consultas están diseñadas para:
 * - Operaciones batch eficientes para crawling masivo
 * - Deduplicación por SKU + source para evitar duplicados cross-site
 * - Actualizaciones parciales sin cargar entidades completas
 * - Limpieza de datos por políticas de retención
 * - Consultas específicas del dominio de e-commerce
 * ------------------------------------------------------------------------
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca producto por SKU único.
     * Utiliza índice idx_product_sku para optimización.
     *
     * @param sku SKU del sitio origen
     * @return Producto si existe, empty si no
     */
    Optional<Product> findBySku(String sku);

    /**
     * SKUs únicos por categoría (anti-duplicados infinite scroll).
     *
     * @param categoryId ID categoría
     * @return Lista SKUs existentes
     */
    @Query("SELECT DISTINCT p.sku FROM Product p WHERE p.category.id = :categoryId")
    List<String> findAllSkusByCategoryId(Long categoryId);

    /**
     * Busca producto por SKU y source para evitar colisiones cross-site.
     *
     * @param sku SKU del producto
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return Producto único si existe
     */
    Optional<Product> findBySkuAndSource(String sku, MarketplaceSource source);

    /**
     * Verifica existencia de producto por SKU y source.
     * Más eficiente que findBySkuAndSource cuando solo se necesita verificación.
     *
     * @param sku SKU candidato
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return true si existe
     */
    boolean existsBySkuAndSource(String sku, MarketplaceSource source);

    /**
     * Lista productos por source y disponibilidad.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param available Estado disponibilidad
     * @return Lista productos filtrados
     */
    List<Product> findAllBySourceAndAvailable(MarketplaceSource source, boolean available);

    /**
     * Obtiene set de SKUs existentes por source para deduplicación.
     * Requiere custom query JPQL para proyección.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return Set SKUs únicos
     */
    @Query("SELECT DISTINCT p.sku FROM Product p WHERE p.source = :source")
    Set<String> findAllSkusBySource(@Param("source") MarketplaceSource source);

    /**
     * Cuenta productos por source y disponibilidad.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param available Estado disponibilidad
     * @return Total productos
     */
    long countBySourceAndAvailable(MarketplaceSource source, boolean available);

    /**
     * Obtiene productos con categoría precargada (evita N+1).
     * Usar cuando se requiera acceso a product.category.
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.source = :source")
    List<Product> findAllBySourceWithCategory(@Param("source") MarketplaceSource source);

    /**
     * Elimina producto por SKU.
     *
     * @param sku SKU a eliminar
     */
    @Modifying
    @Transactional
    void deleteBySku(String sku);

    /**
     * Elimina todos los productos de un source específico.
     * Útil para limpieza completa de sitio.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     */
    @Modifying
    @Transactional
    void deleteBySource(MarketplaceSource source);

    /**
     * Elimina productos viejos por source según cutoff date.
     * Para retención de datos.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param cutoff Fecha límite
     */
    @Modifying
    @Transactional
    void deleteAllBySourceAndUpdatedAtBefore(MarketplaceSource source, LocalDateTime cutoff);




}
