package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Extrae y parsea precios (actual + anterior).
 */
@Component
@Slf4j
public class PriceExtractor {

    /**
     * Extrae precio con selector.
     */
    public BigDecimal extract(Element element, String selector) {
        Element priceElement = element.selectFirst(selector);
        return parsePrice(priceElement);
    }

    /**
     * Precio anterior ML (hardcode selector).
     */
    public BigDecimal extractPrevious(Element element) {
        Element oldPriceEl = element.selectFirst(".andes-money-amount--previous .andes-money-amount__fraction");
        return parsePrice(oldPriceEl);  // null si no existe
    }

    /**
     * Extrae precio con selector múltiple (separa por comas).
     * Devuelve el primer precio válido encontrado en orden de prioridad.
     */
    public BigDecimal extract(Document doc, String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            return null;
        }
        
        String[] selectors = selector.split(",");
        
        for (String sel : selectors) {
            sel = sel.trim();
            if (!sel.isEmpty()) {
                Element priceElement = doc.selectFirst(sel);
                BigDecimal price = parsePrice(priceElement);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    log.debug("Precio encontrado con selector '{}': {}", sel, price);
                    return price; // Devolver el primer precio válido encontrado
                }
            }
        }
        
        log.warn("No se pudo extraer precio con ningún selector: {}", selector);
        return null;
    }

    /**
     * Parser central (solo números).
     */
    private BigDecimal parsePrice(Element el) {
        if (el == null) return null;
        try {
            String cleanText = el.text().replaceAll("[^0-9]", "");
            return cleanText.isEmpty() ? null : new BigDecimal(cleanText);
        } catch (Exception ignored) {
            return null;
        }
    }
}
