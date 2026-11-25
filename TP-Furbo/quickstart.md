# Quickstart Validation Guide

## Prerequisites
- Docker Desktop
- PostgreSQL running on localhost:5432
- Database `tp-furbo` exists

## Setup
1. Configure `application-local.properties` with:
   spring.datasource.url=jdbc:postgresql://localhost:5432/tp-furbo
   spring.datasource.username=postgres
   spring.datasource.password=root

## Run Scenarios
1. Register user:
   curl -X POST http://localhost:8080/users -d '{"email":"test@test.com", "name":"Test", "password":"password"}' -H "Content-Type: application/json"

2. Verify Portfolio existence:
   - Check database `portfolios` table for row with `user_id` equal to created user.

## Running Tests
- ./gradlew test

## Actuator & Prometheus Monitoring

### Endpoints Disponibles
- `/actuator/health` (Público): Devuelve el estado de salud de la aplicación.
- `/actuator/info` (Público): Información general de la aplicación.
- `/actuator/metrics` (Protegido): Métricas detalladas de Micrometer.
- `/actuator/prometheus` (Protegido): Métricas en formato compatible con Prometheus.

### Cómo Consultar
1. **Health (Público)**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
2. **Prometheus (Protegido con JWT)**:
   ```bash
   # Obtener token JWT autenticándose en /api/auth/login
   TOKEN="<tu_token_jwt>"
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/prometheus
   ```

### Consideraciones para Scraping con Prometheus (Autenticación)
Dado que Prometheus realiza peticiones de *scrape* automáticas y no puede manejar fácilmente tokens JWT dinámicos de corta duración, existen las siguientes alternativas recomendadas para producción:
1. **Puerto de Gestión Separado (`management.server.port`)**: Configurar un puerto independiente (ej. `9090`) para Actuator protegido en la red interna (firewall/VPN) de modo que Prometheus pueda scrapear sin autenticación externa pero aislado de la red pública.
2. **Basic Authentication**: Configurar Spring Security con Basic Auth exclusivamente para los endpoints de Actuator (`/actuator/**`) para que Prometheus pueda autenticarse mediante usuario y contraseña estáticos en su configuración de *scrape*.
3. **Service Mesh / Reverse Proxy**: Utilizar un proxy reverso (como Nginx o Istio) delante de la aplicación que inyecte o valide credenciales para el path `/actuator/prometheus`.
