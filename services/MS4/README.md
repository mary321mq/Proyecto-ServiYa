# Microservicio MS4 - Service Request (ms-service-request)

Microservicio Spring Boot para la gestión de solicitudes de servicio dentro de la arquitectura de microservicios 2026.

---

## Estado del proyecto

Actualmente incluye:

- API REST funcional (CRUD de solicitudes, actualización de estado)
- Seguridad con Spring Security + JWT (OAuth2 Resource Server)
- Persistencia con MySQL
- Configuracion por perfiles (`dev`, `prod`)
- Contenerizacion con Docker
- Integracion operativa con **Config Server**
- Integracion operativa con **Registry Server (Eureka)**
- Integracion operativa con **API Gateway**
- Enrutamiento dinamico con **`lb://ms-service-request`**

---

## Arquitectura (estado actual)

```text
Client -> API Gateway -> Microservicios -> Registry Server -> Config Server
```

Este repositorio implementa unicamente el microservicio **ms-service-request**.

---

## Stack tecnologico base 2026

- Java 17
- Spring Boot 3.5.x
- Spring Cloud 2025.x
- Maven 3.9+
- MySQL 8
- Docker
- Docker Compose
- Spring Cloud Config Client
- Eureka Client
- Flyway
- Actuator
- SpringDoc OpenAPI
- Spring Security + OAuth2 Resource Server

---

## Puertos utilizados

| Servicio | Puerto expuesto |
|---|---:|
| MS-SERVICE-REQUEST DEV | 8085 |
| MS-SERVICE-REQUEST PROD | 8085 |
| MySQL DEV | 3314 |
| MySQL PROD | 3314 |
| Config Server DEV | 7071 |
| Config Server PROD | 7072 |
| Registry Server DEV | 8761 |
| Registry Server PROD | 8762 |
| Gateway DEV | 9090 |
| Gateway PROD | 9091 |

---

## DEV vs PROD

| Modo | Ejecucion app | Base de datos | Configuracion | Registro | Puerto app |
|---|---|---|---|---|---:|
| DEV | `mvn spring-boot:run` | Docker/local | Config Server DEV | Registry DEV | 8085 |
| PROD | Docker | Docker | Config Server PROD | Registry PROD | 8085 |

---

# Ejecucion DEV con Config + Registry

## Objetivo

Ejecutar `ms-service-request` en modo desarrollo consumiendo configuracion externa y registrando la instancia en Eureka.

---

## 1. Levantar Config Server (DEV)

Desde `infra/config-server`:

```bash
mvn spring-boot:run
```

Prueba:

```text
http://localhost:7071/ms-service-request/dev
```

---

## 2. Levantar Registry Server (DEV)

Desde `infra/registry-server`:

```bash
mvn spring-boot:run
```

Dashboard:

```text
http://localhost:8761
```

---

## 3. Levantar MySQL de desarrollo

Desde `services/MS4`:

```bash
docker compose -f docker-compose-dev.yml up -d
```

---

## 4. Ejecutar ms-service-request en DEV

Desde `services/MS4`:

```bash
mvn spring-boot:run
```

---

## 5. Probar

Swagger UI:

```text
http://localhost:8085/swagger-ui/index.html
```

Registro en Eureka:

```text
http://localhost:8761
```

---

# Ejecucion PROD con Config + Registry

## Objetivo

Ejecutar `ms-service-request` en contenedor Docker consumiendo configuracion externa y registro de servicio en Eureka.

---

## 1. Levantar infraestructura (config + registry)

Desde `infra`:

```bash
docker compose up -d
```

Pruebas:

```text
http://localhost:7072/ms-service-request/prod
http://localhost:8762
```

---

## 2. Archivo `.env` (modo PROD)

En `services/MS4/.env`:

```env
SERVICE_REQUEST_MYSQL_ROOT_PASSWORD=root
SERVICE_REQUEST_MYSQL_DATABASE=db_service_request

SPRING_PROFILES_ACTIVE=prod

CONFIG_SERVER_URL=http://config-server:7071

SERVICE_REQUEST_DB_HOST=mysql-service-request
SERVICE_REQUEST_DB_PORT=3306
SERVICE_REQUEST_DB_NAME=db_service_request
SERVICE_REQUEST_DB_USERNAME=root
SERVICE_REQUEST_DB_PASSWORD=root
```

---

## 3. Redes utilizadas

- `ms-net` -> red comun de infraestructura (config-server, registry-server, gateway, microservicios)
- `service-request-int` -> red interna de ms-service-request (mysql + app)

---

## 4. Levantar ms-service-request en modo productivo

Desde `services/MS4`:

```bash
docker compose up -d
```

---

## 5. Probar

Eureka PROD (host):

```text
http://localhost:8762
```

---

# Configuracion externa (config-repo)

Archivos esperados:

```text
infra/config-repo/ms-service-request-dev.yml
infra/config-repo/ms-service-request-prod.yml
```

---

# Estado de avance

- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://ms-service-request`
- [x] Spring Security + JWT
- [ ] Feign
- [ ] Circuit Breaker
- [ ] Notificaciones en tiempo real

---

# Tag sugerido

```bash
git tag -a vs04-gateway-lb -m "ms-service-request integrado con API Gateway y enrutamiento lb://ms-service-request"
git push origin vs04-gateway-lb
```
