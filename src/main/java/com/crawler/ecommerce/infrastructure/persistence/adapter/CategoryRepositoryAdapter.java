package com.crawler.ecommerce.infrastructure.persistence.adapter;

import com.crawler.ecommerce.application.port.out.CategoryRepositoryPort;
import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;
import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.infrastructure.persistence.jpa.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa CategoryRepositoryPort usando Spring Data JPA.
 *
 * Convierte el contrato de aplicación (CategoryRepositoryPort) en operaciones
 * concretas de persistencia usando JPA/Hibernate:
 * - Delega operaciones CRUD al CategoryRepository JPA
 * - Implementa métodos específicos del dominio de crawling
 * - Maneja transacciones y actualizaciones parciales
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
 * ------------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> findBySourceUrl(String sourceUrl) {
        return categoryRepository.findBySourceUrl(sourceUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> findAllByStatus(CategoryStatus status) {
        return categoryRepository.findAllByStatus(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> findPendingCategories(MarketplaceSource source) {
        return categoryRepository.findAllBySourceAndStatus(source, CategoryStatus.ACTIVA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTotalPages(String sourceUrl, int totalPages) {
        categoryRepository.updateTotalPages(sourceUrl, totalPages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(String sourceUrl, CategoryStatus status) {
        categoryRepository.updateStatus(sourceUrl, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markCrawlingComplete(String sourceUrl, CategoryStatus status, LocalDateTime completedAt) {
        categoryRepository.markCrawlingComplete(sourceUrl, status, completedAt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countBySource(MarketplaceSource source) {
        return categoryRepository.countBySource(source);
    }
}
