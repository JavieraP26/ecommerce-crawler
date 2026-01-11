package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Detecta disponibilidad de productos.
 * Lógica ML: texto keywords + botón compra.
 *
 * listing: Keywords en item text.
 * detail: Keywords página + botón compra.
 */
@Component
@Slf4j
public class AvailabilityExtractor {

    /**
     * Disponibilidad desde item de listado (keywords texto).
     */
    public boolean extractFromListing(Element item) {
        String text = item.text().toLowerCase();
        return !text.contains("agotado") && !text.contains("sin stock");
    }

    /**
     * Disponibilidad desde ficha completa (texto + botón compra).
     */
    public boolean extractFromDetail(Document doc) {
        String pageText = doc.text().toLowerCase();

        // Keywords negativos
        if (pageText.contains("agotado") ||
                pageText.contains("sin stock") ||
                pageText.contains("no disponible")) {
            return false;
        }

        // Botón compra positivo
        var buyButton = doc.selectFirst(
                "button[data-testid='buy-now-button'], " +
                        ".ui-pdp-action-primary, " +
                        ".andes-button--loud"
        );

        return buyButton != null &&
                !buyButton.text().toLowerCase().contains("agotado");
    }
}
