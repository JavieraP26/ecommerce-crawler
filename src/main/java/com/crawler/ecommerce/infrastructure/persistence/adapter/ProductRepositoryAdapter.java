package com.crawler.ecommerce.infrastructure.persistence.adapter;

import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.domain.model.Product;
import com.crawler.ecommerce.infrastructure.persistence.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador que implementa ProductRepositoryPort usando Spring Data JPA.
 *
 * Convierte el contrato de aplicación (ProductRepositoryPort) en operaciones
 * concretas de persistencia usando JPA/Hibernate:
 * - Delega operaciones CRUD al ProductRepository JPA
 * - Implementa métodos específicos del dominio de crawling
 * - Maneja transacciones batch y operaciones masivas
 * - Optimiza consultas con JPQL e índices
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER OUTBOUND
 *
 * Este adaptador sigue el patrón Adapter de Hexagonal Architecture:
 *
 * - CONTRATO ESTABLE: Application layer depende solo del puerto
 * - IMPLEMENTACIÓN JPA: Usa Spring Data JPA como tecnología concreta
 * - INVERSIÓN DE DEPENDENCIAS: Infraestructura depende del contrato
 * - DELEGACIÓN PURA: Métodos directos sin lógica adicional
 *
 * El adaptador permite:
 * - Testing unitario con mocks del puerto
 * - Cambio de tecnología de persistencia sin modificar aplicación
 * - Optimizaciones específicas de JPA (queries nativas)
 * - Manejo transaccional declarativo con Spring
 * - Operaciones batch eficientes para crawling masivo
 * ------------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductRepository productRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findBySkuAndSource(String sku, MarketplaceSource source) {
        return productRepository.findBySkuAndSource(sku, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBySku(String sku) {
        productRepository.deleteBySku(sku);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<Product> saveAll(List<Product> products) {
        return productRepository.saveAll(products);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAllBySourceAndAvailable(MarketplaceSource source, boolean available) {
        return productRepository.findAllBySourceAndAvailable(source, available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> findAllSkusBySource(MarketplaceSource source) {
        return productRepository.findAllSkusBySource(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsBySkuAndSource(String sku, MarketplaceSource source) {
        return productRepository.existsBySkuAndSource(sku, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllBySourceAndUpdatedBefore(MarketplaceSource source, LocalDateTime cutoff) {
        productRepository.deleteAllBySourceAndUpdatedAtBefore(source, cutoff);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findSkusByCategory(Long categoryId) {
        return productRepository.findAllSkusByCategoryId(categoryId);
    }

}
