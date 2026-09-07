# Indexar API

API pública de indicadores económicos de Argentina (Java Spring Boot). Proyecto
de portfolio de Fernando Rodríguez — pieza que cierra el hueco de "backend
propio visible" diagnosticado en el plan de nivel senior del portfolio.

**Contexto completo, roadmap de 10+ proyectos y reglas de trabajo multi-proyecto:**
`C:\Users\Notebook\Downloads\Proyecto Trubx\Boveda - Obsidian\Fernando\PORTFOLIO 2026\`
— ver especialmente `02 Estrategia y Contenido\Plan Nivel Senior 2026.md`,
`02 Estrategia y Contenido\Roadmap de Proyectos.md` y
`11 Reglas y Flujo de Trabajo\Reglas Multi-Proyecto.md`.

## Regla de oro de este proyecto

**No hay dato fabricado.** Todo lo que la API sirve viene de una ingesta real
contra dolarapi.com. Si algo no se puede verificar contra la fuente real, no
se inventa — se documenta como límite conocido (ver README.md).

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA · PostgreSQL · Flyway ·
springdoc-openapi · Caffeine · Testcontainers · JUnit 5 · Mockito

## Comandos

```
./mvnw spring-boot:run       # levanta la API (necesita Postgres — ver docker-compose.yml)
./mvnw test                   # unitarios + @WebMvcTest + Testcontainers (necesita Docker)
./mvnw test -Dtest=IngestServiceTest   # el único nivel de test que NO necesita Docker
./mvnw compile / test-compile # chequeo rápido sin correr nada
docker compose up -d db       # Postgres local para desarrollo
```

## Notas de compatibilidad — Spring Boot 4

Spring Boot 4 partió los starters de test en piezas finas (algo distinto a la
experiencia de Boot 3.x, y las guías/foros viejos asumen los paths viejos):

- `@WebMvcTest` vive en `org.springframework.boot.webmvc.test.autoconfigure`
  (no en `org.springframework.boot.test.autoconfigure.web.servlet`, que era el
  path de Boot 3.x).
- El coordinate del parent POM es `4.1.1` a secas — **sin** el sufijo
  `.RELEASE` que usaba Boot 2.x (el metadata endpoint de start.spring.io
  todavía devuelve el id con `.RELEASE`, es un artefacto de la API, no el
  coordinate real de Maven Central).
- Requiere Java 21+ por springdoc-openapi 3.x. El proyecto se generó
  originalmente pidiendo Java 17 y hubo que subirlo — si algo no compila
  por versión de Java, revisar `java.version` en `pom.xml` primero.

## Gotchas ya encontrados (para no perder tiempo redescubriéndolos)

- **`@EnableCaching`/`@EnableScheduling` NO van en la clase `@SpringBootApplication`.**
  `@WebMvcTest` usa esa clase como fuente de configuración y respeta cualquier
  anotación declarada directamente sobre ella — un `@WebMvcTest` que no tiene
  nada que ver con cache termina exigiendo un `CacheManager` igual. Van en
  `config/AsyncConfig.java`, separados.
- **Postgres `timestamptz` trunca a microsegundos; `Instant.now()` de Java trae
  nanosegundos.** Un valor guardado y releído desde la base nunca es
  `.equals()` al `Instant` original si no se trunca antes con
  `.truncatedTo(ChronoUnit.MICROS)`. Afecta cualquier test que compare un
  `Instant` contra lo que vuelve de una columna `timestamptz`.

## Principios de arquitectura (no negociables)

1. **Idempotencia real, verificada con test** — no alcanza con documentar que
   algo es idempotente, tiene que haber un test que reintente la misma
   operación y verifique que no duplica nada (ver `IngestServiceTest`).
2. **Un fallo externo nunca tira la API abajo** — si dolarapi.com está caído,
   se loguea y se sigue sirviendo el último dato bueno. Nunca un 500 por
   culpa de una dependencia de terceros caída.
3. **Las migraciones son la única fuente de verdad del esquema** — nunca
   `ddl-auto: update`. Todo cambio de esquema es un archivo nuevo en
   `db/migration`.
4. **Todo endpoint nuevo necesita:** un test de contrato (`@WebMvcTest`) y,
   si toca la base, un test de integración con Testcontainers.
5. **Nada de "TODO" sin dueño ni fecha.** Si algo queda pendiente, va al
   README bajo "Límites conocidos" con la razón — no un comentario suelto en
   el código que nadie vuelve a leer.

## Definición de "terminado" (antes de dar por cerrado cualquier feature)

- [ ] `./mvnw test-compile` limpio
- [ ] `./mvnw test -Dtest=<TestNuevo>` pasa (Docker si aplica)
- [ ] README actualizado si cambió el contrato de la API
- [ ] Sin secretos hardcodeados — todo por variable de entorno (`.env.example`
      actualizado si se agrega una nueva)
- [ ] Si es una feature visible desde afuera (nuevo endpoint, nuevo indicador):
      referenciarla desde el portfolio (`content/side-projects.ts` o un case
      study nuevo) una vez desplegada — ver Reglas Multi-Proyecto en la bóveda.
