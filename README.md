# Vet Service

Microservicio para gestionar veterinarias con coordenadas para mostrar en mapa.

## Tecnologías
- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- JWT Security
- Swagger/OpenAPI

## Integración con Auth Service
- Cada veterinaria está asociada a un usuario del auth-service (userId).
- El UUID del usuario se obtiene del JWT para crear/actualizar veterinarias.
- REFUGIO puede gestionar solo su propia veterinaria.
- ADMIN puede gestionar cualquier veterinaria.

## Configuración
1. Configurar PostgreSQL en `application.properties`
2. Ejecutar `mvn spring-boot:run`

## Endpoints
- POST /vets (ADMIN/REFUGIO) - Crear veterinaria para el usuario autenticado
- PATCH /vets/{id} (REFUGIO/ADMIN) - Actualizar veterinaria (solo propietario o admin)
- GET /vets (público)
- GET /vets/my (REFUGIO/ADMIN) - Obtener veterinaria del usuario actual
- GET /vets/{id} (público)
- GET /vets/nearby?lat=&lng=&radius= (público)

## Documentación API
Accede a la documentación Swagger en: `http://localhost:8080/swagger-ui.html`

## Seguridad
Integrar con auth-service existente para JWT.