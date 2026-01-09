# 🛒 E-commerce Web Crawler

Web crawler desarrollado en **Java 21** con **Spring Boot 4.0.1** para extraer información de productos desde sitios de e-commerce y almacenarla en PostgreSQL.

Implementa **Clean Architecture** con principios **SOLID**, diseñado para escalabilidad multi-sitio.

---

## 🎯 Funcionalidades

### 1. Crawling de Ficha de Producto
Extrae datos completos de un producto individual:
- SKU (identificador único)
- Nombre completo
- Precio actual y anterior
- URLs de imágenes
- Estado de disponibilidad

### 2. Crawling de Categoría con Paginación
Recorre automáticamente todas las páginas de una categoría:
- Total de páginas disponibles
- Productos por página
- Listado completo de productos

### 3. Multi-Sitio (Extensible)
Soporta múltiples sitios e-commerce mediante patrón Strategy:
- ✅ MercadoLibre Argentina
- ✅ Paris.cl
- ✅ Tercer sitio aún no seleccionado

---

## 🏗️ Arquitectura

### Clean Architecture + Hexagonal

```
├── domain/                  # Entidades y lógica de negocio pura
├── application/             # Use cases, ports (contratos)
│   ├── port/in/            # Interfaces para controllers
│   └── port/out/           # Interfaces para repositorios/scrapers
└── infrastructure/          # Adapters (implementaciones)
├── adapter/
│   ├── inbound/        # Controllers REST
│   └── outbound/       # Scrapers por sitio
└── persistence/        # JPA repositories
```

### Stack Tecnológico
- **Backend**: Spring Boot 4.0.1, Java 21
- **Scraping**: Jsoup 1.17.2 (Selenium 4.19.1 como fallback)
- **Base de Datos**: PostgreSQL 16 + Flyway migrations
- **Containerización**: Docker + Docker Compose
- **Testing**: JUnit 5, Mockito, TestContainers

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 21 (JDK)
- Maven 3.9+
- Docker Desktop (para PostgreSQL)

### 1. Clonar Repositorio
```bash
git clone https://github.com/tu-usuario/ecommerce-crawler.git
cd ecommerce-crawler
```

### 2. Levantar Base de Datos
```bash
docker-compose up -d postgres
```

### 3. Ejecutar Aplicación
```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`

### 4. Ejecutar Tests
```bash
mvn test
```

---

## 📡 Endpoints

### Crawling de Producto Individual

**Request:**
```http
POST /api/v1/crawl/product
Content-Type: application/json
```

```json
{
  "url": "https://www.mercadolibre.com.ar/sierra-circular-7-14-185-190mm-1600w-hs7010-makita/p/MLA19813486"
}
```

**Respuesta:**
```json
{
  "sku": "MLA19813486",
  "name": "Sierra Circular 7-1/4 185-190mm 1600w Hs7010 Makita",
  "currentPrice": 125999.00,
  "previousPrice": 139999.00,
  "images": [
    "https://http2.mlstatic.com/D_NQ_NP_...",
    "..."
  ],
  "available": true,
  "source": "MercadoLibre"
}
```

### Crawling de Categoría

```http
POST /api/v1/crawl/category
Content-Type: application/json
```

```json
{
  "url": "https://www.paris.cl/tecnologia/celulares/smartphone/"
}
```

---

## 🗄️ Modelo de Datos

Pendiente, se añadirá cuando se realicen las migraciones.

---

## 🧪 Testing

```bash
# Tests unitarios
mvn test -Dtest="*Test"

# Tests de integración (con TestContainers)
mvn verify
```

**Cobertura**: Mínimo 80% en service layer y use cases.

---

## 📋 Decisiones Técnicas

### ¿Por qué Jsoup sobre Selenium?
- **Performance**: 10x más rápido para HTML estático
- **Recursos**: Consume menos CPU/RAM
- **Casos de uso**: MercadoLibre/Paris usan server-side rendering
- **Fallback**: Selenium disponible para sitios con JS pesado

### ¿Por qué Clean Architecture?
- **Testabilidad**: Lógica de negocio desacoplada de frameworks
- **Escalabilidad**: Agregar nuevos scrapers sin modificar core
- **Mantenibilidad**: Cambios en DB/scraping no afectan use cases

### Patrón Strategy para Scrapers
Cada sitio tiene su adapter (MercadoLibreScraper, ParisScraper) implementando `ScraperPort`. Factory decide el scraper según dominio URL.

---

## 🐛 Manejo de Errores

Se utilizará una GlobalExceptionHandler que cubrira:
- ProductNotFoundException (404)
- ScrapingException (500)
- InvalidUrlException (400)
- RateLimitExceededException (429)
- SiteUnavailableException (503)
- ParsingException (500)
- NetworkTimeoutException (504)
- CaptchaDetectedException (403)

---

## 🔐 Configuración

### Variables de Entorno Obligatorias

Por seguridad, **NO se usan valores hardcodeados**. 
Todas las configuraciones se cargan desde variables de entorno siguiendo el formato de .envexample

---

## 🚢 Deployment

### Docker Build
```bash
mvn clean package
docker build -t ecommerce-crawler:latest .
docker-compose up
```

---

## 👨‍💻 Autora

**Javiera Pulgar**  
[LinkedIn](https://www.linkedin.com/in/javiera-pulgar-rodriguez/) | [GitHub](https://github.com/JavieraP26)

---


