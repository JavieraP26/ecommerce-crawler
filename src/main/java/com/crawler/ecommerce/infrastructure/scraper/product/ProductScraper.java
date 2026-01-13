package com.crawler.ecommerce.infrastructure.scraper.product;

import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.strategy.ProductScrapingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Orquestador principal de scraping de productos con Strategy Pattern.
 *
 * Coordina el proceso completo de extracción de productos:
 * - Auto-detección de estrategia por marketplace (URL pattern matching)
 * - Conexión HTTP con Jsoup (headers anti-bot, timeout)
 * - Delegación a estrategia específica para extracción
 * - Manejo robusto de errores y logging estructurado
 *
 * Diseñado como punto de entrada único para scraping de productos:
 * - API pública y estable para capas superiores
 * - Encapsulación de complejidad de múltiples marketplaces
 * - Manejo centralizado de configuración y errores
 * - Tradeoffs documentados (ej: Falabella anti-bot protection)
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ORCHESTRATOR + STRATEGY
 *
 * Este componente sigue patrones de diseño robustos:
 *
 * - STRATEGY PATTERN: Selección dinámica de implementación por marketplace
 * - SINGLE RESPONSIBILITY: Solo orquesta, no extrae datos directamente
 * - OPEN/CLOSED PRINCIPLE: Expone API estable, oculta estrategias
 * - DEPENDENCY INJECTION: Inyección de estrategias configuradas
 * - TRADEOFF HANDLING: Maneja decisiones de diseño específicas
 *
 * El orquestador permite:
 * - Testing unitario con mock de estrategias
 * - Agregar nuevos marketplaces sin modificar código existente
 * - Cambiar implementación de Jsoup sin afectar API pública
 * - Manejo centralizado de errores y métricas
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductScraper {

    private final List<ProductScrapingStrategy> strategies;

    /**
     * Auto-detecta strategy por URL → scrape.
     */
    public ScrapedProduct scrapeProduct(String productUrl) {
        try {
            ProductScrapingStrategy strategy = resolveStrategy(productUrl);
            log.debug("Conectando a: {}", productUrl);
            Document doc = connect(productUrl);
            return strategy.extractFromDetail(doc, productUrl, strategy.source());
        } catch (Exception e) {
            log.error("Error scrapeando producto {}: {}", productUrl, e.getMessage(), e);
            return null;
        }
    }

    public List<ScrapedProduct> scrapeProductsPage(String pageUrl) {
        try {
            ProductScrapingStrategy strategy = resolveStrategy(pageUrl);
            log.info("Listing [{}] → {}", strategy.source(), pageUrl);
            Document doc = connect(pageUrl);

            return strategy.scrapeProductsPage(doc);
        } catch (Exception e) {
            log.error("Error scrapeProductsPage {}: {}", pageUrl, e.getMessage());
            return List.of();
        }
    }

    private ProductScrapingStrategy resolveStrategy(String url) {
        return strategies.stream()
                .filter(s -> s.matchesUrl(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ProductScrapingStrategy para: " + url));
    }


    private Document connect(String url) throws IOException {
        Connection connection = Jsoup.connect(url)
                .timeout(15000)
                .followRedirects(true);
        
        // Headers específicos por marketplace
        if (url.toLowerCase().contains("falabella.com")) {
            /*
             * TRADEOFF JUSTIFICADO - FALABELLA PRODUCT SCRAPER
             * 
             * PROBLEMA: Falabella bloquea requests automáticos con HTTP 403 Forbidden
             * a pesar de headers completos, cookies, delays y user-agents realistas.
             * 
             * SOLUCIÓN ADOPTADA: Deshabilitar product scraping para Falabella
             * 
             * IMPACTO:
             * Category scraping funciona (437 productos con datos completos)
             * Product scraping individual no disponible para Falabella
             * ML y Paris funcionan normalmente (headers genéricos)
             * 
             * JUSTIFICACIÓN TÉCNICA:
             * - Falabella implementa protección anti-bot avanzada (TLS fingerprint, IP blocking)
             * - Requeriría Selenium/browser real (costo alto, complejidad excesiva)
             * - Category crawling proporciona cobertura completa del catálogo
             * - Tradeoff aceptable: 437 productos vs 1 producto individual
             * 
             * ALTERNATIVAS DESCARTADAS:
             * - Selenium: Overkill para este caso de uso
             * - Proxy rotation: Complejidad innecesaria
             * - Browser automation: Mantenimiento elevado
             */
            throw new IOException("Product scraping no disponible para Falabella debido a protección anti-bot. Use category crawling para obtener productos.");
        } else {
            // Headers genéricos para otros marketplaces (ML, Paris)
            connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        }
        
        return connection.get();
    }
}
