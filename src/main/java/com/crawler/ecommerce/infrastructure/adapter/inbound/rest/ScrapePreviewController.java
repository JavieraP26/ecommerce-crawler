package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;


import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.ProductScraper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Preview para validar scrapers sin persistencia.
 *
 * Permite probar la extracción de datos antes de guardarlos en BD.
 * Útil para:
 * - Validar selectores CSS
 * - Debugging de scrapers
 * - Verificar que los datos se extraen correctamente
 *
 * No persiste nada, solo retorna JSON con lo extraído para verificar un correcto funcionamiento.
 */

@RestController
@RequestMapping("/api/scrape-preview")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Tag(name = "Scrape Preview", description = "Preview de scrapers sin persistencia")
public class ScrapePreviewController {

    private final ProductScraper productScraper; // Inyección directa del scraper

    @GetMapping("/product")
    @Operation(summary = "Preview scraper de producto individual (JSON sin persistencia)")
    public ResponseEntity<?> previewProduct(@RequestParam String url) {
        log.info("Preview Scraper: Extrayendo producto individual de: {}", url);

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL es requerida", "ejemplo",
                            "GET /api/test/product?url=https://www.mercadolibre.com.ar/.../p/MLA19813486"));
        }

        try {
            // SCRAPE
            ScrapedProduct scraped = productScraper.scrapeProduct(url);

            if (scraped == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No se pudo extraer el producto. Verifica la URL y los logs.",
                        "url", url
                ));
            }


            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "product", scraped,
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
            log.error("Error durante el preview de scraping: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("Error interno durante el scraping: " + e.getMessage());
        }
    }

    /**
     * Scraper de listado de productos (página de categoría o búsqueda).
     *
     * @param url URL de la página de listado
     * @return Lista de productos extraídos
     *
     * Ejemplo:
     * GET /api/test/products?url=https://listado.mercadolibre.com.ar/herramientas-electricas
     */
    @GetMapping("/products")
    @Operation(summary = "Preview scraper de productos de una categoría (JSON sin persistencia)")
    public ResponseEntity<?> testProducts(@RequestParam String url) {
        log.info("Extrayendo productos de listado: {}", url);

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



}
