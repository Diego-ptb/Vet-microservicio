# Quick Start - Pruebas Unitarias

## 🚀 Comandos Rápidos

### Ejecutar todas las pruebas
```bash
mvn test
```

### Ejecutar pruebas de un componente
```bash
mvn test -Dtest=VetServiceTest      # Servicio
mvn test -Dtest=VetControllerTest   # Controlador
mvn test -Dtest=VetRepositoryTest   # Repositorio
```

### Ver cobertura de código
```bash
mvn clean test jacoco:report
# Luego abre: target/site/jacoco/index.html
```

## 📊 Resumen de Pruebas

### Total de Pruebas: **45+**
- **VetServiceTest**: 16 pruebas
- **VetControllerTest**: 15 pruebas  
- **VetRepositoryTest**: 8 pruebas

## ✅ Casos de Prueba Clave

### Servicio (VetService)
✓ Crear veterinaria  
✓ Validar coordenadas  
✓ Actualizar veterinaria  
✓ Control de acceso (admin vs refugio)  
✓ Buscar veterinarias cercanas  

### Controlador (VetController)
✓ Endpoints protegidos requieren autenticación  
✓ ADMIN puede crear y actualizar  
✓ REFUGIO puede crear y actualizar su propia vet  
✓ Validación de entrada (latitud/longitud)  
✓ GET /vets es público  

### Repositorio (VetRepository)
✓ Guardar y recuperar  
✓ Buscar por usuario  
✓ Timestamps automáticos  
✓ Actualización de datos  

## 🎯 Verificar Éxito

```bash
mvn clean test
```

Resultado esperado:
```
BUILD SUCCESS
Tests run: 45+, Failures: 0, Errors: 0
```

## 📝 Estructura de Carpetas

```
src/
├── main/java/.../
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
└── test/java/.../
    ├── controller/VetControllerTest.java
    ├── service/VetServiceTest.java
    └── repository/VetRepositoryTest.java
```

## 🔧 Troubleshooting

| Problema | Solución |
|----------|----------|
| Tests fallan sin cambios | `mvn clean test` |
| Error H2 | Reiniciar Maven |
| Timeout | Ejecutar un test a la vez |

## 📚 Más Información

Ver [README_TESTS.md](./README_TESTS.md) para la guía completa.
