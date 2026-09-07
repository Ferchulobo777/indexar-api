# Indexar API

API pública de indicadores económicos de Argentina. Hoy: cotizaciones del dólar
(oficial, blue, MEP, CCL, cripto) ingeridas cada 15 minutos desde
[dolarapi.com](https://dolarapi.com) (gratis, sin API key), con historial
consultable. El esquema está diseñado para sumar más indicadores (inflación,
tasas, paritarias) sin romper el contrato existente.

Construido como pieza de portfolio: el objetivo no es "otra API de dólar" —
es demostrar, con código real y verificable, las prácticas que separan una
API de juguete de una de producción.

## Por qué existe

Ver el diagnóstico completo en
[`02 Estrategia y Contenido/Plan Nivel Senior 2026.md`](../../Proyecto%20Trubx/Boveda%20-%20Obsidian/Fernando/PORTFOLIO%202026/02%20Estrategia%20y%20Contenido/Plan%20Nivel%20Senior%202026.md)
de la bóveda del portfolio: el CV declara experiencia backend real (Node.js,
Python, **Java Spring Boot**, RESTful APIs, microservicios, PostgreSQL) pero
el portfolio público, hasta este proyecto, no tenía ni una sola pieza de
backend propio — solo frontends consumiendo APIs de terceros. Este proyecto
cierra ese hueco.

## Qué tiene

- **Ingesta real y programada** (`@Scheduled`, cada 15 min) contra la API
  pública de dolarapi.com, con `java.net.http.HttpClient` nativo — sin
  dependencias pesadas para una sola llamada GET.
- **Idempotencia real, no de palabra**: una restricción única
  `(series_id, observed_at)` en la base hace que reintentar la misma ingesta
  nunca duplique una fila — verificado con test, no solo documentado.
- **Manejo explícito de fallos**: si dolarapi.com está caído, el scheduler
  loguea y reintenta en el próximo ciclo — la API sigue sirviendo el último
  dato persistido, nunca cae por una dependencia externa caída.
- **Migraciones versionadas con Flyway** — el esquema y el catálogo de series
  están en control de versiones, no creados a mano.
- **Documentación OpenAPI en vivo** — Swagger UI navegable en `/swagger-ui.html`,
  no una captura de pantalla.
- **Cache** (Caffeine) en el catálogo de series — no pega a la base en cada
  request de un dato que cambia una vez por deploy.
- **Handler global de excepciones** — ningún endpoint devuelve un stack trace
  crudo; los errores de negocio mapean a códigos HTTP específicos.
- **Tests en 3 niveles**, cada uno probando lo que le corresponde:
  - `IngestServiceTest` — unitario, mockea la API externa y los repositorios;
    verifica la lógica de idempotencia y manejo de series desconocidas.
  - `IndicatorControllerTest` — `@WebMvcTest`, verifica el contrato HTTP en
    aislamiento (rutas, códigos de estado, forma del JSON).
  - `IndicatorQueryServiceIntegrationTest` — Testcontainers con Postgres real
    + migraciones de Flyway reales; valida que las queries JPQL custom
    (`findHistory`, `findLatest`) funcionan contra una base real, no un mock.
- **Health checks** vía Spring Actuator (`/actuator/health`).

## Límites conocidos (a propósito, no por descuido)

- No hay autenticación todavía — es de lectura pública y de bajo riesgo
  (cotizaciones públicas). Si se agregan indicadores sensibles, entra API
  key + rate limiting antes de exponerlos.
- Sin rate limiting en `/api/v1/**` — próximo paso documentado en
  `docs/ROADMAP.md`.
- El scheduler corre en el mismo proceso que la API (no hay un worker
  separado). Para el volumen de datos actual (cotizaciones cada 15 min) es
  una decisión correcta; no lo sería si se agregan indicadores de alta
  frecuencia.
- Sin caché distribuido (Caffeine es in-memory, por instancia) — no importa
  con una sola instancia corriendo; sí importaría al escalar horizontalmente.

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA · PostgreSQL · Flyway ·
springdoc-openapi (Swagger UI) · Caffeine · Testcontainers · JUnit 5 · Mockito

## Correr en local

Requiere Docker (para Testcontainers en los tests) y una instancia de
PostgreSQL para correr la app (o usar `docker compose up -d db`, ver
`docker-compose.yml`).

```bash
./mvnw spring-boot:run     # levanta la API en :8080
./mvnw test                 # corre los 3 niveles de test (usa Testcontainers)
```

Documentación interactiva una vez levantada: `http://localhost:8080/swagger-ui.html`

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/series` | Catálogo de series disponibles |
| GET | `/api/v1/series/{code}/latest` | Último valor observado de una serie |
| GET | `/api/v1/series/{code}/history?from=...&to=...` | Histórico en un rango de fechas (ISO-8601) |
| GET | `/actuator/health` | Estado de salud de la API |

## Estructura

```
src/main/java/ar/com/ferrodriguez/indexar/
  domain/      # Entidades JPA (IndicatorSeries, IndicatorValue)
  repository/  # Spring Data repositories + queries JPQL custom
  ingest/      # Cliente HTTP + orquestación + scheduler de la ingesta
  service/     # Lógica de negocio de lectura (cache, validación)
  web/         # Controllers REST, DTOs, exception handler
  config/      # Configuración de OpenAPI
```
