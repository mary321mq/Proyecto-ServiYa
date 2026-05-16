# Microservicio MS6 - Payment (ms-payment)

MS6 gestiona los pagos de ServiYa. Registra pagos asociados a solicitudes de servicio, calcula comision y neto, consulta MS4 para validar la solicitud y envia una notificacion a MS8 cuando el pago se registra.

---

## Estado actual

- API REST de pagos con operaciones `GET`, `HEAD`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`.
- Persistencia en MySQL con Spring Data JPA.
- Migraciones con Flyway.
- Configuracion externa con Config Server.
- Registro y descubrimiento con Eureka.
- Enrutamiento por API Gateway mediante `lb://MS-PAYMENT`.
- OpenFeign para llamadas a otros microservicios.
- Circuit Breaker con Resilience4j para llamadas Feign.
- Observabilidad con Actuator, Prometheus, Loki y logs locales.
- Documentacion OpenAPI con SpringDoc Swagger UI.
- Seguridad base con Spring Security + OAuth2 Resource Server.
- Trazabilidad por `CorrelationIdFilter` y encabezado `X-Trace-ID`.

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
| MS6 Payment | 8086 |
| MySQL Payment DEV | 3316 |
| MySQL Payment PROD | 3316 |
| Config Server DEV | 7071 |
| Eureka DEV | 7081 |
| Gateway DEV | 7091 |
| Prometheus DEV | 19090 |
| Grafana DEV | 13000 |
| Loki DEV | 13100 |

---

## Dependencias entre servicios

MS6 puede iniciar con su base de datos, Config Server y Eureka. Para algunas operaciones necesita otros microservicios:

| Dependencia | Uso | Comportamiento si falla |
|---|---|---|
| MS4 `ms-service-request` | Valida la solicitud antes de crear o actualizar pagos. | Circuit Breaker devuelve `503 SERVICE_UNAVAILABLE`. |
| MS8 `ms-notification` | Envia notificacion despues de registrar un pago. | El pago no se rompe; se omite la notificacion y se registra warning. |

Importante: `POST /api/v1/pagos` y `PUT /api/v1/pagos/{id}` validan contra MS4. Si solo esta levantado MS6, esas operaciones pueden responder `503`, y eso es esperado por resiliencia.

---

## Endpoints

Base path:

