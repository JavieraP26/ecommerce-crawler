package com.crawler.ecommerce.infrastructure.scraper.category.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extrae título/nombre de categoría desde página de listado.
 *
 * Implementa extracción robusta con múltiples niveles de fallback:
 * - Selector principal configurable via YAML (@Value)
 * - Jerarquía de fallbacks para máxima compatibilidad
 * - Limpieza y normalización de texto extraído
 *
 * Diseñado para reutilización entre diferentes marketplaces:
 * - Paris.cl: Usa selectores específicos de data attributes
 * - MercadoLibre: Usa selectores de clases CSS estándar
 * - Falabella: Usa selectores de componentes específicos
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - SINGLE RESPONSIBILITY: Solo extrae títulos, no otros datos
 * - CONFIGURACIÓN EXTERNA: Selectores via YAML, no hardcodeados
 * - ROBUSTEZ: Múltiples fallbacks para diferentes estructuras HTML
 * - REUTILIZACIÓN: Componente genérico para cualquier marketplace
 *
 * Los fallbacks siguen esta prioridad:
 * 1. Selector principal (configurable)
 * 2. Último breadcrumb (común en e-commerce)
 * 3. h1 genérico (título principal de página)
 * 4. Meta title (fallback universal)
 * ------------------------------------------------------------------------
*/

@Component
@Slf4j
public class CategoryTitleExtractor {

    /**
     * Extrae nombre de categoría desde Document con selector principal + fallbacks.
     *
     * Prioridad:
     * 1. Selector principal (@Value desde yml)
     * 2. Último breadcrumb (común en e-commerce)
     * 3. h1 (fallback genérico)
     * 4. Meta title
     *
     * @param doc Document HTML de página de categoría
     * @param primarySelector Selector principal (@Value)
     * @return Nombre limpio o null si todos fallan
     */

    public String extract(Document doc, String primarySelector){

        // Selector principal (DEBE venir configurado en yml)
        Element titleElement = doc.selectFirst(primarySelector);
        if (titleElement != null) {
            return titleElement.text().trim();
        }

        //Fallbacks

        // Último breadcrumb
        Element lastBreadcrumb = doc.selectFirst(".breadcrumb li:last-child, [class*='breadcrumb'] li:last-child");
        if (lastBreadcrumb != null){
            String breadcrumbText = lastBreadcrumb.text().trim();
            log.debug("Título categoría extraído desde breadcrumb: {}", breadcrumbText);
            return breadcrumbText;
        }

        // h1 genérico
        titleElement = doc.selectFirst("h1");
        if (titleElement != null) {
            String h1Text = titleElement.text().trim();
            log.debug("Título categoría extraído desde h1: {}", h1Text);
            return h1Text;
        }

        // Meta title
        Element metaTitle = doc.selectFirst("title");
        if (metaTitle != null) {
            return metaTitle.text()
                    .replaceAll(" - Página \\d+", "")
                    .replaceAll(" - \\d+ resultados", "")
                    .trim();
        }

        log.warn("No se pudo extraer título de categoría con ningún selector.");
        return null;
    }
}
