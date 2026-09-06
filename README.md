# pricing-service-api

API REST que resuelve la tarifa aplicable a un producto de una cadena en una fecha
dada, cuando varias tarifas se solapan en el tiempo (se aplica la de mayor prioridad).
Arquitectura hexagonal + DDD dentro de un único módulo (`pricing`), en un monolito
modular verificado con Spring Modulith, sobre Spring Boot 4 / Java 21.

## Índice

- [Arranque rápido](#arranque-rápido)
- [El endpoint](#el-endpoint)
- [Documentación interactiva (OpenAPI / Swagger)](#documentación-interactiva-openapi--swagger)
- [Arquitectura](#arquitectura)
- [Configuración](#configuración)
- [Internacionalización](#internacionalización)
- [Testing](#testing)
- [Docker](#docker)
- [Seguridad de la imagen (Trivy)](#seguridad-de-la-imagen-trivy)
- [CI/CD](#cicd)
- [Stack](#stack)
- [Decisiones de diseño](#decisiones-de-diseño)
- [Monitorización](#monitorización)

## Arranque rápido

Requiere JDK 21 (el wrapper de Gradle se encarga del resto).

```bash
./gradlew bootRun
```

La aplicación arranca en `http://localhost:8080` con una base de datos H2 en memoria,
inicializada automáticamente con las 4 tarifas de ejemplo del enunciado
(`src/main/resources/data.sql`).

También se puede levantar con Docker (ver [Docker](#docker)):

```bash
docker compose -f docker/docker-compose.yaml up --build
```

## El endpoint

```
GET /api/v1/prices?applicationDate={fecha}&brandId={id}&productId={id}
```

| Parámetro         | Tipo            | Formato                               |
|-------------------|-----------------|---------------------------------------|
| `applicationDate` | fecha y hora    | ISO-8601, p.ej. `2020-06-14T10:00:00` |
| `brandId`         | entero positivo | identificador de la cadena            |
| `productId`       | entero positivo | identificador del producto            |

Devuelve identificador de producto, identificador de cadena, tarifa aplicada, fechas
de vigencia y precio final:

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&brandId=1&productId=35455"
```

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

**Respuestas:**

| Caso                                                      | HTTP                                                                  |
|-----------------------------------------------------------|-----------------------------------------------------------------------|
| Tarifa encontrada                                         | `200 OK`                                                              |
| Ninguna tarifa cubre esa fecha                            | `404 Not Found`                                                       |
| Parámetro ausente, con formato inválido, o id no positivo | `400 Bad Request`                                                     |
| Ruta inexistente                                          | `404 Not Found`                                                       |
| Error no controlado                                       | `500 Internal Server Error` (mensaje genérico, sin detalles internos) |

Los mensajes de error siguen la cabecera `Accept-Language` del cliente (ver
[Internacionalización](#internacionalización)).

## Documentación interactiva (OpenAPI / Swagger)

Con la aplicación arrancada:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI 3: `http://localhost:8080/v3/api-docs`

El spec se genera a partir del propio código (controlador, DTOs, anotaciones de
validación) y está traducido igual que los mensajes de error — depende del
`Accept-Language` de quien lo consulte.

## Arquitectura

Arquitectura hexagonal + DDD dentro de un único módulo de negocio (`pricing`), aislado
e independiente, verificado con Spring Modulith (`ApplicationModules.verify()` en
`ModularityTests`, se ejecuta en cada build):

```
com.github.maferrermartin
├── MaferrermartinApplication
└── pricing                                    ← único módulo de aplicación
    ├── domain
    │   └── model
    │       └── ApplicablePrice                ← value object del dominio
    ├── application
    │   ├── port.in
    │   │   └── FindApplicablePriceQuery        ← caso de uso (puerto de entrada)
    │   ├── port.out
    │   │   └── LoadApplicablePricePort         ← puerto de salida
    │   └── PricingApplicationService           ← implementa el caso de uso
    └── infrastructure
        ├── in.web                             ← adaptador REST
        │   ├── PriceController
        │   ├── PriceResponse / ErrorResponse
        │   ├── RestExceptionHandler
        │   └── OpenApiConfig
        └── out.persistence                    ← adaptador JPA/H2
            ├── PriceRateEntity
            ├── PriceRateJpaRepository
            └── JpaLoadApplicablePriceAdapter
```

Todas las clases de implementación (servicio de aplicación, adaptadores, entidad,
repositorio) son *package-private*; solo las interfaces de los puertos son visibles
fuera de su paquete, para reforzar el límite hexagonal.

## Configuración

`src/main/resources/application.properties`:

- **Base de datos**: H2 en memoria, inicializada con `data.sql` al arrancar
  (`spring.jpa.defer-datasource-initialization=true` para que Hibernate cree el
  esquema antes de insertar).
- **Actuator en puerto separado** (`management.server.port=8081`), con solo
  `health`, `prometheus` e `info` expuestos y sin detalles — nunca se publica en
  `docker-compose.yaml`, así que ninguno es alcanzable desde fuera del contenedor;
  solo el `HEALTHCHECK` del propio `Dockerfile` los consulta desde dentro.
- **Idioma**: `Accept-Language` con español por defecto (ver siguiente sección).

## Internacionalización

Los mensajes de error propios (`RestExceptionHandler`) y la documentación OpenAPI
están en `src/main/resources/messages.properties` (español, es el idioma por
defecto) y `messages_en.properties` (inglés) — el mecanismo estándar de Spring
(`MessageSource`), no un formato propio: añadir un idioma nuevo es solo crear
`messages_XX.properties` con las mismas claves, sin tocar código.

```bash
curl -H "Accept-Language: en" "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00&brandId=1"
# {"message":"Missing required parameter 'productId'", ...}

curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00&brandId=1"
# {"message":"Falta el parámetro obligatorio 'productId'", ...}
```

Un idioma sin fichero propio cae automáticamente al español. Los mensajes de Bean Validation
(p.ej. "debe ser mayor que 0") ya vienen traducidos de serie por Hibernate
Validator y siguen el mismo `Accept-Language` sin configuración extra.

## Testing

```bash
./gradlew test              # unitarios + integración (H2), ~30s, sin Docker
./gradlew performanceTest   # rendimiento contra Postgres real en Docker, bajo demanda
```

41 tests en 12 clases:

| Clase                               | Qué cubre                                                              |
|-------------------------------------|------------------------------------------------------------------------|
| `ModularityTests`                   | límites del módulo (`ApplicationModules.verify()`)                     |
| `PricingApplicationServiceTest`     | servicio de aplicación, con el puerto de salida mockeado               |
| `PricingApplicationServiceIT`       | servicio + persistencia real (H2), casos del enunciado                 |
| `JpaLoadApplicablePriceAdapterTest` | mapeo entidad → dominio, con el repositorio mockeado                   |
| `PriceRateJpaRepositoryTest`        | la consulta SQL en sí (`@DataJpaTest`), aislada                        |
| `PriceRateEntityTest`               | asignación correcta de los argumentos del constructor                  |
| `PriceControllerTest`               | controlador (`@WebMvcTest`), Optional→200/404, `Cache-Control`         |
| `PriceControllerIT`                 | los 5 casos del enunciado + errores + i18n + `X-Request-Id`            |
| `RestExceptionHandlerTest`          | cada handler de error, en español e inglés                             |
| `PriceResponseTest`                 | mapeo del DTO de respuesta                                             |
| `RequestIdFilterTest`               | genera/respeta el `X-Request-Id`, lo mete en el MDC y lo limpia        |

**Test de rendimiento** (`PricingModulePerformanceIT`, tag `performance`, excluido de
`./gradlew test`): levanta un PostgreSQL real en Docker (vía Testcontainers) con
~200.000 filas generadas aleatoriamente en un rango de 3 años, con solapamientos de
fechas deliberados (incluida una combinación con 50 tarifas solapadas el mismo día,
el peor caso para la desambiguación por prioridad). Exige que cada consulta resuelva
en menos de 200 ms, y verifica que el resultado devuelto sigue siendo el correcto a
esa escala.

**Cobertura de código** (JaCoCo, plugin nativo de Gradle): `./gradlew check` genera el
informe (`build/reports/jacoco/test/html/index.html`) y falla si la cobertura de
líneas baja del 70%.

## Docker

```bash
docker compose -f docker/docker-compose.yaml up --build
```

`Dockerfile` multi-stage: build con Gradle sobre `eclipse-temurin:21-jdk-noble`
(capas cacheadas para no re-descargar dependencias en cada cambio de código),
runtime sobre `eclipse-temurin:21-jre-noble` sin JDK, usuario no-root. Actuator en
un puerto de gestión (8081) que nunca se publica — el `HEALTHCHECK` del propio
contenedor lo consulta desde dentro.

## Seguridad de la imagen (Trivy)

```bash
docker compose -f docker/docker-compose.yaml build pricing-service-api
docker compose -f docker/docker-compose.yaml --profile scan run --rm trivy
```

No corre en un `up` normal (perfil `scan`, bajo demanda). Escanea la imagen ya
construida en busca de CVE `CRITICAL`/`HIGH`.

## CI/CD

**Hook local (`pre-push`)**: se instala solo (tarea Gradle `installGitHooks`,
enganchada a `test` — cualquiera que clone el repo y ejecute los tests una vez ya lo
tiene). Corre `./gradlew test` antes de dejar salir un `git push`; si fallan, lo
cancela. Se puede saltar con `git push --no-verify`, por eso no es la única capa de
protección.

**GitHub Actions** (`.github/workflows/ci.yml`), en cada push/PR a `master`/`develop`:

| Job            | Qué hace                                                       |
|----------------|----------------------------------------------------------------|
| `test`         | `./gradlew test`                                               |
| `docker-image` | construye la imagen y la escanea con Trivy (`CRITICAL`/`HIGH`) |

La protección real está en la regla de rama de GitHub (status check + PR
obligatorios) — el hook local es solo comodidad para feedback inmediato, no algo que
no se pueda saltar.

## Stack

Spring Boot 4.1 (Web, Data JPA, Validation, Actuator), Spring Modulith, H2,
springdoc-openapi, JUnit 5 + Mockito + AssertJ, Testcontainers (PostgreSQL), Docker,
Trivy. Sin librerías de utilidad de terceros (Lombok, MapStruct...) — records de
Java 21 y código explícito.

## Decisiones de diseño

- **Sin entidades de dominio ricas**: es un endpoint de solo lectura sin invariantes
  que proteger; forzar Entities/Value Objects con comportamiento sería sobre-ingeniería.
- **Prioridad resuelta en SQL, no en Java**: `ORDER BY priority DESC` + `LIMIT` en la
  consulta.
- **"No encontrado" no es una excepción**: `Optional<ApplicablePrice>` vacío es un
  resultado válido de una consulta, no un caso excepcional; el adaptador web decide
  que eso significa 404, no el dominio.
- **Excepciones de dominio**: no existen porque no hay ninguna invariante que se pueda violar en un GET.
- **Visibilidad *package-private* por defecto**: solo las interfaces de los puertos
  son públicas fuera de su paquete; todo lo demás (servicios, adaptadores, entidad,
  repositorio) es interno.

## Monitorización

- **Métricas** (`/actuator/prometheus`, formato Prometheus) y **build info**
  (`/actuator/info`) — mismo puerto aislado 8081 que `health`.
- **Logs estructurados en JSON** (Elastic Common Schema —
  `logging.structured.format.console=ecs`), listos para Loki/ELK/CloudWatch sin
  librería adicional.
- **Stack local de Prometheus + Grafana** (perfil `monitoring`, no corre en un `up`
  normal):

```bash
docker compose -f docker/docker-compose.yaml --profile monitoring up
```

Prometheus (`http://localhost:9090`) scrapea `pricing-service-api:8081` cada 15s.
Grafana (`http://localhost:3000`, entrada anónima como Viewer) trae ya provisionados
el datasource y un dashboard (*Pricing Service API*) con 5 paneles: peticiones/seg,
latencia p95, errores 5xx, memoria heap de la JVM y conexiones activas de Hikari —
sin tocar nada a mano tras el `up`.

## Propuestas de mejora

- **Rate Limiting Escalable**: Incorporar un mecanismo de rate limiting (error http 429) para proteger los endpoints. Para poder ser escalable a múltiples réplicas se podría integrar un backend compartido (ej. Redis) o delegar la lógica de limitación a un proxy inverso o API Gateway en la capa de red (ej. Nginx).
