package com.crawler.ecommerce.infrastructure.scraper.category.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * Encuentra elementos HTML de productos en página de categoría.
 *
 * Responsabilidad única: Localizar contenedores de productos.
 * No extrae datos, solo selecciona elementos para otros extractors.
 *
 * Diseñado para manejar selectores complejos y fallbacks:
 * - Selector principal configurable via YAML (@Value)
 * - Selectores alternativos para debugging y compatibilidad
 * - Validación de contenedores vs items individuales
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - SINGLE RESPONSIBILITY: Solo localiza elementos, no extrae datos
 * - CONFIGURACIÓN EXTERNA: Selectores via YAML, no hardcodeados
 * - DEBUGGING FACILITADO: Múltiples selectores alternativos
 * - ROBUSTEZ: Validación de estructura HTML esperada
 *
 * Los selectores alternativos siguen esta prioridad:
 * - [data-cnstrc-item-id]: Selector principal Paris.cl
 * - [role='gridcell']: Fallback para layouts alternativos
 * - a[id^='product-']: Fallback para IDs de productos
 * - [data-sku]: Fallback para atributos SKU explícitos
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
public class ItemsExtractor {

    /**
     * Selector correcto Paris.cl (2026-01): items individuales con data-cnstrc-item-id
     * NO usar contenedor padre [data-testid='product-list-grid']
     */
    public Elements extractItems(Document doc, String itemsSelector) {
        if (doc == null || itemsSelector == null || itemsSelector.isBlank()) {
            log.warn("Parámetros inválidos: doc={}, selector={}", doc != null, itemsSelector);
            return new Elements();
        }

        log.info("Buscando items con selector: {}", itemsSelector);
        Elements items = doc.select(itemsSelector);

        if (items.isEmpty()) {
            log.warn("No items encontrados con selector principal: {}", itemsSelector);
            
            // Intentar selectores alternativos para debug
            log.info("Probando selectores alternativos...");
            Elements alt1 = doc.select("[data-cnstrc-item-id]");
            Elements alt2 = doc.select("[role='gridcell']");
            Elements alt3 = doc.select("a[id^='product-']");
            Elements alt4 = doc.select("[data-sku]");
            
            log.info("   - [data-cnstrc-item-id]: {} items", alt1.size());
            log.info("   - [role='gridcell']: {} items", alt2.size());
            log.info("   - a[id^='product-']: {} items", alt3.size());
            log.info("   - [data-sku]: {} items", alt4.size());
            
            // Usar el mejor selector alternativo
            if (!alt1.isEmpty()) {
                log.info("Usando selector alternativo: [data-cnstrc-item-id]");
                return alt1;
            } else if (!alt2.isEmpty() && alt2.size() > 1) {
                log.info("Usando selector alternativo: [role='gridcell']");
                return alt2;
            } else if (!alt3.isEmpty()) {
                log.info("Usando selector alternativo: a[id^='product-']");
                return alt3;
            }
            
            return new Elements();
        }

        // Validar no sea contenedor padre (solo 1 item con muchos hijos)
        if (items.size() == 1) {
            int children = items.first().select("[data-cnstrc-item-id]").size();
            if (children > 10) {
                log.warn("Selector apunta a contenedor padre, extrayendo hijos...");
                return items.first().select("[data-cnstrc-item-id]");
            }
        }

        log.info("{} items encontrados con selector principal", items.size());
        return items;
    }

    public int countItems(Document doc, String itemsSelector) {
        return extractItems(doc, itemsSelector).size();
    }
}
