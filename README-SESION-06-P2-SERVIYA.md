# Sesion 06 P2 - Observabilidad Basica en ServiYa

Este documento adapta la guia del docente al proyecto **ServiYa**.

La finalidad de esta fase es validar observabilidad basica de forma manual, sin Prometheus, Loki ni Grafana todavia.

En esta etapa se trabaja con:

- Eureka
- Gateway
- Spring Boot Actuator
- Swagger
- JWT
- logs de consola
- archivos locales de logs
- pruebas directas por navegador y PowerShell

---

## Objetivo

Poder responder preguntas como:

- que microservicios estan levantados
- que servicios estan registrados en Eureka
- que servicio recibio una peticion
- que endpoint respondio correctamente
- que endpoint necesita token
- que metricas expone cada microservicio
- que pasa cuando un microservicio se detiene
- como se evidencia una falla usando health, Gateway, Eureka y logs

Flujo general de ServiYa:

```text
Cliente -> Gateway -> Microservicio
```

Ejemplos:

```text
Cliente -> Gateway -> MS3 Technician
Cliente -> Gateway -> MS4 Service Request
Cliente -> Gateway -> MS5 Assignment
Cliente -> Gateway -> MS6 Payment
```

---

## Microservicios del proyecto

| Servicio | Nombre en Eureka | Puerto DEV | Funcion principal |
|---|---:|---:|---|
| Gateway | GATEWAY | 7091 | Entrada principal y seguridad |
| MS1 Auth | MS-AUTH | 8081 | Login y JWT |
| MS2 User | MS-USER | 8082 | Usuarios/clientes |
| MS3 Technician | MS-TECHNICIAN | 8083 | Tecnicos |
| MS4 Service Request | MS-SERVICE-REQUEST | 8084 | Solicitudes |
| MS5 Assignment | MS-ASSIGNMENT | 8085 | Asignaciones |
| MS6 Payment | MS-PAYMENT | 8086 | Pagos |
| MS7 Review | MS-REVIEW | 8087 | Resenas/calificaciones |
| MS8 Notification | MS-NOTIFICATION | 8088 | Notificaciones |

---

## Evidencias esperadas

Al finalizar esta fase, se debe poder mostrar:

- Eureka con servicios `UP`
- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/metrics/http.server.requests`
- `GET /actuator/metrics/system.cpu.usage`
- `GET /actuator/metrics/jvm.memory.used`
- rutas reales por Gateway
- prueba sin token con respuesta `401`
- prueba con token con respuesta correcta
- logs de consola o archivos locales
- una falla controlada deteniendo un microservicio

---

## Paso 1. Levantar infraestructura base

Levantar primero:

1. Config Server
2. Registry Server / Eureka
3. Gateway

### Config Server

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\infra\config-server"
mvn spring-boot:run
```

Resultado esperado:

```text
Tomcat started on port 7071
```

### Registry Server / Eureka

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\infra\registry-server"
mvn spring-boot:run
```

Resultado esperado:

```text
Tomcat started on port 7081
```

### Gateway

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\infra\gateway"
mvn spring-boot:run
```

Resultado esperado:

```text
Netty started on port 7091
```

---

## Paso 2. Levantar bases de datos y microservicios

Cada microservicio debe tener primero su base de datos levantada.

Ejemplo para MS3:

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\services\MS3"
docker compose -f docker-compose-dev.yml up -d
mvn spring-boot:run
```

Repetir el mismo flujo para los demas MS:

```text
MS1 Auth
MS2 User
MS3 Technician
MS4 Service Request
MS5 Assignment
MS6 Payment
MS7 Review
MS8 Notification
```

---

## Paso 3. Verificar Eureka

Abrir:

```text
http://localhost:7081
```

Se espera ver:

```text
GATEWAY
MS-AUTH
MS-USER
MS-TECHNICIAN
MS-SERVICE-REQUEST
MS-ASSIGNMENT
MS-PAYMENT
MS-REVIEW
MS-NOTIFICATION
```

Estado esperado:

```text
UP
```

### Como explicarlo

```text
Eureka permite verificar que los microservicios se registraron correctamente.
El estado UP indica que la instancia esta disponible para ser descubierta por el Gateway.
```

---

## Paso 4. Verificar health

El endpoint `/actuator/health` permite comprobar si una aplicacion esta viva.

### Gateway

```text
http://localhost:7091/actuator/health
```

### Microservicios

```text
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
http://localhost:8085/actuator/health
http://localhost:8086/actuator/health
http://localhost:8087/actuator/health
http://localhost:8088/actuator/health
```

Resultado esperado:

```json
{
  "status": "UP"
}
```

Si aparece componente `db` en `UP`, tambien se confirma conexion a la base de datos.

### Como explicarlo

```text
El endpoint health confirma que el servicio esta activo.
Si muestra db en UP, tambien confirma que la conexion con la base de datos funciona.
```

---

## Paso 5. Verificar metrics

El endpoint `/actuator/metrics` lista las metricas disponibles.

En ServiYa no todos los servicios exponen Actuator igual.

### Servicios visibles desde navegador en DEV

```text
http://localhost:8082/actuator/metrics
http://localhost:8083/actuator/metrics
http://localhost:8084/actuator/metrics
http://localhost:8085/actuator/metrics
http://localhost:8086/actuator/metrics
http://localhost:8087/actuator/metrics
http://localhost:8088/actuator/metrics
```

### Servicios protegidos con JWT

En ServiYa, Gateway y MS1 Auth tienen `/actuator/metrics` protegido.

Si se abre desde navegador:

```text
http://localhost:7091/actuator/metrics
http://localhost:8081/actuator/metrics
```

Resultado esperado:

```text
401 Unauthorized
```

Esto no significa que no existan metricas. Significa que requieren token.

---

## Paso 6. Obtener token ADMIN

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

$token = $response.accessToken
$token
```

