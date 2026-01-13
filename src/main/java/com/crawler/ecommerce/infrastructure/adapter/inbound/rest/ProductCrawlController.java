package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.application.port.in.CrawlProductUseCasePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para crawling de productos individuales y batch.
 *
 * Expone el caso de uso CrawlProductUseCasePort a través de HTTP/JSON:
 * - Crawling de productos individuales (fichas completas)
 * - Procesamiento batch de múltiples URLs
 * - Operación asíncrona con respuesta inmediata
 *
 * Diseñado para integración flexible:
 * - Soporte para query parameters (individual) y JSON body (batch)
 * - Validación de URLs y límites de procesamiento
 * - Métricas de procesamiento (guardados vs fallidos)
 * - Logging detallado para debugging y monitoreo
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ADAPTER INBOUND
 *
 * Este controlador sigue principios de Hexagonal Architecture:
 *
 * - ADAPTER INBOUND: Convierte HTTP/JSON a llamadas de dominio
 * - DESACOPLAMIENTO: No contiene lógica de negocio
 * - DELEGACIÓN PURA: Todos los casos de uso se delegan a CrawlProductUseCasePort
 * - VALIDACIÓN: Usa anotaciones @Validated y validaciones manuales
 *
 * Los endpoints están diseñados para:
 * - Integración con schedulers de productos individuales
 * - Procesamiento batch de categorías completas
 * - Testing y debugging de scraping de productos
 * - Monitorización de métricas de éxito/fracaso
 * ------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Validated
public class ProductCrawlController {

    private final CrawlProductUseCasePort crawlProductUseCase;

    /**
     * Endpoint para crawling y persistencia de un producto individual.
     * 
     * Procesa una URL completa de ficha de producto.
     * Respuesta asíncrona - el procesamiento ocurre en background.
     * 
     * @param url URL completa de la ficha del producto a scrapear
     * @return Confirmación de recepción del pedido de crawling
     */
    @PostMapping("/product")
    public ResponseEntity<Map<String, Object>> crawlProduct(@RequestParam @NotBlank String url) {
        log.info("API: Crawl producto: {}", url);
        crawlProductUseCase.crawlProduct(url);
        return ResponseEntity.ok(Map.of("success", true, "message", "Crawleado/persistido", "url", url));
    }

    /**
     * Endpoint para crawling y persistencia de múltiples productos.
     * 
     * Procesa una lista de URLs en batch.
     * Ideal para procesar categorías completas o listados grandes.
     * 
     * @param urls Lista de URLs de productos a scrapear
     * @return Confirmación con cantidad de productos guardados exitosamente
     */
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> crawlProducts(@RequestBody @NotEmpty @Size(min = 1, max = 100) List<String> urls) {
        log.info("API: Crawl batch: {} URLs", urls.size());
        
        int savedCount = crawlProductUseCase.crawlProducts(urls);
        
        return ResponseEntity.ok(Map.of(
                "success", true, 
                "processed", urls.size(),
                "saved", savedCount,
                "failed", urls.size() - savedCount
        ));
    }
}
