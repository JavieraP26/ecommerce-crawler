# ADR 001: Usar Anotaciones JPA en Capa Domain

## Estado
Aceptado

## Contexto
Este proyecto implementa Clean Architecture con separación de capas (domain, application, infrastructure).

En Clean Architecture pura, la capa **domain** debe ser independiente de frameworks externos (como JPA), lo que implicaría:
- Crear POJOs puros en `domain/model/` (sin `@Entity`)
- Crear entidades JPA en `infrastructure/persistence/jpa/` (con `@Entity`)
- Implementar mappers bidireccionales (`Product` ↔ `ProductEntity`)

Sin embargo, esto genera:
- **Duplicación**: Dos clases casi idénticas por cada entidad
- **Verbosidad**: Mappers manuales con 10+ campos por entidad
- **Overhead**: 3-5 días adicionales de desarrollo para proyecto de prueba técnica

## Decisión
**Aceptamos anotaciones JPA (`@Entity`, `@Table`, `@Column`) en la capa domain** como trade-off pragmático.

### Justificación
1. **Contexto de prueba técnica**: 3-5 días de desarrollo
2. **Abstracción mediante Ports**: La capa application solo depende de `ProductRepositoryPort` (interfaz), no de implementación JPA
3. **Beneficio vs costo**: Ahorramos 40% de tiempo sin comprometer testabilidad ni cambio de persistencia (vía ports)

### Mitigaciones
- **Ports/Adapters**: `application/port/out/ProductRepositoryPort.java` abstrae persistencia
- **Tests unitarios**: Services usan mocks de ports, no DB real
- **Documentación**: Este ADR justifica la decisión

## Consecuencias

### Positivas
✅ Desarrollo más rápido (sin duplicación/mappers)  
✅ Menos código de mantenimiento  
✅ Spring Data JPA funciona directamente  
✅ Foco en lógica de scraping/validación (core del proyecto)

### Negativas
❌ Domain acoplado a Jakarta Persistence API  
❌ Cambiar a MongoDB requeriría refactor de entidades  
❌ No es Clean Architecture "pura" académica

### Mitigadas
⚠️ Cambio de persistencia sigue siendo viable vía ports (solo afecta infrastructure)  
⚠️ Tests de domain usan H2 in-memory (rápidos)

### Getters/Setters Públicos (Consecuencia JPA)

**Decisión**: Getters/setters públicos en domain entities.

**Justificación**:
- **JPA field/property access**: Simplifica integración vs configuraciones complejas
- **Legibilidad**: Facilita debugging por revisores
- **Pragmatismo**: Foco en crawling vs encapsulación estricta

**Mitigaciones**:
- Lógica dominio en métodos (`hasDiscount()`, `getDiscountPercentage()`)
- Validaciones `@Column(nullable=false, length=...)`
- Services como punto único de modificación

**Futuro**: `@Value` + factory methods para multi-store.


## Alternativas Consideradas

### Opción Rechazada: Domain Puro + Mappers
```
domain/model/Product.java (POJO)
infrastructure/persistence/jpa/ProductEntity.java (@Entity)
infrastructure/persistence/mapper/ProductMapper.java
```

**Rechazada por**: 

- Overhead de desarrollo no justificado para alcance de prueba técnica
- Duplicación de código (2 clases + mapper por cada entidad)
- Incluso usando MapStruct, se mantiene la duplicación conceptual y el overhead cognitivo para el alcance del proyecto


## Referencias
- [Hexagonal Architecture - DDD](https://codely.com/en/blog/hexagonal-architecture-ddd)
- [Pragmatic Clean Architecture in Spring Boot](https://www.linkedin.com/pulse/practical-approach-clean-architecture-java-spring-boot-nakamura-na8if)
- Spring Boot JPA Best Practices (común en proyectos reales)

## Notas
Si este proyecto evolucionara a sistema multi-tenant con múltiples stores de persistencia (SQL + NoSQL), se debería refactorizar a domain puro + mappers (inversión justificada en ese contexto).

---
**Fecha**: 2026-01-09 (ISO 8601) 
**Autor**: Javiera Pulgar  
**Revisores**: Auto-aprobado (proyecto individual)
