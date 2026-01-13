-- V3__add_category_id_to_products.sql
-- Agrega relación producto → categoría para index existente

ALTER TABLE products 
ADD COLUMN category_id BIGINT;

ALTER TABLE products ADD CONSTRAINT fk_product_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;


CREATE INDEX IF NOT EXISTS idx_product_category ON products(category_id);


-- Comentario para documentación
COMMENT ON COLUMN products.category_id IS 'ID categoría (relación opcional hacia categories)';


