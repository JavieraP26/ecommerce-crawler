package com.crawler.ecommerce.application.port.out;

import com.crawler.ecommerce.domain.model.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida para operaciones CRUD de {@link Product}.
 * Abstrae infraestructura (JPA, MongoDB, Redis) de capa aplicación.
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
     * @param source "MercadoLibre", "Paris"
     * @return Producto único
     */
    Optional<Product> findBySkuAndSource(String sku, String source);

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
     * @param source "MercadoLibre", "Paris"
     * @param available solo disponibles
     * @return Productos listos para venta
     */
    List<Product> findAllBySourceAndAvailable(String source, boolean available);

    /**
     * SKUs existentes por source (deduplicación crawling).
     *
     * @param source Sitio origen
     * @return Set SKUs para evitar duplicados
     */
    Set<String> findAllSkusBySource(String source);

    /**
     * Verifica existencia SKU+source (evita duplicados).
     *
     * @param sku SKU candidato
     * @param source Sitio origen
     * @return true si ya existe
     */
    boolean existsBySkuAndSource(String sku, String source);

    /**
     * Limpia productos viejos por source (retención 30 días).
     *
     * @param source Sitio origen
     * @param cutoff Fecha límite
     */
    void deleteAllBySourceAndUpdatedBefore(String source, LocalDateTime cutoff);


}
