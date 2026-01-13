-- V2__Create_categories_table.sql (COMPLETA como V1)
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,

    -- Core
    name VARCHAR(255) NOT NULL,
    source VARCHAR(50) NOT NULL,
    source_url TEXT NOT NULL UNIQUE,

    -- Numbers
    total_pages INTEGER NOT NULL DEFAULT 1 CHECK (total_pages > 0),
    total_products INTEGER NOT NULL DEFAULT 0 CHECK (total_products >= 0),
    products_per_page INTEGER NOT NULL DEFAULT 24 CHECK (products_per_page > 0),

    -- Strings
    breadcrumb VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' 
        CHECK (status IN ('ACTIVA', 'COMPLETA', 'ERROR', 'PAUSADA')),

    -- Timestamps
    last_crawled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices performance
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_source_url ON categories(source_url);
CREATE INDEX IF NOT EXISTS idx_categories_source ON categories(source);
CREATE INDEX IF NOT EXISTS idx_categories_status ON categories(status);
CREATE INDEX IF NOT EXISTS idx_categories_last_crawled ON categories(last_crawled_at DESC);
CREATE INDEX IF NOT EXISTS idx_categories_created ON categories(created_at DESC);

-- Constraints adicionales
CREATE INDEX IF NOT EXISTS idx_categories_total_pages ON categories(total_pages);

-- Comentarios documentación
COMMENT ON TABLE categories IS 'Categorías e-commerce con metadata paginación (MercadoLibre/Paris)';
COMMENT ON COLUMN categories.source_url IS 'URL categoría origen para re-crawling';
COMMENT ON COLUMN categories.total_pages IS 'Páginas detectadas en scrape inicial';
COMMENT ON COLUMN categories.breadcrumb IS 'Pan crumbs: Home > Electrónicos > Celulares';
COMMENT ON COLUMN categories.status IS 'ACTIVA=parcial, COMPLETA=100%, ERROR=falla';
