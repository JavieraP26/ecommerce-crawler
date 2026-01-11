package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Extrae SKUs desde URLs, atributos data-* y meta tags.
 * Prioridad: URL > data-sku > meta tags > data-item-id.
 */
@Component
@Slf4j
public class SkuExtractor {

    /**
     * Extrae SKU desde item de listado.
     * Prioridad: data-sku > href regex MLA\d+
     */
    public String extractFromListing(Element item) {
        // data-sku prioritario
        String dataSku = item.attr("data-sku");
        if (!dataSku.isEmpty()) {
            return dataSku;
        }

        // Fallback regex en href
        String href = item.selectFirst("a[href]").attr("href");
        return extractFromUrl(href);
    }

    /**
     * Extrae SKU desde URL completa.
     * Prioridad: /p/MLA\d+ > /MLA\d+
     */
    public String extractFromUrl(String url) {
        // Ficha específica: /p/MLA\d+
        Pattern pattern = Pattern.compile("/p/(MLA\\d+)");
        var matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Genérico: /MLA\d+
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
