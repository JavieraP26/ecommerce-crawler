package com.crawler.ecommerce.infrastructure.scraper.product.extract;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Detecta disponibilidad de productos mediante análisis de texto.
 *
 * Implementa lógica basada en Machine Learning simple:
 * - Keywords de "no disponibilidad" (agotado, sin stock)
 * - Keywords de "disponibilidad" (botón de compra)
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - HEURÍSTICA SIMPLE: Reglas basadas en texto plano
 * - RESPONSABILIDAD ÚNICA: Solo detección de disponibilidad
 * - ROBUSTEZ: Maneja múltiples patrones de texto
 * - CONFIGURACIÓN: Sin dependencias externas, solo texto
 *
 * La lógica está diseñada para:
 * - Alta precisión para casos positivos (evita falsos negativos)
 * - Baja complejidad para mantenimiento
 *
 * Los patrones de detección siguen esta prioridad:
 * - Keywords negativos: "agotado", "sin stock", "no disponible"
 * - Keywords positivos: "comprar", "agregar", "comprar ahora"
 * - Contexto: listing vs detail (diferentes niveles de certeza)
 * ------------------------------------------------------------------------
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
