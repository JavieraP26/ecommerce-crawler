package com.crawler.ecommerce.infrastructure.persistence.jpa;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
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
     * Elimina producto por SKU.
     *
     * @param sku SKU a eliminar
     */
    void deleteBySku(String sku);

    /**
     * Elimina todos los productos de un source específico.
     * Útil para limpieza completa de sitio.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     */
    void deleteBySource(MarketplaceSource source);

    /**
     * Elimina productos viejos por source según cutoff date.
     * Para retención de datos.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param cutoff Fecha límite
     */
    void deleteAllBySourceAndUpdatedAtBefore(MarketplaceSource source, LocalDateTime cutoff);




}
