package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import com.crawler.ecommerce.infrastructure.scraper.category.CategoryScraper;
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
import java.util.Map;

/**
 * Controlador REST para preview de scraping de categorías SIN persistencia.
 *
 * Permite validación y debugging de selectores CSS antes del crawling:
 * - Extrae metadata de categorías (nombre, paginación, breadcrumb)
 * - Detecta totalPages para planificación de crawling
 * - Retorna productos extraídos sin guardar en BD
 *
 * Diseñado para desarrollo y mantenimiento:
 * - Validación de selectores YAML sin afectar datos
 * - Debugging de estrategias específicas por marketplace
 * - Verificación de cambios en estructura HTML
 * - Testing de paginación detectada
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER INBOUND
 *
 * Este controlador sigue principios de Hexagonal Architecture:
 *
 * - ADAPTER INBOUND: Expone scraping directo vía HTTP
 * - AISLAMIENTO: Usa CategoryScraper directamente (bypassea casos de uso)
 * - TESTING FACILITADO: Permite validar infraestructura sin persistencia
 * - DEBUGGING: Herramienta para desarrolladores y mantenimiento
 *
 * Los endpoints están diseñados para:
 * - Validación de selectores antes de crawling masivo
 * - Detección temprana de cambios estructurales
 * - Debugging de estrategias específicas
 * - Testing de nuevos marketplaces
 * ------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/scrape-preview")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Validated
public class CategoryScrapePreviewController {

    private final CategoryScraper categoryScraper;

    /**
     * Preview UNA página categoría (metadata + productos).
     *
     * @param url URL categoría (?page=2 soportado)
     * @return ScrapedCategoryPage JSON completo
     */
    @GetMapping("/category")
    public ResponseEntity<?> previewCategory(@RequestParam @NotBlank String url) {
        log.info("Preview categoría: {}", url);


        if (url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL requerida", "ejemplo",
                            "GET /api/scrape-preview/category?url=https://paris.cl/celulares"));
        }

        try {
            ScrapedCategoryPage category = categoryScraper.scrapeCategoryPage(url);

            if (category == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No se extrajo categoría. Verifica URL/logs.",
                        "url", url
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "category", category,
                    "summary", Map.of(
                            "name", category.getName(),
                            "totalPages", category.getTotalPages(),
                            "productsPerPage", category.getProductsPerPage(),
                            "currentPage", category.getCurrentPage(),
                            "totalProducts", category.getProducts().size(),
                            "sampleProduct", category.getProducts().isEmpty() ? null : category.getProducts().get(0)
                    )
            ));

        } catch (Exception e) {
            log.error("Error preview categoría {}: {}", url, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error scraping: " + e.getMessage(), "url", url));
        }
    }

    /**
     * Preview DETECTAR total páginas (página 1).
     */
    @GetMapping("/category-pages")
    public ResponseEntity<?> previewTotalPages(@RequestParam @NotBlank String url) {
        log.info("Preview total páginas: {}", url);

        // La validación @NotBlank ya maneja null/empty, pero mantenemos la lógica adicional
        if (url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL requerida", "ejemplo",
                            "GET /api/scrape-preview/category-pages?url=https://paris.cl/celulares"));
        }

        try {
            int totalPages = categoryScraper.detectTotalPages(url);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalPages", totalPages,
                    "message", String.format("Categoría tiene %d páginas", totalPages),
                    "url", url
            ));
        } catch (Exception e) {
            log.error("Error detectando páginas {}: {}", url, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage(), "url", url));
        }
    }
}
