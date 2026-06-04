# Guia Completa de Seguridad - ServiYa

Este documento resume la seguridad implementada en el proyecto **ServiYa**.

La finalidad es explicar como funciona la autenticacion, los roles, el uso de JWT, la proteccion desde Gateway y las pruebas que se deben mostrar como evidencia.

---

## Objetivo

Poder demostrar que ServiYa tiene seguridad basada en:

- login con MS1 Auth
- generacion de token JWT
- validacion del token en Gateway
- roles de usuario
- rutas publicas
- rutas protegidas
- permisos por metodo HTTP
- pruebas con y sin token
- pruebas con ADMIN y CLIENTE

---

## Arquitectura de seguridad

Flujo general:

```text
Usuario -> Gateway -> MS1 Auth -> JWT
Usuario -> Gateway con JWT -> Microservicio protegido
```

Explicacion:

```text
MS1 Auth genera el token.
Gateway valida el token.
Gateway revisa el rol.
Gateway permite o bloquea la peticion.
```

El punto principal de proteccion es:

```text
infra/gateway/src/main/java/com/upeu/gateway/config/SecurityConfig.java
```

El servicio que emite tokens es:

```text
services/MS1/src/main/java/com/upeu/auth
```

---

## Servicios involucrados

| Servicio | Funcion en seguridad |
|---|---|
| Gateway | Valida JWT y aplica reglas por ruta |
| MS1 Auth | Login, registro y generacion de JWT |
| MS2 User | Gestion de usuarios/clientes |
| MS3 Technician | Endpoints de tecnicos con permisos por rol |
| MS4 Service Request | Solicitudes protegidas |
| MS5 Assignment | Asignaciones solo ADMIN |
| MS6 Payment | Pagos protegidos por CLIENTE/ADMIN |
| MS7 Review | Protegido por regla general |
| MS8 Notification | Protegido por regla general |

---

## Usuarios demo

MS1 Auth crea usuarios iniciales para pruebas.

### ADMIN

```text
email:    admin@serviya.local
password: admin123
rol:      ADMIN
```

### CLIENTE

```text
email:    cliente@serviya.local
password: cliente123
rol:      CLIENTE
```

### Uso esperado

```text
ADMIN = administra recursos sensibles
CLIENTE = consume o crea recursos permitidos para cliente
PUBLICO = accede solo a endpoints seguros sin token
```

---

## Claims del token JWT

Cuando el usuario inicia sesion, MS1 Auth genera un JWT con datos como:

```text
sub = email del usuario
uid = id del usuario
role = rol principal
roles = lista de roles
preferred_username = email del usuario
iss = issuer del token
exp = fecha de expiracion
iat = fecha de emision
```

Ejemplo conceptual:

```json
{
  "sub": "admin@serviya.local",
  "uid": 1,
  "role": "ADMIN",
  "roles": ["ADMIN"],
  "preferred_username": "admin@serviya.local",
  "iss": "serviya"
}
```

El Gateway lee el claim:

```text
roles
```

y lo convierte en authorities sin prefijo.

Por eso los permisos se escriben como:

```text
ADMIN
CLIENTE
USER
```

No como:

```text
ROLE_ADMIN
ROLE_CLIENTE
```

---

## Obtener token ADMIN

En PowerShell:

```powershell
$body = @{
  email = "admin@serviya.local"
  password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "http://localhost:7091/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body

$adminToken = $response.accessToken
$adminToken
```

Resultado esperado:

```text
Debe devolver un JWT largo.
```

---

## Obtener token CLIENTE

En PowerShell:

```powershell
$body = @{
  email = "cliente@serviya.local"
  password = "cliente123"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "http://localhost:7091/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body

$clienteToken = $response.accessToken
$clienteToken
```

Resultado esperado:

```text
Debe devolver un JWT largo con rol CLIENTE.
```

---

## Endpoints publicos

Estos endpoints pueden usarse sin token.

### Auth

```text
POST /auth/login
POST /auth/register
POST /api/v1/auth/login
POST /api/v1/auth/register
```

Motivo:

```text
El usuario necesita iniciar sesion o registrarse antes de tener token.
```

### Swagger y documentacion

```text
/swagger-ui.html
/swagger-ui/**
/v3/api-docs/**
/docs/**
```

Motivo:

