# Workflow Git - Ecommerce Crawler

Proyecto individual siguiendo estándares enterprise para prueba técnica.

## Estrategia de Ramas

### main
- Código funcional y estable
- Solo actualizaciones via Pull Requests
- Cada PR = milestone completado (entidad, scraper, tests)

### feature/*
- Una rama por funcionalidad
- Formato: `feature/nombre-descriptivo`
- Ejemplos:
    - `feature/domain-entities`
    - `feature/mercadolibre-scraper`
    - `feature/global-exception-handler`

## Convenciones de Commit

Seguimos [Conventional Commits](https://www.conventionalcommits.org/):
```
<tipo>: <descripción>
[cuerpo opcional]
```

### Tipos
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bugs
- `refactor`: Refactorización sin cambiar comportamiento
- `test`: Agregar/modificar tests
- `docs`: Documentación (README, Javadoc)
- `chore`: Mantenimiento (dependencias, configs)

### Ejemplos
```bash
feat: agregar entidades domain Product y Category
fix: corregir parsing de precio en MercadoLibre scraper
test: agregar tests unitarios para ScraperFactory
docs: actualizar README con modelo de datos
refactor: aplicar patrón Strategy a scrapers
```

## Flujo de Trabajo

# 1. Crear rama desde main actualizado
git checkout main && git pull
git checkout -b feature/nombre-funcionalidad

# 2. Desarrollar con commits frecuentes
git add .
git commit -m "feat: descripción clara"

# 3. Push cuando alcances milestone
git push -u origin feature/nombre-funcionalidad

# 4. Crear PR en GitHub
# - Usar template `.github/PULL_REQUEST_TEMPLATE.md`
# - Auto-revisar checklist
# - Merge cuando esté completo

# 5. Limpiar rama local
git checkout main
git pull
git branch -d feature/nombre-funcionalidad

### Pull Requests

Cada PR documenta un componente completo:
Entidades: Product, Category con JPA
Scraper: MercadoLibre con Jsoup + tests
API: Controller + DTO + validaciones
Infraestructura: Docker, Flyway migrations

Usa el template `.github/PULL_REQUEST_TEMPLATE.md` para consistencia.

## Reglas

✅ Hacer:
- Commits descriptivos y atómicos
- PR por cada feature completada
- Actualizar README si impacta setup/uso

❌ Evitar:
- Commits tipo "WIP" o "fix"
- Mezclar múltiples features en un PR
- Push de código que no compila