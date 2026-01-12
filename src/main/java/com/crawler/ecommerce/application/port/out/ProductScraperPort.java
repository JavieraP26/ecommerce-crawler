package com.crawler.ecommerce.application.port.out;

import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;

import java.util.List;

/**
 * Puerto de salida para scraping de productos.
 * 
 * Desacopla la aplicación de detalles de implementación (Jsoup, HTTP, estrategias específicas).
 * Define el contrato que la capa de aplicación debe usar para scraping
 * sin conocer detalles técnicos de la infraestructura.
 * 
 * Principio: Application layer solo conoce contratos, no infraestructura.
 */
public interface ProductScraperPort {

    /**
     * Extrae un producto individual desde su ficha completa.
     * 
     * Realiza scraping de la página del producto para extraer todos los datos:
     * - Información básica (SKU, nombre, precios)
     * - Disponibilidad y estado
     * - Galería de imágenes
     * 
     * @param productUrl URL completa de la ficha del producto
     * @return ScrapedProduct con todos los datos extraídos o null si falla el scraping
     */
    ScrapedProduct scrapeProduct(String productUrl);

    /**
     * Extrae múltiples productos desde una página de listado.
     * 
     * Procesa una página de categoría o resultados de búsqueda
     * para obtener una lista de productos disponibles.
     * 
     * @param pageUrl URL de la página de listado/categoría
     * @return Lista de productos válidos (con SKU no nulo) encontrados en la página
     */
    List<ScrapedProduct> scrapeProductsPage(String pageUrl);


}
