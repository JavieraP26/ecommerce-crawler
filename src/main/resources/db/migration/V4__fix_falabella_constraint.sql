-- V4__fix_falabella_constraint.sql
-- Fix: Permite source_url NULL solo para Falabella
-- Contexto: Falabella tiene URLs dinámicas/hash que no podemos extraer confiablemente
-- Tradeoff: Prioridad a guardar productos vs validación estricta

-- Agregar constraint condicional
ALTER TABLE products
    ADD CONSTRAINT chk_source_url_falabella
        CHECK (source_url IS NOT NULL OR source = 'FALABELLA');

-- Documentar en columna (alternativa válida)
COMMENT ON COLUMN products.source_url IS
    'URL del producto. Puede ser NULL solo para FALABELLA debido a URLs dinámicas';
