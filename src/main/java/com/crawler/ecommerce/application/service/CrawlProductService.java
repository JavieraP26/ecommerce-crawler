package com.crawler.ecommerce.application.service;

import com.crawler.ecommerce.application.port.in.CrawlProductUseCasePort;
import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del caso de uso de crawling de productos.
 * Orquesta extracción, validación y persistencia de productos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlProductService implements CrawlProductUseCasePort {

    private final ProductRepositoryPort productRepositoryPort;
    // TODO: Inyectar ScraperService cuando exista

    @Override
    public void crawlProduct(String productUrl) {
        log.info("Iniciando crawling de producto: {}", productUrl);

        // TODO: Extraer producto con scraper
        // Product product = scraperService.scrapeProduct(productUrl);

        // TODO: Validar producto
        // validateProduct(product);

        // TODO: Verificar duplicados
        // if (productRepositoryPort.existsBySkuAndSource(product.getSku(), product.getSource())) {
        //     log.warn("Producto ya existe: {}", product.getSku());
        //     return;
        // }

        // TODO: Persistir
        // productRepositoryPort.save(product);

        log.info("Producto crawleado exitosamente (STUB)");
    }

    @Override
    public void crawlProducts(List<String> productUrls) {
        log.info("Iniciando crawling batch de {} productos", productUrls.size());

        productUrls.forEach(this::crawlProduct);

        log.info("Crawling batch completado");
    }
}
