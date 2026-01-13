package com.crawler.ecommerce.domain.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa una categoría de productos en un sitio e-commerce.
 *
 * Almacena metadatos esenciales para el proceso de crawling:
 * - Información básica: nombre, breadcrumb, marketplace de origen
 * - Metadata de paginación: totalPages, productsPerPage
 * - Estado del crawling: status, lastCrawledAt, totalProducts
 *
 * Diseñada para soportar crawling incremental y monitoreo del estado:
 * - Permite reanudar crawling desde última página procesada
 * - Facilita detección de cambios en estructura del sitio
 * - Soporta scheduler de recrawling periódico
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — ENTIDAD DE DOMINIO
 *
 * Esta entidad sigue principios de Domain-Driven Design:
 *
 * - RIQUEZA DE DATOS: Contiene toda la información necesaria
 *   para los casos de uso de crawling sin depender de servicios externos.
 * - IDENTIDAD CLARA: sourceUrl como identificador natural único
 *   además del ID sintético de base de datos.
 * - INVARIANTES PROTEGIDOS: Estados y validaciones encapsulados
 *   a través de enums y valores por defecto seguros.
 *
 * Los índices están optimizados para consultas operativas del crawler:
 * - idx_category_url: Evita duplicados y permite upsert eficiente
 * - idx_category_source: Facilita procesamiento por marketplace
 * ------------------------------------------------------------------------
 */

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_url", columnList = "source_url", unique = true),
        @Index(name = "idx_category_source", columnList = "source")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /**
     * Identificador único de la categoría en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoría según el sitio e-commerce.
     * Ejemplo: "Smartphones", "Herramientas Eléctricas", "Celulares".
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Total de páginas detectadas en la categoría durante el último crawling.
     * Determinado por paginación del sitio (ej: "Página 1 de 25").
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer totalPages = 1;

    /**
     * Total de productos estimados en la categoría.
     * Puede ser calculado como (totalPages * productosPorPagina) o extraído directamente.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer totalProducts = 0;

    /**
     * Sitio e-commerce de origen de la categoría.
     * Valores esperados: "MercadoLibre", "Paris", etc.
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MarketplaceSource source;

    /**
     * URL completa de la categoría en el sitio origen.
     * Usada para paginación (ej: agregando ?page=2).
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceUrl;

    /**
     * Ruta breadcrumb de la categoría (ej: "Tecnología > Celulares > Smartphones").
     * Útil para categorización jerárquica.
     */
    @Column(length = 500)
    private String breadcrumb;

    /**
     * Productos por página detectados en paginación.
     * Ejemplo: 48 productos/página en MercadoLibre.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer productsPerPage = 0;

    /**
     * Estado de la categoría según resultado del crawling.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CategoryStatus status = CategoryStatus.ACTIVA;


    /**
     * Fecha y hora del último crawling exitoso de esta categoría.
     * Útil para detectar cambios en paginación o re-crawling.
     */
    @Column
    private LocalDateTime lastCrawledAt;

    /**
     * Fecha de primera creación de la categoría en la base de datos.
     * Gestionada automáticamente por JPA Auditing.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización de la categoría.
     * Gestionada automáticamente por JPA Auditing (actualizaciones de paginación).
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}
