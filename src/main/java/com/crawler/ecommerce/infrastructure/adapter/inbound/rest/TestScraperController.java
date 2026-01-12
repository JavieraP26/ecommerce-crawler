package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.domain.model.Product;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.persistence.adapter.ProductRepositoryAdapter;
import com.crawler.ecommerce.infrastructure.scraper.product.ProductScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de prueba para validar scrapers.
 * Disponibles en perfiles "local" y "scraper-only" para desarrollo y testing.
 * 
 * Ejemplos de uso:
 * - GET /api/test/product?url=https://www.mercadolibre.com.ar/.../p/MLA19813486
 * - GET /api/test/products?url=https://listado.mercadolibre.com.ar/categoria
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})  // Disponible en ambos perfiles
@Slf4j
public class TestScraperController {

    private final ProductScraper productScraper;
    private final ProductRepositoryAdapter productRepositoryAdapter;

    /**
     * Prueba scraper de producto individual (ficha de producto) + persistencia.
     * Extrae datos del producto y lo guarda en PostgreSQL.
     *
     * @param url URL completa de la ficha del producto
     * @return Producto extraído o error si falla
     * 
     * Ejemplo:
     * GET /api/test/product?url=https://www.mercadolibre.com.ar/sierra-circular-7-14-185-190mm-1600w-hs7010-makita/p/MLA19813486
     */
    @GetMapping("/product")
    public ResponseEntity<?> testProduct(@RequestParam String url) {
        log.info("Test: Extrayendo producto individual de: {}", url);

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL es requerida", "ejemplo",
                            "GET /api/test/product?url=https://www.mercadolibre.com.ar/.../p/MLA19813486"));
        }

        try {
            // 1. SCRAPE
            ScrapedProduct scraped = productScraper.scrapeProduct(url);

            if (scraped == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No se pudo extraer el producto. Verifica la URL y los logs.",
                        "url", url
                ));
            }

            // 2. MAP DTO → ENTITY
            Product product = Product.builder()
                    .sku(scraped.getSku())
                    .name(scraped.getName())
                    .currentPrice(scraped.getCurrentPrice())
                    .previousPrice(scraped.getPreviousPrice())
                    .available(scraped.isAvailable())
                    .source(scraped.getSource())
                    .sourceUrl(scraped.getSourceUrl())
                    .images(scraped.getImages())
                    .build();

            // 3. PERSIST (upsert si SKU existe)
            Product saved = productRepositoryAdapter.findBySku(scraped.getSku())  // ✅
                    .map(existing -> {
                        existing.setName(scraped.getName());
                        existing.setCurrentPrice(scraped.getCurrentPrice());
                        existing.setPreviousPrice(scraped.getPreviousPrice());
                        existing.setAvailable(scraped.isAvailable());
                        existing.setImages(scraped.getImages());
                        log.info("Actualizando producto existente: {}", existing.getSku());
                        return productRepositoryAdapter.save(existing);  // ✅
                    })
                    .orElseGet(() -> {
                        log.info("Guardando nuevo producto: {}", product.getSku());
                        return productRepositoryAdapter.save(product);  // ✅
                    });

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "product", scraped,
                    "saved", Map.of(
                            "id", saved.getId(),
                            "sku", saved.getSku(),
                            "createdAt", saved.getCreatedAt(),
                            "updatedAt", saved.getUpdatedAt()
                    ),
                    "extractedFields", Map.of(
                            "sku", scraped.getSku() != null ? scraped.getSku() : "NO EXTRAÍDO",
                            "name", scraped.getName() != null ? scraped.getName() : "NO EXTRAÍDO",
                            "currentPrice", scraped.getCurrentPrice() != null ? scraped.getCurrentPrice() : "NO EXTRAÍDO",
                            "previousPrice", scraped.getPreviousPrice() != null ? scraped.getPreviousPrice() : "NO HAY",
                            "imagesCount", scraped.getImages() != null ? scraped.getImages().size() : 0,
                            "available", scraped.isAvailable()
                    )
            ));

        } catch (Exception e) {
            log.error("Error en test de producto: {}", url, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage(),
                            "url", url
                    ));
        }
    }

    /**
     * Prueba scraper de listado de productos (página de categoría o búsqueda).
     * 
     * @param url URL de la página de listado
     * @return Lista de productos extraídos
     * 
     * Ejemplo:
     * GET /api/test/products?url=https://listado.mercadolibre.com.ar/herramientas-electricas
     */
    @GetMapping("/products")
    public ResponseEntity<?> testProducts(@RequestParam String url) {
        log.info("Test: Extrayendo productos de listado: {}", url);
        
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL es requerida", "ejemplo",
                            "GET /api/test/products?url=https://listado.mercadolibre.com.ar/categoria"));
        }

        try {
            List<ScrapedProduct> products = productScraper.scrapeProductsPage(url);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalProducts", products.size(),
                    "products", products,
                    "url", url
            ));
            
        } catch (Exception e) {
            log.error("Error en test de productos: {}", url, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage(),
                            "url", url
                    ));
        }
    }

    /**
     * Endpoint de ayuda que muestra ejemplos de uso.
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> help() {
        return ResponseEntity.ok(Map.of(
                "endpoints", Map.of(
                        "testProduct", Map.of(
                                "method", "GET",
                                "url", "/api/test/product?url={PRODUCT_URL}",
                                "ejemplo", "/api/test/product?url=https://www.mercadolibre.com.ar/sierra-circular-7-14-185-190mm-1600w-hs7010-makita/p/MLA19813486",
                                "descripcion", "Extrae datos de un producto individual (ficha de producto)"
                        ),
                        "testProducts", Map.of(
                                "method", "GET",
                                "url", "/api/test/products?url={LISTING_URL}",
                                "ejemplo", "/api/test/products?url=https://listado.mercadolibre.com.ar/herramientas-electricas",
                                "descripcion", "Extrae lista de productos de una página de categoría o búsqueda"
                        )
                ),
                "nota", "Estos endpoints solo están disponibles en perfil 'local'"
        ));
    }
}
