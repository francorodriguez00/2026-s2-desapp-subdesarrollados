<!--
Version change: Template default → 1.0.0
List of modified principles: None (First initialization)
Added sections: Core Principles, Governance
Removed sections: None
-->

# TP-Furbo Constitution

## Core Principles

### I. Arquitectura en capas
El proyecto utiliza una división estricta: `controller` (solo habla con servicio), `servicio` (orquesta modelo y persistencia), `modelo` (dominio puro, sin dependencias), `persistencia` (conoce al modelo).

### II. Modelo rico
La lógica de negocio se centraliza en los objetos de modelo.

### III. Validaciones en niveles
Forma y tipo en DTO; existencia y lógica de acción en el servicio; invariantes del dominio en el modelo (lanzando excepciones del dominio).

### IV. Estrategia de Testing (NON-NEGOTIABLE)
Unitarios del dominio (sin Spring/DB), Integración (PostgreSQL real con Testcontainers), End-to-End con MockMvc en paquetes separados. Prohibido borrar/modificar tests existentes sin permiso.

### V. Definición de Terminado
Completado cuando: tests unitarios/integración pasan; aplicación compila y levanta local; colección de Postman actualizada.

### VI. Idioma y Nomenclatura
Documentos y mensajes de error en español. Identificadores sin acentos/ñ. Normativos y técnicos en inglés.

### VII. Observabilidad y Rendimiento
Logs estructurados, Correlation IDs, health checks, métricas de latencia/error. Uso estratégico de índices, caché en Redis para cotizaciones y APIs externas.

### VIII. YAGNI
Monolito modular inicial. Sin microservicios, colas o DBs adicionales sin justificación técnica explícita y medible.

## Especificaciones Técnicas
- **Stack:** Java 21, Spring Boot 3, PostgreSQL, Redis, React + TypeScript.
- **Seguridad:** Spring Security con JWT stateless.
- **Build/Migraciones:** Gradle; migraciones versionadas (Flyway o Liquibase).

## Governance
La constitución define las reglas base del proyecto. Cualquier cambio debe estar documentado y justificado. El principio YAGNI y la arquitectura en capas son pilares inamovibles.

**Version**: 1.0.0 | **Ratified**: 2026-09-16 | **Last Amended**: 2026-09-16
