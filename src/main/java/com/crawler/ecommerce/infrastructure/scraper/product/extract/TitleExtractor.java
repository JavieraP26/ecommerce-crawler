package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extrae títulos/nombres de productos con fallbacks configurables.
 * Reutilizable entre listing/detail y sitios (ML/Paris).
 *
 * Maneja múltiples selectores + limpieza (trim, null-safe).
 */
@Component
@Slf4j
public class TitleExtractor {

    /**
     * Extrae título desde Element/Document con selector principal + fallbacks.
     *
     * @param element Element/Document donde buscar
     * @param primarySelector Selector principal (@Value desde yml)
     * @param fallbacks Selectores alternativos (varargs)
     * @return Texto limpio o null si todos fallan
     */
    public String extract(Element element, String primarySelector, String... fallbacks) {
        // Selector principal
        Element titleElement = element.selectFirst(primarySelector);

        if (titleElement == null && fallbacks.length > 0) {
            for (String fallback : fallbacks) {
                titleElement = element.selectFirst(fallback);
                if (titleElement != null) break;
            }
        }

        if (titleElement == null) {
            log.debug("No title encontrado con selectores: {}", primarySelector);
            return null;
        }

        return titleElement.text().trim();  // Limpieza básica (igual original)
    }

    /**
     * Versión simple sin fallbacks (para listing rápido).
     */
    public String extract(Element element, String selector) {
        return extract(element, selector);
    }
}
