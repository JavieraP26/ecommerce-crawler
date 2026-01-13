package com.crawler.ecommerce.infrastructure.dto;

import com.crawler.ecommerce.domain.model.MarketplaceSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa un producto extraído mediante scraping de sitios e-commerce.
 *
 * Contiene datos estructurados del producto sin validaciones de dominio:
 * - Identificación: SKU único, nombre del producto
 * - Comercial: Precios actual y anterior, disponibilidad
 * - Contenido: Galería de imágenes, URL de origen
 * - Metadata: Marketplace source para deduplicación
 *
 * Diseñado para transferencia de datos entre capas:
 * - Infrastructure → Application: Datos brutos del scraping
 * - Mutable con @Data: Permite modificación durante mapeo
 * - Builder pattern: Facilita construcción en tests y scraping
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DATA TRANSFER OBJECT
 *
 * Este DTO sigue principios de diseño limpios:
 *
 * - SIN LÓGICA: Contenedor puro de datos sin comportamiento de negocio
 * - MUTABILIDAD CONTROLADA: @Data permite setters para mapeo a dominio
 * - SERIALIZACIÓN: Diseñado para JSON/XML sin conflictos
 * - AISLAMIENTO: Desacopla scraping HTML del dominio
 *
 * Los campos están optimizados para:
 * - Mapeo directo a entidades de dominio (Product)
 * - Serialización JSON para APIs REST y preview endpoints
 * - Logging y debugging de procesos de scraping
 * - Deduplicación por SKU + source
 * ------------------------------------------------------------------------
 */
@Data
@Builder
@AllArgsConstructor
public class ScrapedProduct {
    /**
     * SKU (Stock Keeping Unit) - Código identificador único del producto.
     * Extraído del sitio origen (ej: "MLA19813486" para MercadoLibre).
     */
    String sku;
    
    /**
     * Nombre completo del producto según aparece en el sitio e-commerce.
     * Puede incluir marca, modelo y características principales.
     */
    String name;
    
    /**
     * Precio actual del producto en moneda local.
     * Precio preseleccionado en UI (radio button activo).
     * Puede ser null si no se pudo extraer el precio.
     */
    BigDecimal currentPrice;
    
    /**
     * Precio anterior del producto (si existe).
     * Usado para detectar descuentos y ofertas.
     * Solo presente cuando hay precio tachado visible en la página.
     */
    BigDecimal previousPrice;
    
    /**
     * Lista de URLs de imágenes del producto.
     * Ordenadas por relevancia (primera imagen = principal).
     * Puede ser vacía si no se encontraron imágenes.
     */
    List<String> images;
    
    /**
     * Indica si el producto está disponible para compra.
     * true = disponible, false = agotado o no disponible.
     */
    boolean available;
    
    /**
     * URL completa de la ficha del producto en el sitio origen.
     * Usada para referencia y futuros re-scraping.
     */
    String sourceUrl;
    
    /**
     * Sitio e-commerce de origen del producto.
     * Usado para deduplicación cross-site y routing de estrategias.
     */
    MarketplaceSource source;
}
