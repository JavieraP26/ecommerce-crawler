package com.crawler.ecommerce.infrastructure.scraper.category;

import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import com.crawler.ecommerce.infrastructure.scraper.category.strategy.CategoryScrapingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Orquestador principal de scraping de categorías con Strategy Pattern.
 *
 * Coordina el proceso completo de extracción de categorías:
 * - Auto-detección de estrategia por marketplace (URL pattern matching)
 * - Conexión HTTP con Jsoup (headers anti-bot, timeout)
 * - Delegación a estrategia específica para extracción
 * - Manejo robusto de errores y logging estructurado
 *
 * Diseñado como punto de entrada único para scraping de categorías:
 * - API pública y estable para capas superiores
 * - Encapsulación de complejidad de múltiples marketplaces
 * - Manejo centralizado de configuración y errores
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
public class CategoryScraper {

    private final List<CategoryScrapingStrategy> strategies;

    /**
     * API pública: Auto-detecta strategy por URL.
     */
    public ScrapedCategoryPage scrapeCategoryPage(String categoryUrl) {
        try {
            CategoryScrapingStrategy strategy = resolveStrategy(categoryUrl);
            log.info("Strategy detectada: [{}] para URL: {}", strategy.source(), categoryUrl);

            Document doc;
            try {
                log.info("Conectando a: {}", categoryUrl);
                doc = connect(categoryUrl);
                log.info("Conexión exitosa, tamaño HTML: {} bytes", doc.html().length());
            } catch (IOException e) {
                log.error("Error de conexión Jsoup para {}: {}", categoryUrl, e.getMessage(), e);
                throw new RuntimeException("No se pudo conectar a " + categoryUrl, e);
            }

            ScrapedCategoryPage result = strategy.extractCategoryPage(doc, categoryUrl);
            
            if (result == null) {
                log.error("Strategy retornó null para: {}", categoryUrl);
                return null;
            }
            
            log.info("Scraping completado: {} productos extraídos", result.getProducts().size());
            return result;
            
        } catch (Exception e) {
            log.error("Error scraping {}: {}", categoryUrl, e.getMessage(), e);
            return null;
        }
    }

    public int detectTotalPages(String categoryUrl) {
        return Optional.ofNullable(scrapeCategoryPage(categoryUrl))
                .map(ScrapedCategoryPage::getTotalPages)
                .orElse(1);
    }

    private Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .followRedirects(true)
                .get();
    }

    /** RESUELVE strategy por URL domain */
    private CategoryScrapingStrategy resolveStrategy(String categoryUrl) {
        return strategies.stream()
                .filter(s -> s.matchesUrl(categoryUrl))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No CategoryScrapingStrategy para: " + categoryUrl));
    }
}