```text
Permite revisar y probar la documentacion de APIs.
```

### Actuator permitido

```text
/actuator/health
/actuator/info
/actuator/prometheus
```

Motivo:

```text
Permite health checks y recoleccion de Prometheus.
```

Nota:

```text
/actuator/metrics del Gateway no esta publico.
Requiere token.
```

### MS3 Technician publico

```text
GET /api/v1/tecnicos/**
```

Motivo:

```text
Consultar tecnicos es una operacion publica o de busqueda segura.
```

---

## Matriz de permisos

| Servicio | Publico sin token | CLIENTE con token | ADMIN con token |
|---|---|---|---|
| MS1 Auth | login y register | /auth/me | /auth/me |
| MS3 Technician | GET /api/v1/tecnicos/** | consultar tecnicos | crear, editar, eliminar tecnicos |
| MS4 Service Request | no | GET y POST /api/v1/solicitudes/** | administrar solicitudes |
| MS5 Assignment | no | no | todo /api/v1/asignaciones/** |
| MS6 Payment | no | GET y POST /api/v1/pagos/** | administrar pagos |
| MS7 Review | no publico por regla general | requiere token | requiere token |
| MS8 Notification | no publico por regla general | requiere token | requiere token |

---

## Mapa claro de permisos por accion

### Regla general

```text
Sin token = usuario publico o anonimo
CLIENTE = usuario autenticado con rol CLIENTE
ADMIN = usuario autenticado con rol ADMIN
```

Codigos importantes:

```text
401 = no hay token o token invalido
403 = token valido, pero rol sin permiso
200/201 = permitido y ejecutado
400/404/409 = paso seguridad, pero fallo validacion, datos o regla de negocio
```

---

### MS3 Technician

| Accion | Link o ruta | Sin token | CLIENTE | ADMIN |
|---|---|---|---|---|
| Ver tecnico por id | `GET /api/v1/tecnicos/{id}` | Si | Si | Si |
| Buscar tecnicos cercanos | `GET /api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5` | Si | Si | Si |
| Crear tecnico | `POST /api/v1/tecnicos` | No | No | Si |
| Editar tecnico | `PUT /api/v1/tecnicos/{id}` | No | No | Si |
| Actualizar parcialmente | `PATCH /api/v1/tecnicos/{id}` | No | No | Si |
| Eliminar tecnico | `DELETE /api/v1/tecnicos/{id}` | No | No | Si |

Interpretacion:

```text
Cualquier persona puede consultar tecnicos.
Solo ADMIN puede crear, editar o eliminar tecnicos.
```

Links utiles:

```text
http://localhost:7091/api/v1/tecnicos/1
http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

---

### MS4 Service Request

| Accion | Link o ruta | Sin token | CLIENTE | ADMIN |
|---|---|---|---|---|
| Ver solicitudes | `GET /api/v1/solicitudes/**` | No | Si | Si |
| Crear solicitud | `POST /api/v1/solicitudes/**` | No | Si | Si |
| Editar solicitud | `PUT /api/v1/solicitudes/**` | No | No | Si |
| Actualizar parcialmente | `PATCH /api/v1/solicitudes/**` | No | No | Si |
| Eliminar solicitud | `DELETE /api/v1/solicitudes/**` | No | No | Si |

Interpretacion:

```text
El cliente puede crear y consultar solicitudes.
Solo ADMIN puede administrar o modificar solicitudes.
```

Link util:

```text
http://localhost:7091/api/v1/solicitudes
```

---

### MS5 Assignment

| Accion | Link o ruta | Sin token | CLIENTE | ADMIN |
|---|---|---|---|---|
| Ver asignaciones | `GET /api/v1/asignaciones/**` | No | No | Si |
| Crear asignacion | `POST /api/v1/asignaciones/**` | No | No | Si |
| Editar asignacion | `PUT /api/v1/asignaciones/**` | No | No | Si |
| Actualizar parcialmente | `PATCH /api/v1/asignaciones/**` | No | No | Si |
| Eliminar asignacion | `DELETE /api/v1/asignaciones/**` | No | No | Si |

Interpretacion:

```text
Las asignaciones son administrativas.
Solo ADMIN puede acceder a MS5 Assignment.
```

Link util:

```text
http://localhost:7091/api/v1/asignaciones
```

---

### MS6 Payment

| Accion | Link o ruta | Sin token | CLIENTE | ADMIN |
|---|---|---|---|---|
| Ver pagos | `GET /api/v1/pagos/**` | No | Si | Si |
| Crear pago | `POST /api/v1/pagos/**` | No | Si | Si |
| Editar pago | `PUT /api/v1/pagos/**` | No | No | Si |
| Actualizar estado o datos | `PATCH /api/v1/pagos/**` | No | No | Si |
| Eliminar pago | `DELETE /api/v1/pagos/**` | No | No | Si |

Interpretacion:

```text
El cliente puede consultar y crear pagos.
Solo ADMIN puede modificar, cambiar estado o eliminar pagos.
```

Link util:

```text
http://localhost:7091/api/v1/pagos
```

---

## Resumen rapido de permisos

| Servicio | Publico sin token | CLIENTE con token | ADMIN con token |
|---|---|---|---|
| MS3 Technician | consultar tecnicos | consultar tecnicos | todo |
| MS4 Service Request | nada | ver y crear solicitudes | todo |
| MS5 Assignment | nada | nada | todo |
| MS6 Payment | nada | ver y crear pagos | todo |

---

## Reglas detalladas del Gateway

### MS3 Technician

Publico:

```text
GET /api/v1/tecnicos/**
```

Solo ADMIN:

```text
POST /api/v1/tecnicos/**
PUT /api/v1/tecnicos/**
PATCH /api/v1/tecnicos/**
DELETE /api/v1/tecnicos/**
```

Ejemplos:

```text
GET /api/v1/tecnicos/1
GET /api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

Nota:

```text
GET /api/v1/tecnicos puede devolver 405 si el microservicio no tiene listado general para esa ruta exacta.
Eso no es error de seguridad; es metodo no permitido para esa ruta.
```

---

### MS4 Service Request

CLIENTE, USER o ADMIN:

```text
GET /api/v1/solicitudes/**
POST /api/v1/solicitudes/**
```

Solo ADMIN:

```text
PUT /api/v1/solicitudes/**
PATCH /api/v1/solicitudes/**
DELETE /api/v1/solicitudes/**
```

Interpretacion:

```text
Un cliente puede crear y ver solicitudes.
Un administrador puede modificar, actualizar o eliminar solicitudes.
```

---

### MS5 Assignment

Solo ADMIN:

```text
/api/v1/asignaciones/**
```

Interpretacion:

```text
Las asignaciones son operacion administrativa.
Un CLIENTE no debe asignar tecnicos manualmente.
```

---

### MS6 Payment

CLIENTE, USER o ADMIN:

```text
GET /api/v1/pagos/**
POST /api/v1/pagos/**
```

Solo ADMIN:

```text
PUT /api/v1/pagos/**
PATCH /api/v1/pagos/**
DELETE /api/v1/pagos/**
```

Interpretacion:

```text
Un cliente puede crear un pago y consultar pagos permitidos.
Un administrador puede cambiar, actualizar o eliminar pagos.
```

---

### Resto de rutas

Todo lo que no este declarado como publico o con regla especifica cae en:

```text
authenticated
```

Eso significa:

```text
requiere token valido
```

---

## Diferencia entre 401, 403, 404 y 405

| Codigo | Significado | Ejemplo |
|---:|---|---|
| 401 | No hay token o el token es invalido | abrir /api/v1/pagos sin token |
| 403 | Token valido, pero rol sin permiso | CLIENTE intentando entrar a asignaciones |
| 404 | Ruta existe, pero el recurso no existe | tecnico con id inexistente |
| 405 | Metodo HTTP no permitido | GET /api/v1/tecnicos si no existe listado general |
| 409 | Conflicto de negocio | crear pago duplicado |
| 500 | Error interno | revisar logs |
| 503 | Servicio no disponible | MS apagado o sin instancia |

---

## Pruebas principales de seguridad

### 1. Login ADMIN

```powershell
$body = @{
  email = "admin@serviya.local"
  password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "http://localhost:7091/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body

$adminToken = $response.accessToken
```

Evidencia esperada:

```text
Se obtiene accessToken.
```

---

### 2. Login CLIENTE

```powershell
$body = @{
  email = "cliente@serviya.local"
  password = "cliente123"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "http://localhost:7091/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body

$clienteToken = $response.accessToken
```

Evidencia esperada:

```text
Se obtiene accessToken con rol CLIENTE.
```

---

### 3. Endpoint protegido sin token

Abrir en navegador:

```text
http://localhost:7091/api/v1/pagos
```

Resultado esperado:

```text
401 Unauthorized
```

Interpretacion:

```text
El Gateway protege pagos y exige JWT.
```

---

### 4. MS6 Payment con CLIENTE

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Headers @{ Authorization = "Bearer $clienteToken" }
```

Resultado esperado:

```text
200 OK
lista de pagos o lista vacia
```

Interpretacion:

```text
CLIENTE puede consultar pagos.
```

---

### 5. MS6 Payment POST con CLIENTE

```powershell
$body = @{
  solicitudId = 1
  clienteId = 1
  tecnicoId = 1
  monto = 120
  metodo = "YAPE"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $clienteToken" } `
  -Body $body
```

Resultado esperado:

```text
200, 201 o error de negocio como 409.
```

Nota:

```text
Si devuelve 409, significa que paso seguridad, pero fallo una regla de negocio.
```

---

### 6. MS5 Assignment con CLIENTE

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/asignaciones" `
  -Headers @{ Authorization = "Bearer $clienteToken" }
```

Resultado esperado:

```text
403 Forbidden
```

Interpretacion:

```text
El token es valido, pero CLIENTE no tiene permiso para asignaciones.
```

---

### 7. MS5 Assignment con ADMIN

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/asignaciones" `
  -Headers @{ Authorization = "Bearer $adminToken" }
```

Resultado esperado:

```text
200 OK
lista de asignaciones o lista vacia
```

Interpretacion:

```text
ADMIN si puede acceder a asignaciones.
```

---

### 8. MS3 Technician publico

Abrir:

```text
http://localhost:7091/api/v1/tecnicos/1
```

Resultado posible:

```text
200 si existe el tecnico
404 si no existe
```

Interpretacion:

```text
GET de tecnicos es publico y no requiere token.
```

---

### 9. MS3 Technician crear sin token

Intentar POST sin token desde Swagger:

```text
POST /api/v1/tecnicos
```

Resultado esperado:

```text
401 Unauthorized
```

---

### 10. MS3 Technician crear con CLIENTE

POST con token CLIENTE:

```text
POST /api/v1/tecnicos
```

Resultado esperado:

```text
403 Forbidden
```

Interpretacion:

```text
CLIENTE esta autenticado, pero no puede crear tecnicos.
```

---

### 11. MS3 Technician crear con ADMIN

POST con token ADMIN:

```text
POST /api/v1/tecnicos
```

Resultado esperado:

```text
200, 201 o error de validacion de datos.
```

Interpretacion:

```text
Si no devuelve 401 ni 403, la seguridad permitio la operacion para ADMIN.
```

---

## Uso en Swagger

Abrir:

```text
http://localhost:7091/swagger-ui/index.html
```

Seleccionar definicion:

```text
MS3 Technician
MS4 Service Request
MS5 Assignment
MS6 Payment
```

### Autorizar con token

Presionar:

```text
Authorize
```

Pegar solo el token, sin escribir `Bearer`.

Correcto:

```text
eyJhbGciOi...
```

Incorrecto:

```text
Bearer eyJhbGciOi...
```

Swagger agrega `Bearer` automaticamente.

Si se pega con `Bearer`, la peticion puede salir como:

```text
Authorization: Bearer Bearer eyJ...
```

y fallar.

---

## Pruebas en Swagger paso a paso

La seguridad debe probarse con tres estados:

```text
1. Sin token
2. Token CLIENTE
3. Token ADMIN
```

Para cambiar de usuario en Swagger:

```text
Authorize -> Logout -> pegar nuevo token -> Authorize -> Close
```

Recordatorio:

```text
Pegar solo el token, sin Bearer.
```

---

### MS3 Technician en Swagger

Regla:

```text
Publico sin token: GET /api/v1/tecnicos/**
CLIENTE: consultar tecnicos
ADMIN: crear, editar y eliminar tecnicos
```

#### Prueba 1: sin token

1. Click en `Authorize`.
2. Click en `Logout`.
3. Seleccionar `MS3 Technician`.
4. Ejecutar:

```text
GET /api/v1/tecnicos/{id}
```

Poner:

```text
id = 1
```

Resultado esperado:

```text
200 o 404
```

Lo importante:

```text
No debe salir 401.
```

#### Prueba 2: CLIENTE con token

Ejecutar el mismo:

```text
GET /api/v1/tecnicos/{id}
```

Resultado esperado:

```text
200 o 404
```

#### Prueba 3: CLIENTE intentando crear tecnico

Ejecutar:

```text
POST /api/v1/tecnicos
```

Resultado esperado:

```text
403 Forbidden
```

#### Prueba 4: ADMIN creando, editando o eliminando

Con token ADMIN probar:

```text
POST /api/v1/tecnicos
PUT /api/v1/tecnicos/{id}
DELETE /api/v1/tecnicos/{id}
```

Resultado esperado:

```text
No debe ser 401 ni 403.
Puede salir 400, 404, 200 o 201 segun el cuerpo enviado o si existe el id.
```

---

### MS4 Service Request en Swagger

Regla:

```text
Publico sin token: no
CLIENTE: GET y POST /api/v1/solicitudes/**
ADMIN: administrar solicitudes
```

#### Prueba 1: sin token

Ejecutar:

```text
GET /api/v1/solicitudes
```

Resultado esperado:

```text
401 Unauthorized
```

#### Prueba 2: CLIENTE con token

Ejecutar:

```text
GET /api/v1/solicitudes
POST /api/v1/solicitudes
```

Resultado esperado:

```text
GET debe responder 200.
POST debe responder 200/201 o 400 si faltan datos, pero no 401 ni 403.
```

#### Prueba 3: CLIENTE intentando administrar

Ejecutar:

```text
PUT /api/v1/solicitudes/{id}
DELETE /api/v1/solicitudes/{id}
```

Resultado esperado:

```text
403 Forbidden
```

#### Prueba 4: ADMIN

Con token ADMIN:

```text
PUT /api/v1/solicitudes/{id}
PATCH /api/v1/solicitudes/{id}
DELETE /api/v1/solicitudes/{id}
```

Resultado esperado:

```text
No debe ser 401 ni 403.
Puede salir 200, 204, 400 o 404 segun datos.
```

---

### MS5 Assignment en Swagger

Regla:

```text
Publico sin token: no
CLIENTE con token: no
ADMIN con token: todo /api/v1/asignaciones/**
```

#### Prueba 1: sin token

Ejecutar:

```text
GET /api/v1/asignaciones
```

Resultado esperado:

```text
401 Unauthorized
```

#### Prueba 2: CLIENTE con token

Ejecutar:

```text
GET /api/v1/asignaciones
```

Resultado esperado:

```text
403 Forbidden
```

#### Prueba 3: ADMIN con token

Ejecutar:

```text
GET /api/v1/asignaciones
```

Resultado esperado:

```text
200 OK
```

---

### MS6 Payment en Swagger

Regla:

```text
Publico sin token: no
CLIENTE: GET y POST /api/v1/pagos/**
ADMIN: administrar pagos
```

#### Prueba 1: sin token

Ejecutar:

```text
GET /api/v1/pagos
```

Resultado esperado:

```text
401 Unauthorized
```

#### Prueba 2: CLIENTE con token

Ejecutar:

```text
GET /api/v1/pagos
POST /api/v1/pagos
```

Resultado esperado:

```text
GET debe responder 200.
POST puede responder 200/201 o 409 si el pago ya existe.
Si devuelve 409, igual demuestra que paso seguridad.
```

#### Prueba 3: CLIENTE intentando eliminar

Ejecutar:

```text
DELETE /api/v1/pagos/{id}
```

Poner:

```text
id = 1
```

Resultado esperado:

```text
403 Forbidden
```

#### Prueba 4: ADMIN

Con token ADMIN ejecutar:

```text
DELETE /api/v1/pagos/{id}
PUT /api/v1/pagos/{id}
PATCH /api/v1/pagos/{id}/estado
```

Resultado esperado:

```text
No debe ser 401 ni 403.
Puede salir 200, 204, 400 o 404 segun exista el pago o segun los datos enviados.
```

---

## Pruebas recomendadas en Swagger

### Sin token

1. Click en `Authorize`.
2. Si hay token, click en `Logout`.
3. Probar endpoint protegido.

Resultado esperado:

```text
401 Unauthorized
```

### Con CLIENTE

1. Obtener token CLIENTE.
2. Pegar token en `Authorize`.
3. Probar:

```text
GET /api/v1/pagos
POST /api/v1/pagos
GET /api/v1/solicitudes
POST /api/v1/solicitudes
```

Resultado esperado:

```text
Permitido
```

Probar:

```text
GET /api/v1/asignaciones
```

Resultado esperado:

```text
403 Forbidden
```

### Con ADMIN

1. Obtener token ADMIN.
2. Pegar token en `Authorize`.
3. Probar endpoints administrativos.

Resultado esperado:

```text
Permitido o error de validacion/negocio, pero no 401/403.
```

---

## Seguridad en Actuator

### Publico

```text
/actuator/health
/actuator/info
/actuator/prometheus
```

### Protegido en Gateway

```text
/actuator/metrics
/actuator/metrics/http.server.requests
/actuator/metrics/jvm.memory.used
/actuator/metrics/system.cpu.usage
```

Si se abre en navegador:

```text
401 Unauthorized
```

Consultar con token ADMIN:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/actuator/metrics" `
  -Headers @{ Authorization = "Bearer $adminToken" }
```

Interpretacion:

```text
Las metricas del Gateway existen, pero estan protegidas.
```

---

## Diferencia entre seguridad del Gateway y de los microservicios

En ServiYa, el Gateway es el punto principal de entrada.

Por eso las pruebas oficiales deben hacerse por:

```text
http://localhost:7091
```

No directamente por:

```text
http://localhost:8083
http://localhost:8084
http://localhost:8085
http://localhost:8086
```

Motivo:

```text
El Gateway aplica las reglas de permisos por ruta y rol.
Si se entra directo al puerto del microservicio, se puede estar saltando parte del flujo real.
```

Para exposicion, usar:

```text
Cliente -> Gateway -> Microservicio
```

---

## Seguridad en DEV

En DEV se usa:

```text
Gateway: http://localhost:7091
MS1 Auth: http://localhost:8081
issuer esperado: serviya
```

MS1 genera token con issuer:

```text
serviya
```

Gateway valida que el token venga del issuer esperado.

Si el issuer no coincide, el token puede fallar con:

```text
401 Unauthorized
```

---

## Nota pendiente para PROD

En PROD se debe revisar cuidadosamente:

```text
gateway-prod.yml
ms-auth-prod.yml
JWT_ISSUER
JWT_SECRET
```

El issuer debe ser consistente entre:

```text
MS1 Auth, que emite el token
Gateway, que valida el token
```

Regla:

```text
Si MS1 emite issuer=serviya, Gateway debe validar issuer=serviya.
```

Tambien se debe asegurar que:

```text
JWT_SECRET sea el mismo para MS1 y Gateway
```

Si secret o issuer no coinciden:

```text
el login puede generar token, pero el Gateway lo rechazara.
```

---

## Checklist de evidencia

Para demostrar seguridad completa, presentar:

- login ADMIN exitoso
- login CLIENTE exitoso
- token mostrado en PowerShell
- endpoint protegido sin token devuelve `401`
- endpoint permitido con CLIENTE devuelve `200`
- endpoint de ADMIN probado con CLIENTE devuelve `403`
- endpoint de ADMIN probado con ADMIN devuelve `200` o error de negocio
- MS3 GET publico sin token
- MS3 POST sin token devuelve `401`
- MS3 POST con CLIENTE devuelve `403`
- MS3 POST con ADMIN pasa seguridad
- Swagger usando Authorize correctamente
- `/actuator/metrics` del Gateway protegido
- `/actuator/metrics` consultado con token ADMIN

---

## Frase de cierre para exposicion

```text
En ServiYa la seguridad esta centralizada principalmente en el Gateway.
MS1 Auth genera tokens JWT con roles, y el Gateway valida esos tokens para permitir o bloquear rutas.
Existen endpoints publicos como login, register, Swagger, health, prometheus y consultas GET de tecnicos.
Las rutas sensibles requieren token y algunas requieren rol ADMIN.
Esto se valida con pruebas de 401, 403 y 200 usando usuarios ADMIN y CLIENTE.
```
