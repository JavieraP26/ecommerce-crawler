package com.crawler.ecommerce.infrastructure.adapter.inbound.rest;

import com.crawler.ecommerce.application.port.in.CrawlProductUseCasePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de crawling de productos.
 * 
 * Expone los casos de uso de aplicación a través de HTTP/JSON.
 * No contiene lógica de negocio, solo delega a CrawlProductUseCasePort.
 * 
 * Perfiles: Disponible solo en entornos de desarrollo y scraping
 * para evitar exposición en producción.
 * 
 * @Profile({"dev", "scraper-only"}) Disponible solo en entornos de desarrollo/scraping
 */
@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Profile({"dev", "scraper-only"})
@Slf4j
@Tag(name = "Product Crawling", description = "Crawling fichas/listados + persist")
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
    @Operation(summary = "Crawl + persist producto individual")
    @PostMapping("/product")
    public ResponseEntity<Map<String, Object>> crawlProduct(@RequestParam String url) {
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
     * @return Confirmación con cantidad de URLs procesadas
     */
    @Operation(summary = "Crawl + persist batch URLs")
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> crawlProducts(@RequestBody List<String> urls) {
        log.info("API: Crawl batch: {}", urls.size());
        crawlProductUseCase.crawlProducts(urls);
        return ResponseEntity.ok(Map.of("success", true, "processed", urls.size()));
    }
}
