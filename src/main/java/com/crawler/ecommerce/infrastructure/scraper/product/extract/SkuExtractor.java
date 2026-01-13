package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Extrae SKUs desde múltiples fuentes con prioridad configurable.
 *
 * Implementa extracción robusta para diferentes marketplaces:
 * - Paris.cl: data-cnstrc-item-id (prioridad alta)
 * - MercadoLibre: MLA\d+ (formato estándar)
 * - Falabella: Atributos data-* y fallbacks
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - PRIORIDAD CONFIGURABLE: Fuentes de datos ordenadas por prioridad
 * - ROBUSTEZ: Múltiples fallbacks para máxima compatibilidad
 * - PATTERN MATCHING: Detección de patrones específicos por marketplace
 * - NULL SAFETY: Manejo seguro de valores nulos
 * - CONFIGURACIÓN EXTERNA: Sin dependencias externas, solo texto
 *
 * - VERSATILIDAD: Soporta diferentes formatos de SKU
 *
 * Los métodos están diseñados para:
 * - Extraer desde items de listado (prioridad más alta)
 * - Extraer desde fichas individuales (fallbacks)
 * - Extraer desde URLs completas (último recurso)
 * - Generar SKUs sintéticos para casos especiales (Falabella)
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
public class SkuExtractor {

    /**
     * Extrae SKU desde item de listado.
     * Prioridad: data-cnstrc-item-id > data-sku > Paris.cl patterns > MercadoLibre
     * MLA\d+
     */
    public String extractFromListing(Element item) {
        // Paris.cl Constructor.io ID (highest priority)
        String cnstrcId = item.attr("data-cnstrc-item-id");
        if (!cnstrcId.isEmpty() && isValidSku(cnstrcId)) {
            return cnstrcId;
        }

        // data-sku prioritario
        String dataSku = item.attr("data-sku");
        if (!dataSku.isEmpty() && isValidSku(dataSku)) {
            return dataSku;
        }

        // Paris.cl patterns
        String parisSku = extractParisSku(item);
        if (parisSku != null && isValidSku(parisSku)) {
            return parisSku;
        }

        // MercadoLibre fallback (null-safe)
        Element anchor = item.selectFirst("a[href]");
        if (anchor != null) {
            String urlSku = extractFromUrl(anchor.attr("href"));
            if (urlSku != null && isValidSku(urlSku)) {
                return urlSku;
            }
        }

        return null;
    }
    
    /**
     * Valida si un SKU es válido y no es un placeholder.
     */
    private boolean isValidSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        
        sku = sku.trim();
        
        // Excluir placeholders comunes
        if (sku.equalsIgnoreCase("ssr-pod") || 
            sku.equalsIgnoreCase("pod") || 
            sku.equalsIgnoreCase("product") ||
            sku.equalsIgnoreCase("item") ||
            sku.length() < 3) {
            return false;
        }
        
        // Validar que tenga algún carácter alfanumérico
        return sku.matches(".*[a-zA-Z0-9].*");
    }

    /**
     * Extrae SKU desde item de Paris.cl.
     * Detecta dos formatos:
     * 1. id="product-{slug}-{numeric-sku}" → extrae numeric-sku
     * 2. href="/{name}-{alphanumeric-sku}.html" → extrae alphanumeric-sku
     */
    private String extractParisSku(Element item) {
        // 1. From id attribute: product-{slug}-{numeric-sku}
        String id = item.attr("id");
        if (id.startsWith("product-")) {
            String[] parts = id.split("-");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // Validate it looks like a SKU (numeric or alphanumeric)
                if (lastPart.matches("\\d+") || lastPart.matches("[A-Z0-9]{6,}")) {
                    return lastPart;
                }
            }
        }

        // 2. From href: /{name}-{alphanumeric-sku}.html
        Element anchor = item.selectFirst("a[href]");
        if (anchor != null) {
            String href = anchor.attr("href");
            // Match alphanumeric SKU before .html
            Pattern pattern = Pattern.compile("[-/]([A-Z0-9]{8,})(?:\\.html)?$");
            var matcher = pattern.matcher(href);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    /**
     * Extrae SKU desde URL completa.
     * Prioridad: Falabella > /p/MLA\d+ > /MLA\d+
     */
    public String extractFromUrl(String url) {
        // 1. Falabella: /product/142114237/...
        Pattern falabellaPattern = Pattern.compile("/product/(\\d+)");
        var falabellaMatcher = falabellaPattern.matcher(url);
        if (falabellaMatcher.find()) {
            return falabellaMatcher.group(1);
        }

        // 2. Ficha específica: /p/MLA\d+
        Pattern pattern = Pattern.compile("/p/(MLA\\d+)");
        var matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 3. Genérico: /MLA\d+
        pattern = Pattern.compile("/(MLA\\d+)");
        matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extrae SKU desde DOM de ficha (fallback).
     * Prioridad: meta[property='product:retailer_item_id'] > data-item-id
     */
    public String extractFromDom(Document doc) {
        // Meta tag
        var metaSku = doc.selectFirst("meta[property='product:retailer_item_id']");
        if (metaSku != null) {
            String content = metaSku.attr("content");
            if (!content.isEmpty()) {
                return content;
            }
        }

        // Data attribute
        var itemElement = doc.selectFirst("[data-item-id]");
        return itemElement != null ? itemElement.attr("data-item-id") : null;
    }
}
