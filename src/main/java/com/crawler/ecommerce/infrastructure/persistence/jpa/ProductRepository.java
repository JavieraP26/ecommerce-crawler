package com.crawler.ecommerce.infrastructure.persistence.jpa;

import com.crawler.ecommerce.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository para operaciones CRUD de {@link Product}.
 * Métodos siguen naming conventions de Spring Data JPA.
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
     * Busca producto por SKU y source para evitar colisiones cross-site.
     *
     * @param sku SKU del producto
     * @param source Sitio origen
     * @return Producto único si existe
     */
    Optional<Product> findBySkuAndSource(String sku, String source);

    /**
     * Verifica existencia de producto por SKU y source.
     * Más eficiente que findBySkuAndSource cuando solo se necesita verificación.
     *
     * @param sku SKU candidato
     * @param source Sitio origen
     * @return true si existe
     */
    boolean existsBySkuAndSource(String sku, String source);

    /**
     * Lista productos por source y disponibilidad.
     *
     * @param source Sitio origen
     * @param available Estado disponibilidad
     * @return Lista productos filtrados
     */
    List<Product> findAllBySourceAndAvailable(String source, boolean available);

    /**
     * Obtiene set de SKUs existentes por source para deduplicación.
     * Requiere custom query JPQL para proyección.
     *
     * @param source Sitio origen
     * @return Set SKUs únicos
     */
    @Query("SELECT DISTINCT p.sku FROM Product p WHERE p.source = :source")
    Set<String> findAllSkusBySource(@Param("source") String source);

    /**
     * Cuenta productos por source y disponibilidad.
     *
     * @param source Sitio origen
     * @param available Estado disponibilidad
     * @return Total productos
     */
    long countBySourceAndAvailable(String source, boolean available);

    /**
     * Elimina producto por SKU.
     *
     * @param sku SKU a eliminar
     */
    void deleteBySku(String sku);

    /**
     * Elimina todos los productos de un source específico.
     * Útil para limpieza completa de sitio.
     *
     * @param source Sitio origen
     */
    void deleteBySource(String source);

    /**
     * Elimina productos viejos por source según cutoff date.
     * Para retención de datos.
     *
     * @param source Sitio origen
     * @param cutoff Fecha límite
     */
    void deleteAllBySourceAndUpdatedAtBefore(String source, LocalDateTime cutoff);




}
