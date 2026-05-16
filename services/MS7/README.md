# Microservicio MS7 - Review (ms-review)

Microservicio Spring Boot para la gestión de reseñas y reputación de técnicos dentro de la arquitectura de microservicios 2026.

---

## Estado del proyecto

Actualmente incluye:

- API REST funcional (CRUD de reviews, cálculo de reputación)
- Seguridad con Spring Security + JWT (OAuth2 Resource Server)
- Persistencia con MySQL
- Comunicación inter-servicios con OpenFeign (actualización de ranking en ms-technician)
- Resiliencia con Circuit Breaker (Resilience4j)
- Mapper dedicado (ReviewMapper)
- Trazabilidad con CorrelationIdFilter (X-Trace-ID)
- Configuracion por perfiles (`dev`, `prod`)
- Contenerizacion con Docker
- Integracion operativa con **Config Server**, **Eureka** y **API Gateway**

---

## Stack tecnologico base 2026

- Java 17 | Spring Boot 3.5.x | Spring Cloud 2025.x | Maven 3.9+ | MySQL 8 | Docker | OpenFeign | Resilience4j | Flyway | Actuator | SpringDoc OpenAPI | Spring Security + OAuth2

---

## Puertos utilizados

| Servicio | Puerto |
|---|---:|
| MS-REVIEW | 8087 |
| MySQL DEV | 3317 |
| MySQL PROD | 3317 |

---

## Ejecucion DEV

```bash
# 1. Config Server + Registry Server (desde infra/)
# 2. MySQL dev
docker compose -f docker-compose-dev.yml up -d
# 3. App
mvn spring-boot:run
# 4. Swagger: http://localhost:8087/swagger-ui/index.html
```

---

## Ejecucion PROD

```bash
# 1. Infraestructura (desde infra/)
docker compose up -d
# 2. ms-review (desde services/MS7/)
docker compose up -d
```

---

## Configuracion externa (config-repo)

```text
infra/config-repo/ms-review-dev.yml
infra/config-repo/ms-review-prod.yml
```

---

## Estado de avance

- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Spring Security + JWT
- [x] Feign Client (MsTechnicianClient)
- [x] Circuit Breaker (Resilience4j)
- [x] CorrelationIdFilter
- [x] Mapper dedicado
- [ ] Caching de reputación
- [ ] Moderación de reseñas
