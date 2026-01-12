package com.crawler.ecommerce.application.service;

import com.crawler.ecommerce.application.port.in.CrawlProductUseCasePort;
import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.application.port.out.ProductScraperPort;  // NUEVO
import com.crawler.ecommerce.domain.model.Product;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicación que implementa el caso de uso de crawling de productos.
 * 
 * Flujo completo: URL → Scraping → Mapeo a dominio → Persistencia (upsert).
 * Servicio de aplicación (gestionado por Spring solo para wiring de dependencias).
 * 
 * @see ProductScraperPort Para detalles de implementación de scraping
 * @see ProductRepositoryPort Para detalles de persistencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlProductService implements CrawlProductUseCasePort {

    private final ProductScraperPort productScraperPort;
    private final ProductRepositoryPort productRepositoryPort;

    /**
     * Procesa un producto individual: scraping, validación y persistencia.
     * 
     * Flujo: Validar URL → Scrapear → Validar datos → Mapear a dominio → Persistir (upsert).
     * 
     * @param productUrl URL completa de la ficha del producto a procesar
     */
    @Override
    public void crawlProduct(String productUrl) {
        log.info("Crawling producto individual: {}", productUrl);

        ScrapedProduct scraped = productScraperPort.scrapeProduct(productUrl);
        if (scraped == null) {
            log.warn("Scrape falló (null SKU): {}", productUrl);
            return;
        }

        Product product = toDomain(scraped);
        productRepositoryPort.findBySkuAndSource(product.getSku(), product.getSource())
                .ifPresentOrElse(
                        existing -> {
                            log.info("Upsert → Actualizando: {}", product.getSku());
                            updateFromScraped(existing, scraped);
                            productRepositoryPort.save(existing);
                        },
                        () -> {
                            log.info("Upsert → Nuevo: {}", product.getSku());
                            productRepositoryPort.save(product);
                        }
                );
    }

    /**
     * Procesa múltiples URLs en batch con procesamiento secuencial.
     * 
     * Itera sobre cada URL y delega al procesamiento individual.
     * No lanza excepciones para no interrumpir el procesamiento del resto del batch.
     * 
     * @param productUrls Lista de URLs de productos a procesar
     */
    @Override
    public void crawlProducts(List<String> productUrls) {
        log.info("Crawling batch: {} URLs", productUrls.size());
        productUrls.forEach(this::crawlProduct);
        log.info("Batch completado");
    }

    /**
     * Convierte ScrapedProduct (DTO de infraestructura) a Product (entidad de dominio).
     * 
     * Crea una nueva instancia inmutable del dominio sin efectos secundarios.
     * Preserva todos los campos extraídos por el scraper incluyendo imágenes.
     * 
     * @param scraped DTO con datos extraídos del scraping
     * @return Entidad de dominio lista para persistencia
     */
    private Product toDomain(ScrapedProduct scraped) {
        return Product.builder()
                .sku(scraped.getSku())
                .name(scraped.getName())
                .currentPrice(scraped.getCurrentPrice())
                .previousPrice(scraped.getPreviousPrice())
                .available(scraped.isAvailable())
                .source(scraped.getSource())
                .sourceUrl(scraped.getSourceUrl())
                .images(scraped.getImages())
                .build();
    }

    /**
     * Actualiza campos volátiles del producto existente con datos del scraping.
     * 
     * Preserva los timestamps de auditoría de JPA (createdAt/updatedAt).
     * Actualiza solo los campos que pueden cambiar entre scrapings:
     * - Nombre del producto
     * - Precios (current/previous)
     * - Disponibilidad
     * - Lista de imágenes
     * 
     * @param existing Producto existente en base de datos
     * @param scraped Datos nuevos extraídos del scraping
     */
    private void updateFromScraped(Product existing, ScrapedProduct scraped) {
        existing.setName(scraped.getName());
        existing.setCurrentPrice(scraped.getCurrentPrice());
        existing.setPreviousPrice(scraped.getPreviousPrice());
        existing.setAvailable(scraped.isAvailable());
        existing.setImages(scraped.getImages());
    }
}
