package com.crawler.ecommerce.infrastructure.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO que representa una página de categoría extraída mediante scraping.
 *
 * Contiene metadata estructurada de la página y productos extraídos:
 * - Información básica: nombre, breadcrumb de navegación
 * - Metadata de paginación: totalPages, productsPerPage, currentPage
 * - Productos: lista de ScrapedProduct extraídos de esta página
 *
 * Diseñado para transferencia de datos entre capas:
 * - Infrastructure → Application: Datos brutos del scraping
 * - Inmutable con @Value: Evita modificaciones accidentales
 * - Builder pattern: Facilita construcción en tests y scraping
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — DATA TRANSFER OBJECT
 *
 * Este DTO sigue principios de diseño limpios:
 *
 * - SIN LÓGICA: Contenedor puro de datos sin comportamiento
 * - INMUTABILIDAD: @Value garantiza inmutabilidad después de creación
 * - SERIALIZACIÓN: Diseñado para JSON/XML sin conflictos
 * - AISLAMIENTO: Desacopla scraping HTML del dominio
 *
 * Los campos están optimizados para:
 * - Mapeo directo a entidades de dominio (Category)
 * - Serialización JSON para APIs REST
 * - Logging y debugging de procesos de scraping
 * - Validación de estructura de categorías
 * ------------------------------------------------------------------------
 */
@Value
@Builder
public class ScrapedCategoryPage {
    /**
     * Nombre de la categoría según el sitio e-commerce.
     * Ejemplo: "Smartphones", "Herramientas Eléctricas".
     */
    String name;
    
    /**
     * Ruta breadcrumb de navegación jerárquica.
     * Ejemplo: "Tecnología > Celulares > Smartphones".
     */
    String breadcrumb;
    
    /**
     * Total de páginas detectadas en la categoría.
     * Determinado por paginación del sitio (ej: "Página 1 de 50").
     */
    int totalPages;
    
    /**
     * Cantidad de productos por página detectados.
     * Ejemplo: 48 productos por página en MercadoLibre.
     */
    int productsPerPage;
    
    /**
     * Número de página actual siendo procesada.
     * Útil para logging y tracking de progreso.
     */
    int currentPage;
    
    /**
     * Lista de productos extraídos de esta página específica.
     * Contiene ScrapedProduct con datos brutos del scraping.
     */
    List<ScrapedProduct> products;
}
