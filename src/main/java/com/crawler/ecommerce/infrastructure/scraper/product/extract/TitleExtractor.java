package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extrae títulos/nombres de productos con fallbacks configurables.
 *
 * Implementa extracción robusta y flexible para diferentes marketplaces:
 * - Múltiples selectores separados por coma (configurables)
 * - Fallbacks dinámicos (varargs) para máxima compatibilidad
 * - Limpieza y normalización de texto extraído
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - FLEXIBILIDAD EXTREMA: Múltiples selectores y fallbacks configurables
 * - NULL SAFETY: Manejo seguro de valores nulos y vacíos
 * - REUTILIZACIÓN: Componente genérico para cualquier marketplace
 * - ROBUSTEZ: Validación completa y logging estructurado
 * - CONFIGURACIÓN EXTERNA: Selectores via YAML, no hardcodeados
 *
 * Los métodos están diseñados para:
 * - Extraer desde items de listado (rápido, selectores múltiples)
 * - Extraer desde fichas individuales (detallado, con fallbacks)
 * - Soportar diferentes estructuras HTML por marketplace
 * - Proporcionar logging para debugging de selectores
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
public class TitleExtractor {

    /**
     * Extrae título desde Element/Document con selector principal + fallbacks.
     * Soporta múltiples selectores separados por coma en primarySelector.
     *
     * @param element Element/Document donde buscar
     * @param primarySelector Selector principal (puede tener múltiples separados por coma)
     * @param fallbacks Selectores alternativos (varargs)
     * @return Texto limpio o null si todos fallan
     */
    public String extract(Element element, String primarySelector, String... fallbacks) {
        // Si primarySelector tiene comas, probar cada uno
        if (primarySelector != null && primarySelector.contains(",")) {
            String[] selectors = primarySelector.split(",");
            for (String sel : selectors) {
                sel = sel.trim();
                if (!sel.isEmpty()) {
                    Element titleElement = element.selectFirst(sel);
                    if (titleElement != null) {
                        String text = titleElement.text().trim();
                        if (!text.isEmpty()) {
                            return text;
                        }
                    }
                }
            }
        } else {
            // Selector único
            Element titleElement = element.selectFirst(primarySelector);
            if (titleElement != null) {
                String text = titleElement.text().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }

        // Intentar fallbacks
        if (fallbacks.length > 0) {
            for (String fallback : fallbacks) {
                Element titleElement = element.selectFirst(fallback);
                if (titleElement != null) {
                    String text = titleElement.text().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        }

        log.debug("No title encontrado con selectores: {}", primarySelector);
        return null;
    }

    /**
     * Versión simple sin fallbacks (para listing rápido).
     * Soporta múltiples selectores separados por coma.
     */
    public String extract(Element element, String selector) {
        if (element == null || selector == null || selector.isBlank()) {
            return null;
        }
        
        // Si tiene comas, probar cada selector
        if (selector.contains(",")) {
            String[] selectors = selector.split(",");
            for (String sel : selectors) {
                sel = sel.trim();
                if (!sel.isEmpty()) {
                    Element titleElement = element.selectFirst(sel);
                    if (titleElement != null) {
                        String text = titleElement.text().trim();
                        if (!text.isEmpty()) {
                            return text;
                        }
                    }
                }
            }
            return null;
        }
        
        // Selector único
        Element titleElement = element.selectFirst(selector);
        return titleElement != null ? titleElement.text().trim() : null;
    }
}
