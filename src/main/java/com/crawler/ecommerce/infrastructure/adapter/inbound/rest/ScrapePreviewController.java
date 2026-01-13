package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.ProductScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para preview de scraping de productos SIN persistencia.
 *
 * Permite validación y debugging de scraping de productos:
 * - Extrae datos completos de fichas de productos
 * - Procesa listados de productos (categorías/búsquedas)
 * - Retorna resultados JSON sin guardar en base de datos
 *
 * Diseñado para desarrollo y mantenimiento:
 * - Validación de selectores CSS sin afectar datos
 * - Debugging de estrategias específicas por marketplace
 * - Verificación de extracción de campos (SKU, precios, imágenes)
 * - Testing de nuevos marketplaces o cambios estructurales
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER INBOUND
 *
 * Este controlador sigue principios de Hexagonal Architecture:
 *
 * - ADAPTER INBOUND: Expone scraping directo vía HTTP
 * - AISLAMIENTO: Usa ProductScraper directamente (bypassea casos de uso)
 * - TESTING FACILITADO: Permite validar infraestructura sin persistencia
 * - DEBUGGING: Herramienta esencial para desarrolladores
 *
 * Los endpoints están diseñados para:
 * - Validación de selectores antes de crawling masivo
 * - Debugging de extracción de datos específicos
 * - Testing de estrategias de scraping
 * - Verificación de cambios en estructura HTML
 * ------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/scrape-preview")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Validated
public class ScrapePreviewController {

    private final ProductScraper productScraper; // Inyección directa del scraper

    /**
     * Preview scraping producto SIN persistencia.
     * Útil para debugging y testing de selectores.
     */
@GetMapping("/product")
    public ResponseEntity<?> previewProduct(@RequestParam @NotBlank String url) {
        log.info("Preview producto: {}", url);

        // La validación @NotBlank ya maneja null/empty, pero mantenemos la lógica adicional
        if (url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL requerida", "ejemplo",
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
                    .body(Map.of("error", "Error interno durante el scraping: " + e.getMessage()));
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
    public ResponseEntity<?> testProducts(@RequestParam @NotBlank String url) {
        log.info("Extrayendo productos de listado: {}", url);

        // La validación @NotBlank ya maneja null/empty, pero mantenemos la lógica adicional
        if (url.trim().isEmpty()) {
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
