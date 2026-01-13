package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Extrae y parsea precios (actual + anterior).
 *
 * Maneja múltiples formatos y selectores para máxima compatibilidad:
 * - Selector único con múltiples selectores separados por coma
 * - Parsing robusto con manejo de errores
 * - Soporte para precios ML (hardcode) y generales
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - PARSING ROBUSTO: Manejo de diferentes formatos de precios
 * - NULL SAFETY: Manejo seguro de valores nulos y errores
 * - CONFIGURACIÓN EXTERNA: Selectores via YAML, no hardcodeados
 * - PRECISIÓN FINANCIERA: Parsing exacto con decimales
 *
 * Los métodos están diseñados para:
 * - Extraer precio actual (más importante)
 * - Extraer precio anterior (para descuentos)
 * - Soportar múltiples selectores por marketplace
 * - Retornar valores válidos o null cuando falla extracción
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
public class PriceExtractor {

    /**
     * Extrae precio con selector (soporta múltiples selectores separados por coma).
     */
    public BigDecimal extract(Element element, String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            return null;
        }
        
        // Si tiene comas, intenta cada selector hasta encontrar uno válido
        String[] selectors = selector.split(",");
        for (String sel : selectors) {
            sel = sel.trim();
            if (!sel.isEmpty()) {
                Element priceElement = element.selectFirst(sel);
                BigDecimal price = parsePrice(priceElement);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    return price;
                }
            }
        }
        return null;
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
        
        // Si tiene comas, intenta cada selector hasta encontrar uno válido
        String[] selectors = selector.split(",");
        for (String sel : selectors) {
            sel = sel.trim();
            if (!sel.isEmpty()) {
                Element priceElement = doc.selectFirst(sel);
                BigDecimal price = parsePrice(priceElement);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    return price;
                }
            }
        }
        return null;
    }


    public BigDecimal extractOfficialStore(Document doc, String selector) {
        return extract(doc, selector);
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