Resultado esperado:

```text
Debe devolver un JWT largo.
```

### Como explicarlo

```text
MS1 Auth genera el token JWT y el Gateway permite usarlo para acceder a rutas protegidas.
```

---

## Paso 7. Consultar metricas protegidas con token

### Gateway metrics

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/actuator/metrics" `
  -Headers @{ Authorization = "Bearer $token" } |
ConvertTo-Json -Depth 10
```

### Gateway http.server.requests

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/actuator/metrics/http.server.requests" `
  -Headers @{ Authorization = "Bearer $token" } |
ConvertTo-Json -Depth 10
```

### Gateway JVM memory

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/actuator/metrics/jvm.memory.used" `
  -Headers @{ Authorization = "Bearer $token" } |
ConvertTo-Json -Depth 10
```

### Gateway CPU

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/actuator/metrics/system.cpu.usage" `
  -Headers @{ Authorization = "Bearer $token" } |
ConvertTo-Json -Depth 10
```

### Como explicarlo

```text
En ServiYa, las metricas del Gateway estan protegidas.
Sin token devuelven 401.
Con token ADMIN responden correctamente.
```

---

## Paso 8. Metricas especificas por microservicio

Ejemplos para MS3:

```text
http://localhost:8083/actuator/metrics/http.server.requests
http://localhost:8083/actuator/metrics/jvm.memory.used
http://localhost:8083/actuator/metrics/system.cpu.usage
```

Ejemplos para MS6:

```text
http://localhost:8086/actuator/metrics/http.server.requests
http://localhost:8086/actuator/metrics/jvm.memory.used
http://localhost:8086/actuator/metrics/system.cpu.usage
```

### Que significan

```text
http.server.requests = peticiones HTTP recibidas
jvm.memory.used = memoria usada por la JVM
system.cpu.usage = uso de CPU
```

En `http.server.requests` se observan valores como:

```text
COUNT = cantidad de peticiones
TOTAL_TIME = tiempo total acumulado
MAX = peticion mas lenta observada
```

---

## Paso 9. Probar endpoints reales por Gateway

La puerta de entrada principal es:

```text
http://localhost:7091
```

### MS1 Auth

```text
POST http://localhost:7091/auth/login
```

Evidencia esperada:

```text
Token JWT generado.
```

### MS3 Technician

No usar:

```text
GET /api/v1/tecnicos
```

porque puede responder:

```text
405 Method Not Allowed
```

Eso ocurre porque esa ruta exacta no tiene GET general.

Usar:

```text
http://localhost:7091/api/v1/tecnicos/1
```

Resultado posible:

```text
200 si existe el tecnico
404 si no existe el tecnico
```

Tambien:

```text
http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

### MS4 Service Request

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/solicitudes" `
  -Headers @{ Authorization = "Bearer $token" }
```

### MS5 Assignment

Requiere token ADMIN:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/asignaciones" `
  -Headers @{ Authorization = "Bearer $token" }
```

### MS6 Payment

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Headers @{ Authorization = "Bearer $token" }
```

### MS7 Review

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/reviews" `
  -Headers @{ Authorization = "Bearer $token" }
```

### MS8 Notification

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/notificaciones" `
  -Headers @{ Authorization = "Bearer $token" }
