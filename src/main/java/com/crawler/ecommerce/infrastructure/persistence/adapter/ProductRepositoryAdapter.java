package com.crawler.ecommerce.infrastructure.persistence.adapter;

import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.domain.model.Product;
import com.crawler.ecommerce.infrastructure.persistence.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementación de ProductRepositoryPort usando Spring Data JPA.
 * Adapter que conecta el puerto de salida con la implementación JPA.
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
    public Optional<Product> findBySkuAndSource(String sku, String source) {
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
    @Override
    public List<Product> saveAll(List<Product> products) {
        return productRepository.saveAll(products);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAllBySourceAndAvailable(String source, boolean available) {
        return productRepository.findAllBySourceAndAvailable(source, available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> findAllSkusBySource(String source) {
        return productRepository.findAllSkusBySource(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsBySkuAndSource(String sku, String source) {
        return productRepository.existsBySkuAndSource(sku, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllBySourceAndUpdatedBefore(String source, LocalDateTime cutoff) {
        productRepository.deleteAllBySourceAndUpdatedAtBefore(source, cutoff);
    }
}
