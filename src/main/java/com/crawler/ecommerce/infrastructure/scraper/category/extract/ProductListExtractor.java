package com.crawler.ecommerce.infrastructure.scraper.category.extract;

import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import com.crawler.ecommerce.infrastructure.scraper.product.extract.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Transforma elementos HTML de productos → ScrapedProduct.
 *
 * Extractor principal de productos desde categorías:
 * - Coordina 4 extractors especializados (SKU, título, precio, imágenes, disponibilidad)
 * - Maneja tradeoffs específicos por marketplace (ej: Falabella SKU generation)
 * - Construye DTOs completos con todos los datos necesarios
 *
 * Diseñado para manejar complejidad de diferentes marketplaces:
 * - Paris.cl: Data attributes, selectores específicos de precio
 * - MercadoLibre: Estructura HTML estable y consistente
 * - Falabella: Tradeoffs con URLs dinámicas y SKU sintéticos
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — EXTRACTOR COMPONENT
 *
 * Este extractor sigue principios de diseño robustos:
 *
 * - COORDINATION: Orquesta múltiples extractors especializados
 * - TRADEOFF HANDLING: Maneja decisiones de diseño específicas
 * - MARKETPLACE ABSTRACTION: Aísla lógica de cada sitio
 * - ROBUSTEZ: Validación completa y fallbacks inteligentes
 * - CONFIGURACIÓN: Selectores via YAML para cada marketplace
 *
 * Los extractors coordinados permiten:
 * - Extracción modular por tipo de dato
 * - Reutilización entre categorías y productos individuales
 * - Testing unitario aislado de cada extractor
 * - Mantenimiento simplificado de selectores específicos
 * ------------------------------------------------------------------------
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductListExtractor {

    // Extractores básicos para cada campo
    private final SkuExtractor skuExtractor;
    private final TitleExtractor titleExtractor;
    private final PriceExtractor priceExtractor;
    private final ImageExtractor imageExtractor;
    private final AvailabilityExtractor availabilityExtractor;

    /**
     * Extrae productos desde items de categoría.
     *
     * @param items  Elements de productos encontrados en página de categoría
     * @param source Marketplace de origen (para asignar a ScrapedProduct)
     * @return Lista de productos con datos básicos desde categoría
     */
    public List<ScrapedProduct> extractProducts(Elements items,
            com.crawler.ecommerce.domain.model.MarketplaceSource source) {
        if (items.isEmpty()) {
            log.debug("Lista items vacía, no hay productos para extraer");
            return List.of();
        }

        List<ScrapedProduct> products = items.stream()
                .map(item -> extractProductFromItem(item, source))
                .filter(Objects::nonNull)
                .toList();

        log.info("Extraídos {} productos desde categoría", products.size());
        return products;
    }

    /**
     * Extrae datos básicos de un item de categoría.
     * 
     * @param item   Elemento HTML del producto
     * @param source Marketplace de origen
     */
    private ScrapedProduct extractProductFromItem(Element item,
            com.crawler.ecommerce.domain.model.MarketplaceSource source) {
        try {
            // 1. SKU (obligatorio)
            String sku = skuExtractor.extractFromListing(item);
            
            // Caso especial: Falabella - intenta los atributos de datos primero, luego el hash del nombre
            if ((sku == null || sku.isBlank()) && source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                // Intenta los atributos de datos primero (prioridad alta)
                String productId = item.attr("data-product-id");
                if (!productId.isEmpty() && isValidFalabellaSku(productId)) {
                    sku = productId;
                }
                if (sku == null || sku.isBlank()) {
                    String itemId = item.attr("data-item-id");
                    if (!itemId.isEmpty() && isValidFalabellaSku(itemId)) {
                        sku = itemId;
                    }
                }
                if (sku == null || sku.isBlank()) {
                    String dataSku = item.attr("data-sku");
                    if (!dataSku.isEmpty() && isValidFalabellaSku(dataSku)) {
                        sku = dataSku;
                    }
                }
                if (sku == null || sku.isBlank()) {
                    String testId = item.attr("data-testid");
                    if (!testId.isEmpty() && isValidFalabellaSku(testId)) {
                        sku = testId;
                    }
                }
                
                // Si no hay data attributes, usar hash del nombre como fallback
                // TRADEOFF: Falabella no expone SKU/código de producto en listados de categoría
                // A diferencia de Paris (data-cnstrc-item-id) y MercadoLibre (MLAxxxx), Falabella solo muestra
                // información visual (nombre, precio, imagen) sin identificadores únicos en el HTML del listado.
                // 
                // Solución: Generar SKU sintético basado en hash del nombre + timestamp parcial
                // - hash(name): Consistente para mismo producto (evita variaciones)
                // - timestamp: Diferenciador para evitar duplicados por items múltiples del mismo producto
                // - Formato: FAL-1907153860-8234 (prefijo + hash + timestamp)
                //
            }
            if (sku == null || sku.isBlank()) {
                // TRADEOFF: Generar SKU único para Falabella
                if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                    // Mejorar unicidad: hash del nombre + timestamp + random + UUID suffix
                    String productName = titleExtractor.extract(item, "b.pod-subTitle, [id*='pod-displaySubTitle'], .subTitle-rebrand", "a[href]");
                    String productHash = productName != null ? 
                        String.valueOf(Math.abs(productName.hashCode())) : 
                        String.valueOf(System.currentTimeMillis() % 10000);
                    
                    // Generar SKU más único con múltiples componentes
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String random = String.valueOf((int)(Math.random() * 10000));
                    String itemHash = String.valueOf(Math.abs(item.hashCode())); // Hash del elemento HTML
                    
                    sku = "FAL-" + productHash + "-" + timestamp.substring(timestamp.length() - 6) + "-" + random + "-" + itemHash.substring(0, 3);
                    
                    log.debug("Falabella: SKU generado (tradeoff): {}", sku);
                } else {
                    log.debug("SKU no encontrado en item");
                    return null;
                }
            }
            log.debug("SKU encontrado: {}", sku);

            // 2. Nombre (obligatorio)
            String name = null;
            if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.PARIS) {
                name = titleExtractor.extract(item, ".ui-line-clamp-2.ui-text-xs, .ui-line-clamp-2.ui-leading-[14px], [class*='ui-line-clamp-2'][class*='ui-text-xs'], [class*='ui-line-clamp-2'][class*='ui-leading']",
                        "a[href]");
            } else if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                name = titleExtractor.extract(item, "b.pod-subTitle, [id*='pod-displaySubTitle'], .subTitle-rebrand", "a[href]");
            } else {
                name = titleExtractor.extract(item, ".ui-search-item__title, h2, h3, .title, .product-name");
            }

            if (name == null || name.trim().isEmpty()) {
                log.warn("Nombre no encontrado para SKU: {}", sku);
                return null;
            }
            log.debug("Nombre encontrado: {}", name);

            // 3. Precio (opcional pero importante)
            BigDecimal currentPrice = null;
            BigDecimal previousPrice = null;
            if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.PARIS) {
                // Selectores específicos Paris.cl
                currentPrice = priceExtractor.extract(item,
                        "div[data-testid='paris-pod-price'] span:not(.ui-line-through), " +
                        "[data-testid='paris-price'], " +
                        ".price-main, " +
                        "[class*='price-current'], " +
                        ".price .amount");
            } else if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                // Selectores específicos Falabella.cl
                // Prioridad: Precio CMR (tarjeta falabella) > Precio normal
                currentPrice = priceExtractor.extract(item,
                        "span.copy10.primary.high, span.copy10.primary.medium, " +
                        "[class*='primary high'], [class*='primary medium'], " +
                        "[class*='line-height-22']");
                // Precio anterior tachado (descuentos)
                previousPrice = priceExtractor.extractPrevious(item);
            } else {
                currentPrice = priceExtractor.extract(item, ".andes-money-amount__fraction, .price");
            }

            if (currentPrice == null) {
                log.debug("Precio no encontrado para SKU: {} (continuando sin precio)", sku);
            } else {
                log.debug("Precio encontrado: {}", currentPrice);
            }

            // 4. URL del producto
            Element linkElement = null;
            String sourceUrl = null;
            if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                // Falabella: el item ahora es el <a> directamente (selector actualizado)
                linkElement = item.selectFirst("a[href]");
                
                // DEBUG: Log para diagnóstico
                if (linkElement == null) {
                    log.debug("Falabella: No se encontró enlace <a> en item: {}", item.className());
                } else {
                    sourceUrl = linkElement.attr("href");
                    log.debug("Falabella: Enlace encontrado: {}", sourceUrl);
                }
                
                if (linkElement != null) {
                    // Convertir URL relativa a absoluta
                    if (sourceUrl != null && sourceUrl.startsWith("/")) {
                        sourceUrl = "https://www.falabella.cl" + sourceUrl;
                    }
                } else {
                    // HARDCODE: URL base para Falabella (tradeoff definitivo)
                    sourceUrl = "https://www.falabella.com/falabella-cl/category/cat70057/Notebooks";
                    log.debug("Falabella: URL hardcodeada (tradeoff): {}", sourceUrl);
                }
            } else {
                // ML y Paris: selector estándar
                linkElement = item.selectFirst("a[href]");
                if (linkElement != null) {
                    sourceUrl = linkElement.attr("href");
                    // Convertir URL relativa a absoluta
                    if (sourceUrl != null && sourceUrl.startsWith("/")) {
                        if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.PARIS) {
                            sourceUrl = "https://www.paris.cl" + sourceUrl;
                        } else if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.MERCADO_LIBRE) {
                            sourceUrl = "https://www.mercadolibre.com.ar" + sourceUrl;
                        }
                    }
                }
            }

            // 5. Imágenes
            List<String> images;
            if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.PARIS) {
                images = imageExtractor.extractFromListing(item, "img.ui-size-full, img.ui-object-contain, [class*='ui-size-full'], [class*='ui-object-contain'], img[src*='cl-dam-resizer.ecomm.cencosud.com']");
            } else if (source == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                images = imageExtractor.extractFromListing(item, "img[id*='pod-image'], img.jsx-1996933093");
            } else {
                images = imageExtractor.extractFromListing(item, "img");
            }
            log.debug("Imágenes encontradas: {}", images.size());

            // 6. Disponibilidad
            boolean available = availabilityExtractor.extractFromListing(item);
            log.debug("Disponible: {}", available);

            ScrapedProduct product = ScrapedProduct.builder()
                    .sku(sku)
                    .name(name)
                    .currentPrice(currentPrice)
                    .previousPrice(previousPrice)
                    .images(images)
                    .available(available)
                    .sourceUrl(sourceUrl)
                    .source(source)
                    .build();

            log.debug("Producto completo extraído: SKU={}, Name={}, Price={}, Images={}, Available={}", 
                     sku, name, currentPrice, images.size(), available);
            return product;

        } catch (Exception e) {
            log.error("Error extrayendo producto: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Valida si un SKU de Falabella es válido y no es un placeholder.
     */
    private boolean isValidFalabellaSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        
        sku = sku.trim();
        
        // Excluir placeholders comunes específicos de Falabella
        if (sku.equalsIgnoreCase("ssr-pod") || 
            sku.equalsIgnoreCase("pod") || 
            sku.equalsIgnoreCase("product") ||
            sku.equalsIgnoreCase("item") ||
            sku.startsWith("test-") || // Test IDs
            sku.length() < 3) {
            return false;
        }
        
        // Validar que tenga algún carácter alfanumérico
        return sku.matches(".*[a-zA-Z0-9].*");
    }
}
