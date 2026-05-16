# Microservicio MS3 - Technician (ms-technician)

MS3 gestiona los tecnicos de ServiYa. Permite registrar tecnicos, actualizar sus datos, ubicacion y ranking, y buscar tecnicos cercanos a una solicitud consumiendo informacion de MS4 (`ms-service-request`).

---

## Estado actual

- API REST de tecnicos con operaciones `GET`, `POST`, `PUT`, `DELETE` y endpoints funcionales de ubicacion, ranking y cercania.
- Persistencia en MySQL con Spring Data JPA.
- Migraciones con Flyway.
- Configuracion externa con Config Server.
- Registro y descubrimiento con Eureka.
- Enrutamiento por API Gateway mediante `lb://MS-TECHNICIAN`.
- OpenFeign para llamadas a otros microservicios.
- Circuit Breaker con Resilience4j para llamadas Feign.
- Observabilidad con Actuator, Prometheus, Loki y logs locales.
- Documentacion OpenAPI con SpringDoc Swagger UI.
- Seguridad base con Spring Security + OAuth2 Resource Server.
- Trazabilidad por `CorrelationIdFilter` y encabezado `X-Correlation-Id`.

---

## Stack

- Java 17
- Spring Boot 3.5.x
- Spring Cloud 2025.x
- Maven
- MySQL 8.4
- Docker / Docker Compose
- Flyway
- OpenFeign
- Resilience4j Circuit Breaker
- Actuator + Micrometer Prometheus
- Loki4j
- SpringDoc OpenAPI

---

## Puertos

| Componente | Puerto |
|---|---:|
| MS3 Technician | 8083 |
| MySQL Technician DEV | 3313 |
| MySQL Technician PROD | 3313 |
| Config Server DEV | 7071 |
| Eureka DEV | 7081 |
| Gateway DEV | 7091 |
| Prometheus DEV | 19090 |
| Grafana DEV | 13000 |
| Loki DEV | 13100 |

---

## Dependencias entre servicios

MS3 puede iniciar con su base de datos, Config Server y Eureka. Para la busqueda de tecnicos cercanos por solicitud necesita a MS4:

| Dependencia | Uso | Comportamiento si falla |
|---|---|---|
| MS4 `ms-service-request` | Obtiene la ubicacion de la solicitud para calcular cercania. | Circuit Breaker activa el fallback y la busqueda devuelve lista vacia si no hay datos de solicitud. |

Importante: `GET /api/v1/tecnicos/cercanos` depende de MS4. Si solo esta levantado MS3, ese endpoint puede responder vacio, y eso es esperado por resiliencia.

---

## Endpoints

Base path:

```text
/api/v1/tecnicos
```

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/api/v1/tecnicos` | Registra un tecnico. |
| GET | `/api/v1/tecnicos/{id}` | Obtiene un tecnico por ID. |
| PUT | `/api/v1/tecnicos/{id}` | Actualiza datos generales del tecnico. |
| DELETE | `/api/v1/tecnicos/{id}` | Elimina un tecnico. |
| PUT | `/api/v1/tecnicos/{id}/ubicacion` | Actualiza latitud y longitud del tecnico. |
| PUT | `/api/v1/tecnicos/{id}/ranking` | Actualiza ranking del tecnico. |
| GET | `/api/v1/tecnicos/cercanos?solicitudId={id}&radioKm={km}` | Devuelve tecnicos cercanos segun la ubicacion de una solicitud de MS4. |

Ejemplos:

```text
GET http://localhost:7091/api/v1/tecnicos/1
GET http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1
GET http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

---

## DTOs principales

Crear tecnico:

```json
{
  "nombre": "Juan Perez",
  "email": "juan@serviya.com",
  "telefono": "999888777"
}
```

Actualizar tecnico:

```json
{
  "nombre": "Juan Perez Actualizado",
  "telefono": "999111222",
  "activo": true
}
```

Actualizar ubicacion:

```json
{
  "lat": -15.499,
  "lng": -70.133
}
```

Actualizar ranking:

```json
{
  "ranking": 4.8
}
```

---

## Circuit Breaker

El Circuit Breaker esta habilitado para OpenFeign:

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

Configuracion principal:

- Ventana de llamadas: `10`
- Minimo de llamadas: `5`
- Umbral de fallos: `50%`
- Tiempo abierto: `10s`
- Half-open: `3` llamadas permitidas
- Timeout: `3s`

Fallback:

- `MsServiceRequestFallbackFactory`: protege llamadas a MS4.

