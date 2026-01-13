package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.application.port.in.CrawlCategoryUseCasePort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Controlador REST para crawling de categorías (fire-and-forget).
 *
 * Expone el caso de uso CrawlCategoryUseCasePort a través de HTTP/JSON:
 * - Inicia crawling completo de categorías (todas las páginas)
 * - Permite crawling de páginas específicas (debug/retry)
 * - Opera en modo asíncrono con respuesta inmediata
 *
 * Diseñado para integración con sistemas externos:
 * - Respuesta inmediata para evitar timeouts HTTP
 * - Procesamiento en background con logging detallado
 * - Soporte para query parameters y JSON body
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER INBOUND
 *
 * Este controlador sigue principios de Hexagonal Architecture:
 *
 * - ADAPTER INBOUND: Convierte HTTP/JSON a llamadas de dominio
 * - DESACOPLAMIENTO: No contiene lógica de negocio
 * - DELEGACIÓN PURA: Todos los casos de uso se delegan a CrawlCategoryUseCasePort
 * - VALIDACIÓN: Usa @Validated y validaciones manuales
 *
 * Los endpoints están diseñados para:
 * - Integración con schedulers externos
 * - Debugging y reintentos manuales
 * - Testing de selectores sin persistencia
 * ------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Validated
public class CategoryCrawlController {

    private final CrawlCategoryUseCasePort crawlCategoryUseCase;

    @Data
    public static class CategoryCrawlRequest {
        @NotBlank
        private String url;
    }

    /**
     * Crawling completo de categoría (todas las páginas).
     * Fire-and-forget: Inicia y responde inmediatamente.
     */
    @PostMapping("/category")
    public ResponseEntity<Map<String, Object>> crawlCategory(
            @RequestParam(required = false) String url,
            @RequestBody(required = false) CategoryCrawlRequest request) {
        // Prioridad: query param > JSON body
        String categoryUrl = url != null && !url.isBlank() 
                ? url 
                : (request != null ? request.getUrl() : null);
        
        if (categoryUrl == null || categoryUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "URL requerida",
                    "message", "Proporciona 'url' como query param o en JSON body: {\"url\": \"...\"}",
                    "example_query", "/api/crawl/category?url=https://www.paris.cl/categoria",
                    "example_body", "{\"url\": \"https://www.paris.cl/categoria\"}"
            ));
        }
        
        log.info("API → Crawl categoría completa: {}", categoryUrl);
        crawlCategoryUseCase.crawlCategory(categoryUrl);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Crawl categoría iniciado (fire-and-forget)",
                "url", categoryUrl,
                "pages", "Detectando..."
        ));
    }

    /**
     * Crawling UNA página específica (debug/retry).
     * Fire-and-forget: Útil para reprocesar páginas fallidas.
     */
    @PostMapping("/category-page")
    public ResponseEntity<Map<String, Object>> crawlCategoryPage(
            @RequestParam @NotBlank String categoryUrl,
            @RequestParam @Min(1) int pageNumber) {
        log.info("API → Crawl página {}/{}", categoryUrl, pageNumber);
        crawlCategoryUseCase.crawlCategoryPage(categoryUrl, pageNumber);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Página iniciada (fire-and-forget)",
                "url", categoryUrl,
                "page", pageNumber
        ));
    }
}
