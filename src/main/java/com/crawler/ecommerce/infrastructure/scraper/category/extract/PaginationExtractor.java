package com.crawler.ecommerce.infrastructure.scraper.category.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extrae solo totalPages desde información de paginación.
 *
 * Responsabilidad específica: Detección de paginación, no cuenta productos.
 * ProductListExtractor maneja productsPerPage (separación de responsabilidades).
 *
 * Implementa múltiples estrategias de detección:
 * - Selector principal configurable via YAML (@Value)
 * - Texto de paginador específico ("Página X de Y")
 * - Último link de paginación
 * - Cálculo basado en count de productos (lazy loading)
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - SINGLE RESPONSIBILITY: Solo detecta paginación, no extrae productos
 * - CONFIGURACIÓN EXTERNA: Selectores via YAML, no hardcodeados
 * - MULTIPLE STRATEGIES: Detección por diferentes patrones HTML
 * - LAZY LOADING SUPPORT: Cálculo basado en count de productos
 *
 * Las estrategias de detección siguen esta prioridad:
 * - 1. Selector principal (configurable)
 * - 2. Texto paginador específico (ej: "Página 2 de 50")
 * - 3. Último link de paginación
 * - 4. Default: 1 página si no se detecta nada
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
public class PaginationExtractor {

    /**
     * Extrae totalPages desde paginador.
     *
     * @param doc             Document HTML página categoría
     * @param primarySelector Selector paginación (@Value yml)
     * @return Total páginas >= 1
     */
    public int extractTotalPages(Document doc, String primarySelector) {
        // 1. Selector principal
        Integer totalPages = extractFromSelector(doc, primarySelector);
        if (totalPages != null) {
            log.debug("Total pages desde selector principal: {}", totalPages);
            return totalPages;
        }

        // 2. Fallback: Texto paginador específico
        totalPages = extractFromPaginationText(doc);
        if (totalPages != null) {
            log.debug("Total pages detectado desde texto del paginador: {}", totalPages);
            return totalPages;
        }

        // 3. Fallback: Último link
        totalPages = extractFromLastPaginationLink(doc);
        if (totalPages != null) {
            log.debug("Total pages desde último link de paginación: {}", totalPages);
            return totalPages;
        }

        log.warn("No se detectó paginación, asumiendo 1 página");
        return 1;
    }

    private Integer extractFromSelector(Document doc, String selector) {
        Element element = doc.selectFirst(selector);
        return element != null ? parsePageNumber(element.text()) : null;
    }

    /**
     * Busca "Página X de Y" SOLO en zona paginador (no todo el doc).
     * Evita falsos positivos de números aleatorios en página.
     */
    private Integer extractFromPaginationText(Document doc) {
        Element paginationArea = doc.selectFirst(".pagination, [class*='page'], nav[aria-label*='paginación']");
        if (paginationArea == null)
            return null;

        String pageText = paginationArea.text().toLowerCase();
        var matcher = java.util.regex.Pattern.compile("página\\s*(\\d+)\\s*(de|of)\\s*(\\d+)")
                .matcher(pageText);
        return matcher.find() ? Integer.valueOf(matcher.group(3)) : null;
    }

    private Integer extractFromLastPaginationLink(Document doc) {
        Element lastLink = doc.selectFirst(".pagination li:last-child a, [class*='page'] a:last-child");
        return lastLink != null ? parsePageNumber(lastLink.text()) : null;
    }

    private Integer parsePageNumber(String text) {
        if (text == null)
            return null;
        try {
            String clean = text.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? null : Integer.valueOf(clean);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Calcula totalPages basado en total de productos y productos por página.
     * Útil para sitios con "lazy loading" (Ej: "Has visto 30 de 1029 productos").
     *
     * @param doc                Document HTML
     * @param totalCountSelector Selector del elemento con el total (Ej: "1029
     *                           productos")
     * @param productsPerPage    Cantidad de productos en la primera página
     * @return Total páginas calculado o null si falla
     */
    public Integer calculateTotalPagesFromProductCount(Document doc, String totalCountSelector, int productsPerPage) {
        if (productsPerPage <= 0)
            return null;

        Element totalEl = doc.selectFirst(totalCountSelector);
        if (totalEl == null)
            return null;

        String text = totalEl.text();
        log.debug("Texto total productos encontrado: '{}'", text);

        // Patrón específico para "X de Y productos" o "Has visto X de Y"
        java.util.regex.Matcher lazyMatcher = java.util.regex.Pattern.compile(
                "(?:visto|mostrado)?\\s*(\\d+)\\s*(?:de|of)\\s*(\\d+)\\s*(?:productos|results)?", 
                java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (lazyMatcher.find()) {
            int shown = Integer.parseInt(lazyMatcher.group(1));
            int total = Integer.parseInt(lazyMatcher.group(2));
            
            int totalPages = (int) Math.ceil((double) total / productsPerPage);
            log.info("Lazy loading detectado: {} de {} productos → {} páginas ({} por página)",
                    shown, total, totalPages, productsPerPage);
            
            return totalPages;
        }

        // Fallback: Busca el número más grande (método anterior)
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(text);

        int maxNumber = 0;
        boolean found = false;

        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            if (num > maxNumber) {
                maxNumber = num;
                found = true;
            }
        }

        if (!found || maxNumber == 0)
            return null;

        int totalPages = (int) Math.ceil((double) maxNumber / productsPerPage);
        log.info("Cálculo paginación fallback: Total productos {} / Por página {} = {} páginas",
                maxNumber, productsPerPage, totalPages);

        return totalPages;
    }
}