---

## Observabilidad

Actuator expone:

```text
http://localhost:8083/actuator/health
http://localhost:8083/actuator/prometheus
http://localhost:8083/actuator/metrics
```

Prometheus DEV scrapea:

```text
host.docker.internal:8083/actuator/prometheus
```

Logs:

```text
./logs/ms-technician.log
```

Herramientas DEV:

```text
Prometheus: http://localhost:19090
Grafana:    http://localhost:13000
Loki:       http://localhost:13100/ready
```

Grafana:

```text
Usuario: admin
Clave: admin
```

---

## Enlaces utiles en DEV

```text
Config MS3:       http://localhost:7071/ms-technician/dev
Eureka:           http://localhost:7081/
Gateway:          http://localhost:7091/
Swagger MS3:      http://localhost:8083/swagger-ui/index.html
Swagger Gateway:  http://localhost:7091/swagger-ui/index.html?urls.primaryName=MS3%20Technician
OpenAPI MS3:      http://localhost:8083/v3/api-docs
OpenAPI Gateway:  http://localhost:7091/docs/ms3/v3/api-docs
Health MS3:       http://localhost:8083/actuator/health
Metrics MS3:      http://localhost:8083/actuator/prometheus
```

---

## Ejecucion en DEV

1. Levantar Config Server, Registry Server y Gateway desde `infra`.

2. Levantar MySQL de MS3:

```bash
cd services/MS3
docker compose -f docker-compose-dev.yml up -d
```

3. Levantar MS3:

```bash
cd services/MS3
mvn spring-boot:run
```

4. Abrir Swagger:

```text
http://localhost:8083/swagger-ui/index.html
```

O por Gateway:

```text
http://localhost:7091/swagger-ui/index.html?urls.primaryName=MS3%20Technician
```

---

## Ejecucion en PROD

1. Crear o revisar `.env` desde `.env.example`.

2. Levantar infraestructura:

```bash
cd infra
docker compose up -d
```

3. Levantar MS3:

```bash
cd services/MS3
docker compose up -d
```

Variables principales:

```text
SPRING_PROFILES_ACTIVE=prod
CONFIG_SERVER_URL=http://config-server:7072
TECHNICIAN_MYSQL_ROOT_PASSWORD=root
TECHNICIAN_MYSQL_DATABASE=db_technician
TECHNICIAN_DB_HOST=mysql-technician
TECHNICIAN_DB_PORT=3306
TECHNICIAN_DB_NAME=db_technician
TECHNICIAN_DB_USERNAME=root
TECHNICIAN_DB_PASSWORD=root
```

---

## Configuracion externa

Archivos en Config Server:

```text
infra/config-repo/ms-technician-dev.yml
infra/config-repo/ms-technician-prod.yml
```

Variables usadas por MS3:

| Variable | Descripcion | Default DEV |
|---|---|---|
| `CONFIG_SERVER_URL` | URL del Config Server. | `http://localhost:7071` |
| `EUREKA_URL` | URL de Eureka. | `http://localhost:7081/eureka` |
| `TECHNICIAN_DB_HOST` | Host de MySQL. | `localhost` |
| `TECHNICIAN_DB_PORT` | Puerto de MySQL. | `3313` |
| `TECHNICIAN_DB_NAME` | Base de datos. | `db_technician` |
| `TECHNICIAN_DB_USERNAME` | Usuario de base de datos. | `root` |
| `TECHNICIAN_DB_PASSWORD` | Password de base de datos. | `root` |
| `JWT_SECRET` | Secreto JWT para entorno local. | `dev-secret-...` |

---

## Verificacion rapida

Compilar:

```bash
mvn -DskipTests compile
```

Probar salud:

```bash
curl http://localhost:8083/actuator/health
```

Probar Swagger/OpenAPI:

```bash
curl http://localhost:8083/v3/api-docs
```

Probar por Gateway:

```bash
curl http://localhost:7091/docs/ms3/v3/api-docs
```

---

## Estado de avance

- [x] API REST de tecnicos
- [x] Persistencia MySQL
- [x] Flyway
- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://MS-TECHNICIAN`
- [x] Spring Security + JWT
- [x] CorrelationIdFilter
- [x] OpenFeign
- [x] Circuit Breaker con Resilience4j
- [x] Observabilidad con Actuator, Prometheus y Loki
- [x] Busqueda de tecnicos cercanos por solicitud
- [ ] Integracion con mas microservicios
