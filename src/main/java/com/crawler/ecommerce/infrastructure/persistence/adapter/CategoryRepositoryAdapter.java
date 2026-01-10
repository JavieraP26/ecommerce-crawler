package com.crawler.ecommerce.infrastructure.persistence.adapter;

import com.crawler.ecommerce.application.port.out.CategoryRepositoryPort;
import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;
import com.crawler.ecommerce.infrastructure.persistence.jpa.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de CategoryRepositoryPort usando Spring Data JPA.
 * Adapter que conecta el puerto de salida con la implementación JPA.
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
    public List<Category> findPendingCategories(String source) {
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
    public void markCrawlingComplete(String sourceUrl, LocalDateTime completedAt) {
        categoryRepository.markCrawlingComplete(sourceUrl, completedAt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countBySource(String source) {
        return categoryRepository.countBySource(source);
    }
}
