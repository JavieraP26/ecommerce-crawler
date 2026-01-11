package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
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

    /**
     * Prueba scraper de producto individual (ficha de producto).
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
            ScrapedProduct product = productScraper.scrapeProduct(url);
            
            if (product == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No se pudo extraer el producto. Verifica la URL y los logs.",
                        "url", url
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "product", product,
                    "extractedFields", Map.of(
                            "sku", product.getSku() != null ? product.getSku() : "NO EXTRAÍDO",
                            "name", product.getName() != null ? product.getName() : "NO EXTRAÍDO",
                            "currentPrice", product.getCurrentPrice() != null ? product.getCurrentPrice() : "NO EXTRAÍDO",
                            "previousPrice", product.getPreviousPrice() != null ? product.getPreviousPrice() : "NO HAY",
                            "imagesCount", product.getImages() != null ? product.getImages().size() : 0,
                            "available", product.isAvailable()
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