```text
/api/v1/pagos
```

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/v1/pagos` | Lista pagos. Permite filtros opcionales por `clienteId`, `tecnicoId` o `estado`. |
| POST | `/api/v1/pagos` | Crea un pago y calcula comision/neto. |
| GET | `/api/v1/pagos/{id}` | Obtiene un pago por ID. |
| HEAD | `/api/v1/pagos/{id}` | Verifica si existe un pago por ID. |
| PUT | `/api/v1/pagos/{id}` | Actualiza completamente un pago. |
| PATCH | `/api/v1/pagos/{id}/estado` | Actualiza solo el estado del pago. |
| DELETE | `/api/v1/pagos/{id}` | Elimina un pago. |
| GET | `/api/v1/pagos/solicitud/{solicitudId}` | Lista pagos por solicitud. |
| OPTIONS | `/api/v1/pagos` | Muestra metodos permitidos para la coleccion. |
| OPTIONS | `/api/v1/pagos/{id}` | Muestra metodos permitidos para un recurso. |

Ejemplos:

```text
GET http://localhost:7091/api/v1/pagos
GET http://localhost:7091/api/v1/pagos?estado=PAGADO
GET http://localhost:7091/api/v1/pagos/1
GET http://localhost:7091/api/v1/pagos/solicitud/1
```

---

## DTOs principales

Crear pago:

```json
{
  "solicitudId": 1,
  "clienteId": 1,
  "tecnicoId": 1,
  "monto": 100.00,
  "metodo": "YAPE"
}
```

Actualizar pago:

```json
{
  "solicitudId": 1,
  "clienteId": 1,
  "tecnicoId": 1,
  "monto": 120.00,
  "metodo": "TARJETA",
  "estado": "PAGADO"
}
```

Actualizar estado:

```json
{
  "estado": "ANULADO"
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

Fallbacks:

- `MsServiceRequestFallbackFactory`: protege llamadas a MS4.
- `MsNotificationFallbackFactory`: protege llamadas a MS8.

---

## Observabilidad

Actuator expone:

```text
http://localhost:8086/actuator/health
http://localhost:8086/actuator/prometheus
http://localhost:8086/actuator/metrics
```

Prometheus DEV scrapea:

```text
host.docker.internal:8086/actuator/prometheus
```

Logs:

```text
./logs/ms-payment.log
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
Config MS6:       http://localhost:7071/ms-payment/dev
Eureka:           http://localhost:7081/
Gateway:          http://localhost:7091/
Swagger MS6:      http://localhost:8086/swagger-ui/index.html
Swagger Gateway:  http://localhost:7091/swagger-ui/index.html?urls.primaryName=MS6%20Payment
OpenAPI MS6:      http://localhost:8086/v3/api-docs
OpenAPI Gateway:  http://localhost:7091/docs/ms6/v3/api-docs
Health MS6:       http://localhost:8086/actuator/health
Metrics MS6:      http://localhost:8086/actuator/prometheus
```

---

## Ejecucion en DEV

1. Levantar Config Server, Registry Server y Gateway desde `infra`.

2. Levantar MySQL de MS6:

```bash
cd services/MS6
docker compose -f docker-compose-dev.yml up -d
```

3. Levantar MS6:

```bash
cd services/MS6
mvn spring-boot:run
```

4. Abrir Swagger:

```text
http://localhost:8086/swagger-ui/index.html
```

O por Gateway:

```text
http://localhost:7091/swagger-ui/index.html?urls.primaryName=MS6%20Payment
```

---

## Ejecucion en PROD

1. Crear o revisar `.env` desde `.env.example`.

2. Levantar infraestructura:

```bash
cd infra
docker compose up -d
```

3. Levantar MS6:

```bash
cd services/MS6
docker compose up -d
```

Variables principales:

```text
SPRING_PROFILES_ACTIVE=prod
CONFIG_SERVER_URL=http://config-server:7071
PAYMENT_MYSQL_ROOT_PASSWORD=root
PAYMENT_MYSQL_DATABASE=db_payment
PAYMENT_DB_HOST=mysql-payment
PAYMENT_DB_PORT=3306
PAYMENT_DB_NAME=db_payment
PAYMENT_DB_USERNAME=root
PAYMENT_DB_PASSWORD=root
```

---

## Configuracion externa

Archivos en Config Server:

```text
infra/config-repo/ms-payment-dev.yml
infra/config-repo/ms-payment-prod.yml
```

Variables usadas por MS6:

| Variable | Descripcion | Default DEV |
|---|---|---|
| `CONFIG_SERVER_URL` | URL del Config Server. | `http://localhost:7071` |
| `EUREKA_URL` | URL de Eureka. | `http://localhost:7081/eureka` |
| `PAYMENT_DB_HOST` | Host de MySQL. | `localhost` |
| `PAYMENT_DB_PORT` | Puerto de MySQL. | `3316` |
| `PAYMENT_DB_NAME` | Base de datos. | `db_payment` |
| `PAYMENT_DB_USERNAME` | Usuario de base de datos. | `root` |
| `PAYMENT_DB_PASSWORD` | Password de base de datos. | `root` |
| `JWT_SECRET` | Secreto JWT para entorno local. | `dev-secret-...` |
| `SERVIYA_COMISION_PORCENTAJE` | Porcentaje de comision. | `0.10` |

---

## Verificacion rapida

Compilar:

```bash
mvn -DskipTests compile
```

Probar salud:

```bash
curl http://localhost:8086/actuator/health
```

Probar Swagger/OpenAPI:

```bash
curl http://localhost:8086/v3/api-docs
```

Probar por Gateway:

```bash
curl http://localhost:7091/docs/ms6/v3/api-docs
```

---

## Estado de avance

- [x] API REST de pagos
- [x] CRUD ampliado de pagos
- [x] Persistencia MySQL
- [x] Flyway
- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://MS-PAYMENT`
- [x] Spring Security + JWT
- [x] CorrelationIdFilter
- [x] Mapper dedicado
- [x] OpenFeign
- [x] Circuit Breaker con Resilience4j
- [x] Observabilidad con Actuator, Prometheus y Loki
- [ ] Integracion real con pasarela de pagos
