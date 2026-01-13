package com.crawler.ecommerce.application.port.out;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.domain.model.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida para persistencia de {@link Product}.
 *
 * Abstrae infraestructura (JPA, MongoDB, Redis) de la capa de aplicación.
 * Define operaciones necesarias para crawling, deduplicación y batch processing.
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DISEÑO ORIENTADO A CASOS DE USO
 *
 * Este RepositoryPort no pretende ser un CRUD genérico.
 * Expone operaciones específicas del dominio de crawling de productos:
 *
 * - deduplicación por SKU + source,
 * - persistencia batch por página,
 * - consultas operativas para crawling incremental,
 * - limpieza por políticas de retención.
 *
 * Aunque algunas operaciones podrían modelarse como save() sobre la entidad
 * completa, se opta por métodos explícitos por razones pragmáticas:
 *
 * - EFICIENCIA: evita rehidratar entidades completas en procesos batch.
 * - CLARIDAD SEMÁNTICA: expresa acciones concretas del dominio
 *   (ej: "existe SKU", "limpiar productos antiguos").
 * - ALINEACIÓN CON CASOS DE USO: el port refleja lo que la aplicación
 *   necesita del almacenamiento, no la estructura de la base de datos.
 *
 * CONSISTENCIA:
 * Este diseño es intencionalmente simétrico a CategoryRepositoryPort.
 *
 * REFACTOR FUTURO:
 * Si aparecen múltiples casos de uso con patrones similares,
 * estas operaciones podrán agruparse bajo servicios de dominio
 * o comandos de más alto nivel.
 * ------------------------------------------------------------------------
 */

public interface ProductRepositoryPort {

    /**
     * Persiste producto ORQUESTADO por Use Case.
     * Use case debe verificar existencia antes.
     * Si existe por SKU+source, actualiza precios/available/images.
     *
     * @param product Producto crawleado
     * @return Producto persistido con ID generado
     */
    Product save(Product product);

    /**
     * Busca producto por SKU único.
     * Útil para detectar duplicados en crawling.
     * @param sku SKU del sitio origen (ej: "MLA19813486")
     * @return Producto si existe, empty si no
     */
    Optional<Product> findBySku(String sku);

    /**
     * Busca por SKU+source (evita colisiones cross-site).
     * @param sku SKU producto
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return Producto único
     */
    Optional<Product> findBySkuAndSource(String sku, MarketplaceSource source);

    /**
     * Elimina producto por SKU (soft-delete o hard).
     * @param sku SKU a eliminar
     */
    void deleteBySku(String sku);

    /**
     * Guarda lista de productos en batch (paginación).
     *
     * @param products Productos de una página
     * @return Lista persistida
     */
    List<Product> saveAll(List<Product> products);

    /**
     * Lista productos disponibles por source.
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param available solo disponibles
     * @return Productos listos para venta
     */
    List<Product> findAllBySourceAndAvailable(MarketplaceSource source, boolean available);

    /**
     * SKUs existentes por source (deduplicación crawling).
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return Set SKUs para evitar duplicados
     */
    Set<String> findAllSkusBySource(MarketplaceSource source);

    /**
     * Verifica existencia SKU+source (evita duplicados).
     *
     * @param sku SKU candidato
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @return true si ya existe
     */
    boolean existsBySkuAndSource(String sku, MarketplaceSource source);

    /**
     * Limpia productos viejos por source (retención 30 días).
     *
     * @param source MarketplaceSource (MERCADO_LIBRE, PARIS, FALABELLA)
     * @param cutoff Fecha límite
     */
    void deleteAllBySourceAndUpdatedBefore(MarketplaceSource source, LocalDateTime cutoff);

    /**
     * SKUs existentes por categoría (anti-duplicados París infinite scroll).
     */
    List<String> findSkusByCategory(Long categoryId);

}
