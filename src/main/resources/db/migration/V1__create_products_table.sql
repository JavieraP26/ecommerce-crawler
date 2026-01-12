-- Tabla productos e-commerce con precios y disponibilidad
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    current_price DECIMAL(12,2) NOT NULL,
    previous_price DECIMAL(12,2),
    available BOOLEAN NOT NULL DEFAULT true,
    source VARCHAR(50) NOT NULL,
    source_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_current_price CHECK (current_price >= 0),
    CONSTRAINT chk_previous_price CHECK (previous_price IS NULL OR previous_price >= 0)
);

-- Tabla imágenes producto (ElementCollection)
CREATE TABLE product_images (
    product_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    position INTEGER NOT NULL,

    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, position)
);

-- Índices performance
CREATE UNIQUE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_source ON products(source);
CREATE INDEX idx_product_available ON products(available);
CREATE INDEX idx_product_current_price ON products(current_price);
CREATE INDEX idx_product_created_at ON products(created_at DESC);
CREATE INDEX idx_product_images_product ON product_images(product_id);

-- Comentarios documentación
COMMENT ON TABLE products IS 'Productos extraídos desde e-commerce (MercadoLibre/Paris) con precios actuales/anteriores';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - Código único sitio origen (ej: MLA19813486, HS7010)';
COMMENT ON COLUMN products.source_url IS 'URL ficha completa producto para re-scraping';
COMMENT ON COLUMN products.previous_price IS 'Precio anterior para cálculo descuentos (hasDiscount/getDiscountPercentage methods)';
COMMENT ON TABLE product_images IS 'Imágenes producto (ElementCollection @OrderColumn) ordenadas por position';
