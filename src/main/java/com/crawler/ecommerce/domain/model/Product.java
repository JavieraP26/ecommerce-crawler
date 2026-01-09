package com.crawler.ecommerce.domain.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa un producto extraído desde sitios e-commerce.
 * Almacena información completa del producto incluyendo precios, imágenes y disponibilidad.
 */

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_source", columnList = "source"),
        @Index(name = "idx_product_available", columnList = "available"),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /**
     * Identificador único del producto en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SKU (Stock Keeping Unit) - Código identificador único del producto en el sitio origen.
     * Ejemplo: "MLA19813486" para MercadoLibre.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    /**
     * Nombre completo del producto según aparece en el sitio e-commerce.
     */
    @Column(nullable = false, length = 500)
    private String name;

    /**
     * Precio actual del producto en la moneda local del sitio.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    /**
     * Precio anterior del producto (si existe), utilizado para detectar descuentos.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal previousPrice;

    /**
     * Indica si el producto está disponible para compra.
     * true = disponible, false = agotado o no disponible.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    /**
     * Sitio e-commerce de origen del producto.
     * Valores esperados: "MercadoLibre", "Paris", etc.
     */
    @Column(nullable = false, length = 50)
    private String source;

    /**
     * URL completa de la ficha del producto en el sitio origen.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceUrl;

    /**
     * Categoría a la que pertenece el producto (relación opcional).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Lista de URLs de imágenes del producto.
     * Almacenadas en tabla intermedia product_images con @ElementCollection.
     *
     * Nota: Si las imágenes requirieran metadata adicional (alt text, tamaño, tipo)
     * o lifecycle propio (auditoría, versionado), deberían modelarse como entidad
     * ProductImage con relación @OneToMany.
     */
    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url", columnDefinition = "TEXT")
    @OrderColumn(name = "position")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    /**
     * Fecha de primera extracción del producto.
     * Gestionada automáticamente por JPA Auditing.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización del producto.
     * Gestionada automáticamente por JPA Auditing.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calcula el porcentaje de descuento si existe un precio anterior.
     *
     * @return Porcentaje de descuento (0-100) o 0 si no hay descuento.
     */
    public double getDiscountPercentage() {
        if (previousPrice == null || previousPrice.compareTo(currentPrice) <= 0) {
            return 0.0;
        }
        return previousPrice.subtract(currentPrice)
                .divide(previousPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Verifica si el producto tiene descuento activo.
     *
     * @return true si existe precio anterior mayor al actual.
     */
    public boolean hasDiscount() {
        return previousPrice != null && previousPrice.compareTo(currentPrice) > 0;
    }

}
