<div align="center">

# 📊 Indexar API

**Indicadores económicos de Argentina, en tiempo real y con historial real.**
Cotizaciones del dólar (oficial, blue, MEP, CCL, cripto) ingeridas cada 15 minutos desde una fuente pública, sin API key. El esquema está pensado para sumar más indicadores sin romper el contrato.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io)
[![Testcontainers](https://img.shields.io/badge/Tested_with-Testcontainers-2496ED?style=for-the-badge&logo=testcontainers&logoColor=white)](https://testcontainers.com)

**[🔗 Ver API en vivo](#)** &middot; **[📖 Swagger UI](#)**

</div>

<br />

Construido como pieza de portfolio: el objetivo no es "otra API de dólar" —
es demostrar, con código real y verificable, las prácticas que separan una
API de juguete de una de producción.

## Qué tiene

- **Ingesta real y programada** (`@Scheduled`, cada 15 min) contra la API
  pública de [dolarapi.com](https://dolarapi.com) (gratis, sin key), con
  `java.net.http.HttpClient` nativo — sin dependencias pesadas para una sola
  llamada GET.
- **Idempotencia real, no de palabra**: una restricción única
  `(series_id, observed_at)` en la base hace que reintentar la misma ingesta
  nunca duplique una fila — verificado con test, no solo documentado.
- **Manejo explícito de fallos**: si dolarapi.com está caído, el scheduler
  loguea y reintenta en el próximo ciclo — la API sigue sirviendo el último
  dato persistido, nunca cae por una dependencia de terceros caída.
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
- **Deploy como código**: `render.yaml` (Blueprint) + `Dockerfile` multi-stage
  — clonar, conectar y desplegar sin tocar configuración a mano salvo la
  connection string de la base.

## Límites conocidos (a propósito, no por descuido)

- No hay autenticación todavía — es de lectura pública y de bajo riesgo
  (cotizaciones públicas). Si se agregan indicadores sensibles, entra API
  key + rate limiting antes de exponerlos.
- Sin rate limiting en `/api/v1/**` todavía — próximo paso del roadmap.
- El scheduler corre en el mismo proceso que la API (no hay un worker
  separado). Para el volumen de datos actual (cotizaciones cada 15 min) es
  una decisión correcta; no lo sería si se agregan indicadores de alta
  frecuencia.
- Sin caché distribuido (Caffeine es in-memory, por instancia) — no importa
  con una sola instancia corriendo; sí importaría al escalar horizontalmente.
- Free tier de hosting: la instancia puede dormirse tras 15 min sin tráfico
  (primer request después de eso tarda unos segundos en responder).

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA · PostgreSQL (Neon) · Flyway ·
springdoc-openapi (Swagger UI) · Caffeine · Docker · Testcontainers · JUnit 5 · Mockito

## Correr en local

Requiere Docker (para Testcontainers en los tests) y una instancia de
PostgreSQL para correr la app (`docker compose up -d db` levanta una local).

```bash
cp .env.example .env         # completar si hace falta
./mvnw spring-boot:run        # levanta la API en :8080
./mvnw test                    # corre los 3 niveles de test (usa Testcontainers)
```

Documentación interactiva una vez levantada: `http://localhost:8080/swagger-ui.html`

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/series` | Catálogo de series disponibles |
| GET | `/api/v1/series/{code}/latest` | Último valor observado de una serie |
| GET | `/api/v1/series/{code}/history?from=...&to=...` | Histórico en un rango de fechas (ISO-8601) |
| GET | `/actuator/health` | Estado de salud de la API |
| GET | `/swagger-ui.html` | Documentación interactiva (OpenAPI) |

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

## Deploy

Pensado para desplegarse gratis con dos servicios separados a propósito
(el Postgres gratis de la mayoría de los PaaS expira o se borra a los
30 días; una base serverless dedicada no):

1. **Base de datos** — [Neon](https://neon.tech) (Postgres serverless, tier
   gratis sin expiración para proyectos chicos).
2. **Aplicación** — [Render](https://render.com), Blueprint (`render.yaml`
   incluido): conectar el repo, pegar la connection string de Neon como
   `DB_URL`/`DB_USER`/`DB_PASSWORD`, deploy.

## Créditos

Diseñado y desarrollado por [Fernando Rodríguez](https://github.com/Ferchulobo777).
Datos provistos por una API pública de cotizaciones — este proyecto no ofrece
asesoramiento financiero, la información se brinda "tal cual".
