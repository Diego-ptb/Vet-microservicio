# Guía de Pruebas Unitarias - Vet Service

Este documento describe cómo ejecutar las pruebas unitarias para el servicio de Veterinarias.

## Estructura de Pruebas

El proyecto contiene pruebas unitarias e integración para los siguientes componentes:

### 1. **VetServiceTest** (`src/test/java/.../service/VetServiceTest.java`)
Pruebas unitarias del servicio de lógica de negocio:
- Creación de veterinarias
- Validación de coordenadas (latitud/longitud)
- Actualización de veterinarias
- Búsqueda de veterinarias por ID y usuario
- Búsqueda de veterinarias cercanas
- Manejo de excepciones

### 2. **VetControllerTest** (`src/test/java/.../controller/VetControllerTest.java`)
Pruebas de integración de los endpoints REST:
- Autenticación y autorización
- Validación de entrada
- Respuestas HTTP
- Control de roles (ADMIN, REFUGIO)

### 3. **VetRepositoryTest** (`src/test/java/.../repository/VetRepositoryTest.java`)
Pruebas de integración con la base de datos:
- Búsqueda de datos
- Guardado y actualización
- Timestamps automáticos
- Eliminación de registros

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- Base de datos H2 configurada para pruebas (se inicia automáticamente)

## Ejecución de Pruebas

### 1. Ejecutar todas las pruebas

```bash
mvn test
```

### 2. Ejecutar pruebas de un componente específico

```bash
# Pruebas del servicio
mvn test -Dtest=VetServiceTest

# Pruebas del controlador
mvn test -Dtest=VetControllerTest

# Pruebas del repositorio
mvn test -Dtest=VetRepositoryTest
```

### 3. Ejecutar un test específico

```bash
# Ejemplo: Ejecutar solo el test de creación de veterinaria
mvn test -Dtest=VetServiceTest#testCreateVet_Success
```

### 4. Ejecutar pruebas con cobertura de código

```bash
mvn clean test jacoco:report
```

El reporte de cobertura se genera en: `target/site/jacoco/index.html`

### 5. Ejecutar pruebas en modo verbose

```bash
mvn test -X
```

## Casos de Prueba Principales

### VetServiceTest
| Método | Descripción |
|--------|-------------|
| `testCreateVet_Success` | Crear veterinaria exitosamente |
| `testCreateVet_UserAlreadyHasVet` | Excepción cuando el usuario ya tiene veterinaria |
| `testCreateVet_InvalidLatitude` | Validar latitud inválida |
| `testCreateVet_InvalidLongitude` | Validar longitud inválida |
| `testUpdateVet_Success` | Actualizar veterinaria existente |
| `testUpdateVet_AccessDenied` | Denegar acceso a veterinaria ajena |
| `testUpdateVet_AdminAccess` | Admin puede actualizar cualquier veterinaria |
| `testGetAllVets_Success` | Obtener todas las veterinarias |
| `testGetNearbyVets_Success` | Buscar veterinarias cercanas |

### VetControllerTest
| Método | Descripción |
|--------|-------------|
| `testCreateVet_Admin` | Crear veterinaria con rol ADMIN |
| `testCreateVet_Refugio` | Crear veterinaria con rol REFUGIO |
| `testCreateVet_NoAuth` | Denegar creación sin autenticación |
| `testUpdateMyVet_Refugio` | Actualizar con rol REFUGIO |
| `testUpdateMyVet_Admin` | Actualizar con rol ADMIN |
| `testGetAllVets` | Obtener todas sin autenticación |
| `testCreateVet_InvalidLatitude` | Validación de entrada |

### VetRepositoryTest
| Método | Descripción |
|--------|-------------|
| `testSaveAndFindById` | Guardar y recuperar por ID |
| `testFindByUserId` | Buscar por ID de usuario |
| `testUpdateVet` | Actualizar veterinaria |
| `testAutoTimestamps_OnCreation` | Timestamps se establecen en creación |
| `testAutoTimestamps_OnUpdate` | Timestamps se actualizan en modificación |

## Configuración de Pruebas

### Archivos de Configuración

El proyecto usa `application.properties` para configurar la base de datos H2 en pruebas:

```properties
# Base de datos H2 para pruebas (automática)
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Interpretación de Resultados

### Ejecución Exitosa
```
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
```

### Con Fallos
```
Tests run: 45, Failures: 2, Errors: 1, Skipped: 0
[ERROR] testCreateVet_Success FAILED
```

## Troubleshooting

### Error: "No qualifying bean of type 'JwtAuthenticationFilter'"
**Solución**: Las pruebas del controlador están anotadas con `@SpringBootTest` que carga todo el contexto.

### Error: "Connection refused - Database"
**Solución**: Reiniciar Maven y dejar que H2 se inicie automáticamente.

### Error: "Tests taking too long"
**Solución**: Ejecutar solo las pruebas que necesitas con `-Dtest=NombrePrueba`

## Integración con CI/CD

Para incluir las pruebas en un pipeline de CI/CD:

```bash
# En GitHub Actions, GitLab CI, etc.
mvn clean verify
```

## Mejores Prácticas

1. **Ejecutar pruebas antes de commit**: `mvn test`
2. **Mantener pruebas independientes**: Cada prueba no debe depender de otra
3. **Usar @WithMockUser para pruebas con seguridad**: Para pruebas del controlador
4. **Actualizar pruebas cuando cambien requisitos**: Las pruebas documenten el comportamiento esperado
5. **Verificar cobertura regularmente**: Objetivo mínimo 80%

## Agregar Nuevas Pruebas

### Template para nuevo test unitario

```java
@Test
@DisplayName("Descripción clara del comportamiento")
void testNuevoComportamiento() {
    // Arrange: Preparar datos
    
    // Act: Ejecutar el código
    
    // Assert: Verificar resultados
}
```

### Ejemplo de test con Mock

```java
@Test
@DisplayName("Should handle error case")
void testErrorCase() {
    when(repository.findById(anyUUID())).thenThrow(new RuntimeException("Not found"));
    
    assertThrows(RuntimeException.class, () -> service.getVet(id));
}
```

## Comandos Útiles

```bash
# Limpiar y ejecutar
mvn clean test

# Ejecutar sin compilar (si ya está compilado)
mvn test -DskipTests=false

# Ejecutar solo pruebas que coincidan con un patrón
mvn test -Dtest=*Service*

# Generar reporte HTML
mvn surefire-report:report
mvn site
# Abierto en: target/site/surefire-report.html

# Ejecutar pruebas en paralelo
mvn test -DparallelTestCount=4
```

## Monitoreo de Cobertura

Después de ejecutar `mvn clean test jacoco:report`, verificar:
- **Línea de cobertura**: `target/site/jacoco/index.html` → Line Coverage
- **Rama de cobertura**: Verifica decisiones if/else, bucles, etc.
- **Métodos no cubiertos**: Identificar qué métodos necesitan pruebas adicionales

## Contacto y Soporte

Para preguntas sobre las pruebas, revisar:
1. Los comentarios `@DisplayName` en el código de prueba
2. La documentación de JUnit5
3. La documentación de Spring Testing

---

**Última actualización**: Mayo 2026
**Versión de JUnit**: 5.x (incluida con Spring Boot 3.2.0)
