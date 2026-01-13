package com.crawler.ecommerce.application.service;

import com.crawler.ecommerce.application.port.in.CrawlCategoryUseCasePort;
import com.crawler.ecommerce.application.port.out.CategoryRepositoryPort;
import com.crawler.ecommerce.application.port.out.CategoryScraperPort;
import com.crawler.ecommerce.application.port.out.ProductRepositoryPort;
import com.crawler.ecommerce.domain.model.Category;
import com.crawler.ecommerce.domain.model.CategoryStatus;
import com.crawler.ecommerce.domain.model.MarketplaceSource;
import com.crawler.ecommerce.domain.model.Product;
import com.crawler.ecommerce.infrastructure.dto.ScrapedCategoryPage;
import com.crawler.ecommerce.infrastructure.dto.ScrapedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NOTA ARQUITECTÓNICA — DECISIÓN CONSCIENTE
 *
 * Este Application Service concentra actualmente:
 * - orquestación del caso de uso,
 * - mapeo DTO → dominio,
 * - coordinación de persistencia.
 * 
 * Flujo categoría completa:
 * 1. Scrape página 1 → metadata + totalPages
 * 2. Persist/upsert categoría
 * 3. CrawlProducts todas páginas
 *
 * Esta decisión es deliberada y pragmática:
 *
 * - ALCANCE CONTROLADO: el proyecto tiene un número acotado de use cases
 * y este patrón no se repite aún en otros servicios.
 *
 * - EVITA COMPLEJIDAD PREMATURA: extraer mappers o coordinadores adicionales
 * hoy aumentaría el número de clases sin un beneficio real.
 *
 * - CLEAN ARCHITECTURE RESPETADA:
 * - no existe dependencia hacia infraestructura,
 * - la lógica de scraping permanece fuera de application,
 * - los repositorios se acceden exclusivamente vía ports.
 *
 * REFACTOR FUTURO PLANIFICADO:
 * Cuando exista un segundo o tercer caso de uso con flujo equivalente,
 * el mapeo será extraído a componentes dedicados (ej: CategoryMapper,
 * ProductMapper) manteniendo este servicio como orquestador puro.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CrawlCategoryService implements CrawlCategoryUseCasePort {

    private final CategoryScraperPort categoryScraperPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    /**
     * Crawlea categoría COMPLETA.
     * Para Paris.cl: solo procesa primera página (scroll infinito).
     * Para otros: procesa todas las páginas.
     */
    @Override
    public void crawlCategory(String categoryUrl) {
        log.info("Iniciando crawling categoría: {}", categoryUrl);

        try {
            MarketplaceSource source = detectSourceFromUrl(categoryUrl);
            
            // 1. Scrape página 1 → metadata + productos
            ScrapedCategoryPage page1 = categoryScraperPort.scrapeCategoryPage(categoryUrl);
            if (page1 == null) {
                throw new IllegalStateException("Scrape página 1 retornó null");
            }

            log.info("Página 1 scrapeada: {} productos encontrados", page1.getProducts().size());

            // 2. Crear/actualizar categoría
            Category category = toDomain(page1, categoryUrl);
            Category savedCategory = upsertCategory(category);
            
            // Asegurar que tenemos el ID de la categoría guardada
            if (savedCategory.getId() == null) {
                savedCategory = categoryRepositoryPort.findBySourceUrl(categoryUrl)
                        .orElseThrow(() -> new IllegalStateException("Categoría no encontrada después de guardar"));
            }
            
            log.info("Categoría guardada: {} (ID: {})", savedCategory.getName(), savedCategory.getId());

            // 3. PARIS.CL: Solo procesar primera página (scroll infinito, sin paginación)
            if (source == MarketplaceSource.PARIS) {
                log.info("Paris.cl detectado: procesando solo primera página (scroll infinito)");
                processProductsFromPage(page1, savedCategory);
            } else {
                // Otros marketplaces: procesar todas las páginas
                int totalPages = page1.getTotalPages();
                log.info("Procesando {} páginas (source={})", totalPages, source);
                
                // Procesar página 1 primero
                processProductsFromPage(page1, savedCategory);
                
                // Procesar páginas restantes si hay más
                for (int page = 2; page <= totalPages; page++) {
                    crawlCategoryPageInternal(categoryUrl, savedCategory, page);
                }
            }

            // Actualizar totalProducts en categoría
            int totalProductsInDb = productRepositoryPort.findSkusByCategory(savedCategory.getId()).size();
            savedCategory.setTotalProducts(totalProductsInDb);
            categoryRepositoryPort.save(savedCategory);
            log.info("Total productos en BD para categoría: {}", totalProductsInDb);

            categoryRepositoryPort.markCrawlingComplete(categoryUrl, CategoryStatus.COMPLETA, LocalDateTime.now());
            log.info("Categoría completada: {} (source={}), {} productos guardados", 
                    categoryUrl, source, totalProductsInDb);

        } catch (Exception e) {
            log.error("Error crawling {}: {}", categoryUrl, e.getMessage(), e);
            throw e; // Re-lanzar para que el GlobalExceptionHandler lo maneje
        }
    }
    
    /**
     * Procesa productos de una página scrapeada y los guarda en BD.
     */
    private void processProductsFromPage(ScrapedCategoryPage page, Category category) {
        log.info("Procesando productos de página...");
        
        if (page == null) {
            log.error("Página es null");
            return;
        }
        
        if (page.getProducts() == null) {
            log.error("Lista de productos es null");
            return;
        }
        
        log.info("Total productos scrapeados: {}", page.getProducts().size());

        if (page.getProducts().isEmpty()) {
            log.warn("Página sin productos para procesar");
            return;
        }

        // Validar productos
        List<ScrapedProduct> validProducts = page.getProducts().stream()
                .filter(p -> {
                    if (p == null) {
                        log.warn("Producto null encontrado");
                        return false;
                    }
                    if (p.getSku() == null || p.getSku().isBlank()) {
                        log.warn("Producto sin SKU: {}", p.getName());
                        return false;
                    }
                    if (p.getName() == null || p.getName().isBlank()) {
                        log.warn("Producto sin nombre: SKU={}", p.getSku());
                        return false;
                    }
                    return true;
                })
                .toList();

        log.info("Productos válidos: {} de {}", validProducts.size(), page.getProducts().size());

        if (validProducts.isEmpty()) {
            log.error("No hay productos válidos para guardar");
            // Log detallado de productos inválidos
            page.getProducts().forEach(p -> {
                if (p != null) {
                    log.warn("   - SKU: {}, Name: {}, Price: {}", p.getSku(), p.getName(), p.getCurrentPrice());
                }
            });
            return;
        }

        // Filtrar SKUs ya existentes en esta categoría
        List<String> existingSkus = productRepositoryPort.findSkusByCategory(category.getId());
        log.info("SKUs existentes en categoría: {}", existingSkus.size());

        List<ScrapedProduct> newProducts = validProducts.stream()
                .filter(p -> !existingSkus.contains(p.getSku()))
                .toList();

        log.info("Productos nuevos a guardar: {} de {}", newProducts.size(), validProducts.size());

        if (!newProducts.isEmpty()) {
            try {
                List<Product> productsToSave = newProducts.stream()
                        .map(p -> {
                            try {
                                return toProductDomain(p, category);
                            } catch (Exception e) {
                                log.error("Error mapeando producto SKU={}: {}", p.getSku(), e.getMessage(), e);
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .toList();

                log.info("Intentando guardar {} productos...", productsToSave.size());
                
                if (!productsToSave.isEmpty()) {
                    List<Product> saved = productRepositoryPort.saveAll(productsToSave);
                    log.info("Guardados {} productos exitosamente en categoría '{}'", saved.size(), category.getName());
                    
                    // Log de ejemplo de productos guardados
                    saved.stream().limit(3).forEach(p -> {
                        log.info("   ✓ Producto guardado: SKU={}, Name={}, Price={}", 
                                p.getSku(), p.getName(), p.getCurrentPrice());
                    });
                } else {
                    log.warn("No hay productos válidos para guardar después del mapeo");
                }
            } catch (Exception e) {
                log.error("Error guardando productos: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.warn("Todos los productos ya existen en la categoría");
        }
    }
    /**
     * Crawlea UNA página específica (batch/debug).
     */
    @Override
    public void crawlCategoryPage(String categoryUrl, int pageNumber) {
        categoryRepositoryPort.findBySourceUrl(categoryUrl)
                .ifPresentOrElse(
                        category -> crawlCategoryPageInternal(categoryUrl, category, pageNumber),
                        () -> log.warn("Categoría no encontrada para crawling: {}", categoryUrl));
    }

    private void crawlCategoryPageInternal(String categoryUrl, Category category, int pageNumber) {
        log.info("Procesando página {}/{} de categoría '{}'", pageNumber, category.getName(), categoryUrl);

        String pagedUrl = categoryUrl.contains("?")
                ? categoryUrl + "&page=" + pageNumber
                : categoryUrl + "?page=" + pageNumber;

        ScrapedCategoryPage page = categoryScraperPort.scrapeCategoryPage(pagedUrl);
        if (page != null) {
            processProductsFromPage(page, category);
        } else {
            log.warn("Página {} retornó null", pageNumber);
        }
    }

    /**
     * Upsert categoría (nueva o actualizar paginación).
     * Retorna la categoría guardada con ID.
     */
    private Category upsertCategory(Category category) {
        return categoryRepositoryPort.findBySourceUrl(category.getSourceUrl())
                .map(existing -> {
                    log.info("Actualizando categoría existente: {} (ID: {})", existing.getName(), existing.getId());
                    existing.setTotalPages(category.getTotalPages());
                    existing.setProductsPerPage(category.getProductsPerPage());
                    existing.setName(category.getName());
                    existing.setBreadcrumb(category.getBreadcrumb());
                    return categoryRepositoryPort.save(existing);
                })
                .orElseGet(() -> {
                    log.info("Nueva categoría: {}", category.getName());
                    return categoryRepositoryPort.save(category);
                });
    }

    /**
     * ScrapedCategoryPage → Category dominio.
     */
    /**
     * Detecta source desde URL usando marketplace names dinámicos.
     * Escalable para nuevos marketplaces sin modificar código.
     */
    private MarketplaceSource detectSourceFromUrl(String url) {
        String urlLower = url.toLowerCase();
        if (urlLower.contains("paris.cl") || urlLower.contains("paris")) {
            return MarketplaceSource.PARIS;
        }
        if (urlLower.contains("mercadolibre") || urlLower.contains("mercadolibre.com")) {
            return MarketplaceSource.MERCADO_LIBRE;
        }
        if (urlLower.contains("falabella")) {
            return MarketplaceSource.FALABELLA;
        }
        throw new IllegalStateException("Source no detectado desde URL: " + url);
    }

    /**
     * ScrapedCategoryPage → Category dominio.
     * Manejo robusto: si no hay productos en p1, intenta detectar source desde URL.
     */
    private Category toDomain(ScrapedCategoryPage scraped, String sourceUrl) {
        // Intentar detectar source desde productos
        MarketplaceSource source = scraped.getProducts().stream()
                .map(ScrapedProduct::getSource)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> detectSourceFromUrl(sourceUrl)); // ← Método dinámico

        return Category.builder()
                .name(scraped.getName())
                .totalPages(scraped.getTotalPages())
                .productsPerPage(scraped.getProductsPerPage())
                .source(source)
                .sourceUrl(sourceUrl)
                .breadcrumb(scraped.getBreadcrumb())
                .status(CategoryStatus.ACTIVA)
                .build();
    }

    /**
     * ScrapedProduct → Product dominio (batch).
     */
    private Product toProductDomain(ScrapedProduct scraped, Category category) {
        try {
            // Validar datos requeridos
            if (scraped.getSku() == null || scraped.getSku().isBlank()) {
                throw new IllegalArgumentException("SKU no puede ser null o vacío");
            }
            if (scraped.getName() == null || scraped.getName().isBlank()) {
                throw new IllegalArgumentException("Nombre no puede ser null o vacío");
            }
            if (scraped.getSourceUrl() == null || scraped.getSourceUrl().isBlank()) {
                // TRADEOFF: Falabella no siempre tiene URL en listado
                // Permitimos URL vacía para Falabella, requerimos para otros marketplaces
                if (scraped.getSource() == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                    // Permitimos URL vacía para Falabella
                    log.debug("SourceUrl vacío para Falabella (permitido por tradeoff)");
                } else {
                    throw new IllegalArgumentException("SourceUrl no puede ser null o vacío");
                }
            }
            if (category == null || category.getId() == null) {
                throw new IllegalArgumentException("Category no puede ser null y debe tener ID");
            }

            Product product = Product.builder()
                    .sku(scraped.getSku())
                    .name(scraped.getName())
                    .currentPrice(scraped.getCurrentPrice()) // Puede ser null
                    .previousPrice(scraped.getPreviousPrice()) // Puede ser null
                    .available(scraped.isAvailable())
                    .source(scraped.getSource())
                    .sourceUrl(scraped.getSourceUrl())
                    .images(scraped.getImages() != null ? scraped.getImages() : new ArrayList<>())
                    .category(category)
                    .build();

            // Validación final para Falabella: si el precio es null, usar valor por defecto
            if (product.getCurrentPrice() == null && product.getSource() == com.crawler.ecommerce.domain.model.MarketplaceSource.FALABELLA) {
                product = Product.builder()
                        .sku(scraped.getSku())
                        .name(scraped.getName())
                        .currentPrice(BigDecimal.ZERO) // Valor por defecto para evitar NOT NULL constraint
                        .previousPrice(scraped.getPreviousPrice())
                        .available(scraped.isAvailable())
                        .source(scraped.getSource())
                        .sourceUrl(scraped.getSourceUrl()) // Puede ser null para Falabella
                        .images(scraped.getImages() != null ? scraped.getImages() : new ArrayList<>())
                        .category(category)
                        .build();
                
                // DEBUG: Verificar valores finales para Falabella
                log.debug("Falabella DEBUG: SKU={}, Source={}, SourceUrl={}, Price={}", 
                         product.getSku(), product.getSource(), product.getSourceUrl(), product.getCurrentPrice());
            }

            log.debug("Producto mapeado: SKU={}, Name={}, Price={}, CategoryId={}", 
                     product.getSku(), product.getName(), product.getCurrentPrice(), category.getId());
            return product;
        } catch (Exception e) {
            log.error("Error mapeando producto SKU={}: {}", scraped.getSku(), e.getMessage(), e);
            throw e;
        }
    }
}