```

---

## Paso 10. Validar seguridad

### Sin token

Abrir en navegador:

```text
http://localhost:7091/api/v1/pagos
```

Resultado esperado:

```text
401 Unauthorized
```

### Con token

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Headers @{ Authorization = "Bearer $token" }
```

Resultado esperado:

```text
200 OK, lista de pagos o lista vacia
```

### Interpretacion de errores

```text
401 = no se envio token o el token es invalido
403 = token valido, pero rol sin permiso
404 = ruta valida, pero recurso no encontrado
405 = metodo HTTP no permitido para esa ruta
500 = error interno, revisar logs
```

---

## Paso 11. Revisar logs locales

En esta sesion todavia no se usa Loki.

Los logs se revisan:

1. En la consola donde corre `mvn spring-boot:run`
2. En archivos locales de logs

Carpetas sugeridas:

```text
C:\CICLO 5\ServiYa\ServiYa\infra\gateway\logs
C:\CICLO 5\ServiYa\ServiYa\services\MS3\logs
C:\CICLO 5\ServiYa\ServiYa\services\MS4\logs
C:\CICLO 5\ServiYa\ServiYa\services\MS5\logs
C:\CICLO 5\ServiYa\ServiYa\services\MS6\logs
```

Ver ultimas lineas del Gateway:

```powershell
Get-Content "C:\CICLO 5\ServiYa\ServiYa\infra\gateway\logs\gateway.log" -Tail 80
```

Buscar:

```text
401
403
503
MS-TECHNICIAN
MS-PAYMENT
/api/v1/pagos
/api/v1/tecnicos
```

### Como explicarlo

```text
Los logs permiten revisar que ocurrio dentro del sistema.
Mientras las metricas muestran numeros, los logs muestran mensajes concretos de ejecucion o error.
```

---

## Paso 12. Falla controlada con MS3 Technician

El objetivo es comprobar que se puede detectar una caida.

### 12.1 Estado normal

Verificar health:

```text
http://localhost:8083/actuator/health
```

Resultado esperado:

```json
{
  "status": "UP"
}
```

Probar por Gateway:

```text
http://localhost:7091/api/v1/tecnicos/1
```

Resultado posible:

```text
200 si existe
404 si no existe
```

Lo importante es que no sea `503`.

### 12.2 Detener MS3

En la terminal donde corre MS3:

```text
Ctrl + C
```

No detener MySQL. Solo detener la aplicacion MS3.

### 12.3 Ver health despues de detener

Abrir:

```text
http://localhost:8083/actuator/health
```

Resultado esperado:

```text
Connection refused
```

o pagina indicando que no se puede acceder.

### 12.4 Probar Gateway despues de detener MS3

```text
http://localhost:7091/api/v1/tecnicos/1
```

Resultado esperado:

```text
503 Service Unavailable
```

o error equivalente del Gateway.

### 12.5 Revisar Eureka

Abrir:

```text
http://localhost:7081
```

Puede tardar unos segundos en actualizar.

### Como explicarlo

```text
Eureka no elimina inmediatamente la instancia.
Primero espera a que el microservicio deje de renovar su registro.
Por eso el cambio puede tardar unos segundos.
```

### 12.6 Levantar MS3 nuevamente

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\services\MS3"
mvn spring-boot:run
```

Verificar:

```text
http://localhost:8083/actuator/health
http://localhost:7081
```

Resultado esperado:

```text
MS-TECHNICIAN vuelve a estar UP
```

---

## Checklist de evaluacion

Para cerrar esta fase, presentar evidencia de:

- Eureka con todos los servicios `UP`
- health de Gateway y microservicios
- metrics de microservicios
- metrics protegidas de Gateway con token
- metricas especificas `http.server.requests`
- login exitoso en MS1 Auth
- endpoints reales por Gateway
- prueba sin token con `401`
- prueba con token exitosa
- logs locales o consola
- falla controlada de MS3
- MS3 recuperado despues de levantarlo nuevamente

---

## Cierre conceptual

La observabilidad basica en ServiYa permite validar manualmente:

```text
health = si el servicio esta vivo
metrics = datos numericos del servicio
logs = mensajes de lo ocurrido
Gateway = entrada principal y seguridad
Eureka = registro y descubrimiento de servicios
JWT = acceso protegido a rutas sensibles
```

Frase de cierre:

```text
En esta fase valide que ServiYa tiene observabilidad basica manual usando Actuator, Eureka, Gateway y logs locales.
Tambien comprobe que la seguridad JWT protege endpoints sensibles y que una falla controlada puede detectarse con health, Gateway, Eureka y logs.
```
