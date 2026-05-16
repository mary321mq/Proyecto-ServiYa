# Microservicio MS1 - Auth (ms-auth)

Microservicio Spring Boot para la autenticación y gestión de credenciales dentro de la arquitectura de microservicios 2026.

---

## Estado del proyecto

Actualmente incluye:

- API REST funcional (register, login, me)
- Seguridad con Spring Security + JWT (HMAC-SHA256)
- Persistencia con MySQL
- Configuracion por perfiles (`dev`, `prod`)
- Contenerizacion con Docker
- Integracion operativa con **Config Server**
- Integracion operativa con **Registry Server (Eureka)**
- Integracion operativa con **API Gateway**
- Enrutamiento dinamico con **`lb://ms-auth`**

---

## Arquitectura (estado actual)

```text
Client -> API Gateway -> Microservicios -> Registry Server -> Config Server
```

Este repositorio implementa unicamente el microservicio **ms-auth**.

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
- JWT (Nimbus JOSE)

---

## Puertos utilizados

| Servicio | Puerto expuesto |
|---|---:|
| MS-AUTH DEV | 8081 |
| MS-AUTH PROD | 8081 |
| MySQL DEV | 3310 |
| MySQL PROD | 3311 |
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
| DEV | `mvn spring-boot:run` | Docker/local | Config Server DEV | Registry DEV | 8081 |
| PROD | Docker | Docker | Config Server PROD | Registry PROD | 8081 |

---

# Ejecucion DEV con Config + Registry

## Objetivo

Ejecutar `ms-auth` en modo desarrollo consumiendo configuracion externa y registrando la instancia en Eureka.

---

## 1. Levantar Config Server (DEV)

Desde `infra/config-server`:

```bash
mvn spring-boot:run
```

Prueba:

```text
http://localhost:7071/ms-auth/dev
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

Desde `services/MS1`:

```bash
docker compose -f docker-compose-dev.yml up -d
```

---

## 4. Ejecutar ms-auth en DEV

Desde `services/MS1`:

```bash
mvn spring-boot:run
```

---

## 5. Probar

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

Registro en Eureka:

```text
http://localhost:8761
```

### Endpoints principales

#### Registro
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@mail.com","password":"123456","nombre":"Test User"}'
```

#### Login
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@mail.com","password":"123456"}'
```

#### Perfil (requiere JWT)
```bash
curl http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

---

# Ejecucion PROD con Config + Registry

## Objetivo

Ejecutar `ms-auth` en contenedor Docker consumiendo configuracion externa y registro de servicio en Eureka.

---

## 1. Levantar infraestructura (config + registry)

Desde `infra`:

```bash
docker compose up -d
```

Pruebas:

```text
http://localhost:7072/ms-auth/prod
http://localhost:8762
```

---

## 2. Archivo `.env` (modo PROD)

En `services/MS1/.env`:

```env
AUTH_MYSQL_ROOT_PASSWORD=root
AUTH_MYSQL_DATABASE=db_auth

SPRING_PROFILES_ACTIVE=prod

CONFIG_SERVER_URL=http://config-server:7071

AUTH_DB_HOST=mysql-auth
AUTH_DB_PORT=3306
AUTH_DB_NAME=db_auth
AUTH_DB_USERNAME=root
AUTH_DB_PASSWORD=root
```

---

## 3. Redes utilizadas

- `ms-net` -> red comun de infraestructura (config-server, registry-server, gateway, microservicios)
- `auth-int` -> red interna de ms-auth (mysql + app)

---

## 4. Levantar ms-auth en modo productivo

Desde `services/MS1`:

```bash
docker compose up -d
```

---

## 5. Probar

API:

```text
http://localhost:8081/api/v1/auth/login
```

Eureka PROD (host):

```text
http://localhost:8762
```

---

# Configuracion externa (config-repo)

Archivos esperados:

```text
infra/config-repo/ms-auth-dev.yml
infra/config-repo/ms-auth-prod.yml
```

Puntos clave ya configurados:

- `ms-auth-dev.yml` usa `eureka.client.service-url.defaultZone=http://localhost:8761/eureka`
- `ms-auth-prod.yml` usa `eureka.client.service-url.defaultZone=http://registry-server:8761/eureka`

---

# Problemas resueltos

- Config Server no accesible -> faltaba red `ms-net`
- Registry Server no accesible -> infraestructura no levantada completa
- MySQL no accesible -> faltaba red `auth-int`
- Error datasource -> configuracion externa no cargada

---

# Estado de avance

- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://ms-auth`
- [x] Spring Security + JWT
- [ ] Feign
- [ ] Circuit Breaker
- [ ] Rate Limiting
- [ ] Refresh Token

---

# Siguiente paso

Continuar con atributos de calidad sobre la base actual:

- implementar comunicacion entre microservicios con Feign
- agregar resiliencia con Circuit Breaker
- implementar refresh token y token rotation
- logging distribuido con Correlation ID

---

# Tag sugerido

```bash
git tag -a vs04-gateway-lb -m "ms-auth integrado con API Gateway y enrutamiento lb://ms-auth"
git push origin vs04-gateway-lb
```
