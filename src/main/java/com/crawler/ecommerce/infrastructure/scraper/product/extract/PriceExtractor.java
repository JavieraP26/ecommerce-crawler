package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
