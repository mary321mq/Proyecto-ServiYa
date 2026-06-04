# Sesion 07 y 07g2 - Observabilidad con Herramientas en ServiYa

Este documento adapta las guias del docente **Sesion 07 - Observabilidad con Herramientas** y **Sesion 07g2 - Observabilidad con Herramientas en 30 minutos** al proyecto **ServiYa**.

Esta fase continua despues de la observabilidad basica manual de la Sesion 06 P2.

En la Sesion 06 P2 se uso:

- Eureka
- Actuator
- Gateway
- Swagger
- JWT
- logs locales

En esta sesion se agregan herramientas centralizadas:

- Prometheus
- Loki
- Promtail
- Grafana

---

## Objetivo

Poder demostrar observabilidad centralizada en ServiYa.

Al finalizar esta fase, se debe poder explicar:

1. Si los microservicios estan vivos.
2. Si Prometheus puede recolectar metricas.
3. Si hay trafico HTTP.
4. Que servicios reciben mas peticiones.
5. Cuanta memoria y CPU consumen.
6. Si hay errores HTTP 5xx.
7. Que dicen los logs centralizados.
8. Que pasa cuando un microservicio se cae.
9. Como crear una alerta simple en Grafana.

Flujo general:

```text
Cliente -> Gateway -> Microservicio
                       |
                       +---- metricas ----> Prometheus
                       |
                       +---- logs --------> Promtail -> Loki

Prometheus + Loki -> Grafana
```

Ejemplos en ServiYa:

```text
Cliente -> Gateway -> MS3 Technician
Cliente -> Gateway -> MS4 Service Request
Cliente -> Gateway -> MS5 Assignment
Cliente -> Gateway -> MS6 Payment
```

---

## Herramientas usadas

| Herramienta | Funcion |
|---|---|
| Prometheus | Recolecta metricas de los microservicios |
| Loki | Almacena y permite consultar logs |
| Promtail | Lee logs locales y los envia a Loki |
| Grafana | Visualiza metricas, logs, dashboards y alertas |

---

## Microservicios observados

| Servicio | Nombre en Eureka | Job en Prometheus | Puerto DEV |
|---|---|---:|---:|
| Gateway | GATEWAY | gateway-dev | 7091 |
| MS1 Auth | MS-AUTH | ms-auth-dev | 8081 |
| MS2 User | MS-USER | ms-user-dev | 8082 |
| MS3 Technician | MS-TECHNICIAN | ms-technician-dev | 8083 |
| MS4 Service Request | MS-SERVICE-REQUEST | ms-service-request-dev | 8084 |
| MS5 Assignment | MS-ASSIGNMENT | ms-assignment-dev | 8085 |
| MS6 Payment | MS-PAYMENT | ms-payment-dev | 8086 |
| MS7 Review | MS-REVIEW | ms-review-dev | 8087 |
| MS8 Notification | MS-NOTIFICATION | ms-notification-dev | 8088 |

---

## URLs principales

```text
Eureka:      http://localhost:7081
Gateway:     http://localhost:7091
Swagger:     http://localhost:7091/swagger-ui/index.html
Prometheus:  http://localhost:19090
Grafana:     http://localhost:13000
Loki:        http://localhost:13100
```

Credenciales de Grafana:

```text
usuario: admin
clave:   admin
```

---

## Preparacion en DEV

Antes de levantar observabilidad deben estar arriba:

1. Config Server
2. Registry Server / Eureka
3. Gateway
4. Bases de datos de los microservicios
5. Microservicios de ServiYa

Orden recomendado:

```text
Config Server
Registry Server / Eureka
Gateway
MS1 Auth
MS2 User
MS8 Notification
MS3 Technician
MS4 Service Request
MS5 Assignment
MS6 Payment
MS7 Review
```

Luego levantar observabilidad:

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\observability"
docker compose -f docker-compose-dev.yml up -d
```

Este comando levanta:

```text
Prometheus
Loki
Promtail
Grafana
```

---

## Paso 1. Ver que Prometheus detecta los servicios

### Objetivo

Comprobar que Prometheus puede recolectar metricas de los microservicios.

Abrir:

```text
http://localhost:19090/targets
```

Se espera `UP` en:

```text
prometheus
gateway-dev
ms-auth-dev
ms-user-dev
ms-technician-dev
ms-service-request-dev
ms-assignment-dev
ms-payment-dev
ms-review-dev
ms-notification-dev
```

### Consulta base

En Prometheus ejecutar:

```promql
up
```

Interpretacion:

```text
1 = Prometheus puede consultar metricas del servicio
0 = Prometheus no puede consultar metricas del servicio
```

### Consulta por MS3

```promql
up{job="ms-technician-dev"}
```

Resultado esperado:

```text
1
```

### Como explicarlo

```text
Eureka muestra que los servicios estan registrados.
Prometheus muestra que puede recolectar metricas de esos servicios.
El estado UP en Prometheus confirma que el endpoint de metricas responde correctamente.
```

### Evidencia

Guardar captura de:

```text
http://localhost:19090/targets
```

y de la consulta:

```promql
up
```

---

## Paso 2. Ver trafico HTTP

### Objetivo

Pasar de:

```text
El servicio esta vivo
```

a:

```text
El servicio esta recibiendo requests
```

### Generar trafico

Usar endpoints reales por Gateway.

#### MS3 Technician

```text
http://localhost:7091/api/v1/tecnicos/1
```

Tambien:

```text
http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

Nota:

```text
Un 404 tambien genera trafico HTTP.
Para Prometheus sirve porque la peticion llego al sistema.
```

#### MS4 Service Request

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/solicitudes" `
  -Headers @{ Authorization = "Bearer $token" }
```

#### MS5 Assignment

Requiere token ADMIN:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/asignaciones" `
  -Headers @{ Authorization = "Bearer $token" }
```

#### MS6 Payment

Requiere token:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Headers @{ Authorization = "Bearer $token" }
```

Ejecutar cada peticion varias veces.

### Consulta recomendada

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Si no aparece informacion, usar:

```promql
sum by (job) (rate(http_server_requests_seconds_count[5m]))
```

Para total acumulado:

```promql
sum by (job) (http_server_requests_seconds_count)
```

### Como leer la consulta

```text
Prometheus, dime cuantas requests por segundo recibio cada servicio durante el ultimo minuto.
```

Partes:

```text
http_server_requests_seconds_count = contador acumulado de peticiones HTTP
rate(...[1m]) = velocidad de crecimiento en el ultimo minuto
sum by (job) = agrupa por microservicio
```

### Evidencia

Guardar captura de:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Donde aparezcan servicios como:

```text
gateway-dev
ms-technician-dev
ms-payment-dev
ms-service-request-dev
```

---

## Paso 3. Ver recursos basicos

### Objetivo

Observar consumo de recursos, no solo trafico.

---

### 3.1 Memoria JVM

Consulta:

```promql
sum by (job) (jvm_memory_used_bytes)
```

Para verlo en MB:

```promql
sum by (job) (jvm_memory_used_bytes) / 1024 / 1024
```

### Como explicarlo

```text
jvm_memory_used_bytes muestra cuanta memoria esta usando la JVM de cada microservicio.
Al dividir entre 1024 dos veces se obtiene el valor aproximado en megabytes.
```

---

### 3.2 CPU

Consulta:

```promql
avg by (job) (system_cpu_usage)
```

Si no muestra informacion suficiente:

```promql
avg by (job) (process_cpu_usage)
```

Interpretacion:

```text
0.01 = 1%
0.10 = 10%
0.80 = 80%
```

### Como explicarlo

```text
Las metricas de recursos permiten detectar si un microservicio esta consumiendo mucha memoria o CPU.
Esto es distinto a monitorear trafico HTTP.
```

### Evidencia

Guardar captura de:

```promql
sum by (job) (jvm_memory_used_bytes) / 1024 / 1024
```

y:

```promql
avg by (job) (system_cpu_usage)
```

o:

```promql
avg by (job) (process_cpu_usage)
```

---

## Paso 4. Ver errores HTTP

### Objetivo

Detectar respuestas de error del servidor.

Consulta:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

Si se quiere ver acumulado:

```promql
sum by (job, status) (http_server_requests_seconds_count{status=~"5.."})
```

### Como leer la consulta

```text
Por cada servicio y codigo HTTP 5xx, dime cuantos errores por segundo se estan generando.
```

Partes:

```text
status=~"5.." = filtra codigos 500, 502, 503, etc.
rate(...[1m]) = calcula errores por segundo en el ultimo minuto
sum by (job, status) = agrupa por servicio y codigo HTTP
```

### Resultado esperado

Normalmente debe estar:

```text
0
```

Si sube, existe un error de servidor.

### Como explicarlo

```text
Prometheus detecta el sintoma del error, pero no explica la causa exacta.
Para investigar la causa se revisan los logs en Loki.
```

---

## Paso 5. Entrar a Loki desde Grafana

### Objetivo

Consultar logs centralizados.

Abrir Grafana:

```text
http://localhost:13000
```

Ir a:

```text
Explore
```

Seleccionar datasource:

```text
Loki
```

Usar rango:

```text
Last 5 minutes
```

Si no aparece nada:

```text
Last 15 minutes
```

o:

```text
Last 1 hour
```

---

### 5.1 Consulta general

```logql
{service=~"gateway|ms-auth|ms-user|ms-technician|ms-service-request|ms-assignment|ms-payment|ms-review|ms-notification"}
```

### Como leerla

```text
Loki, muestrame logs cuyo label service sea cualquiera de esos servicios.
```

---

### 5.2 Logs por servicio

Gateway:

```logql
{service="gateway"}
```

MS1 Auth:

```logql
{service="ms-auth"}
```

MS3 Technician:

```logql
{service="ms-technician"}
```

MS4 Service Request:

```logql
{service="ms-service-request"}
```

MS5 Assignment:

```logql
{service="ms-assignment"}
```

MS6 Payment:

```logql
{service="ms-payment"}
```

MS7 Review:

```logql
{service="ms-review"}
```

MS8 Notification:

```logql
{service="ms-notification"}
```

---

### 5.3 Filtrar por texto

Errores:

```logql
{service=~"gateway|ms-payment|ms-technician"} |= "ERROR"
```

Rutas:

```logql
{service="gateway"} |= "api/v1"
```

Pagos:

```logql
{service="ms-payment"} |= "pago"
```

Codigos 401:

```logql
{service="gateway"} |= "401"
```

### Como explicarlo

```text
Loki centraliza logs de los microservicios.
Ya no se necesita abrir varias consolas para revisar que paso.
Se puede filtrar por servicio o por texto.
```

### Evidencia

Guardar capturas de:

```text
Grafana Explore con datasource Loki
{service="gateway"}
{service="ms-payment"}
{service="ms-technician"}
consulta general con varios servicios
```

---

## Paso 6. Caso end-to-end

### Objetivo

Seguir una peticion real usando Prometheus y Loki.

En la guia del docente:

```text
Cliente -> Gateway -> Producto -> Catalogo
```

En ServiYa se puede demostrar con:

```text
Cliente -> Gateway -> MS6 Payment
```

o:

```text
Cliente -> Gateway -> MS3 Technician
```

---

### 6.1 Generar peticion hacia MS6

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:7091/api/v1/pagos" `
  -Headers @{ Authorization = "Bearer $token" }
```

Ejecutar varias veces.

### 6.2 Ver metrica

Prometheus:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Buscar:

```text
gateway-dev
ms-payment-dev
```

### 6.3 Ver logs

Grafana Explore con Loki:

```logql
{service=~"gateway|ms-payment"}
```

### Como explicarlo

```text
La peticion entro por Gateway y llego a MS6 Payment.
Prometheus mostro trafico HTTP en los servicios involucrados.
Loki permitio revisar logs centralizados relacionados con esos servicios.
```

---

### 6.4 Caso alternativo con MS3

Generar peticion:

```text
http://localhost:7091/api/v1/tecnicos/1
```

Prometheus:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Buscar:

```text
gateway-dev
ms-technician-dev
```

Loki:

```logql
{service=~"gateway|ms-technician"}
```

---

## Paso 7. Crear dashboard inicial en Grafana

### Objetivo

Pasar de consultas manuales a una vista reutilizable.

Abrir:

```text
http://localhost:13000
```

Ir a:

```text
Dashboards -> New -> New dashboard -> Add visualization
```

Datasource:

```text
Prometheus
```

---

### Panel 1. Servicios vivos

Consulta:

```promql
up
```

Tipo:

```text
Stat
```

Titulo:

```text
Servicios vivos
```

Alternativa:

```promql
sum(up)
```

Titulo:

```text
Targets UP
```

---

### Panel 2. Requests por segundo

Consulta:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Tipo:

```text
Time series
```

Titulo:

```text
Requests por segundo
```

Leyenda:

```text
{{job}}
```

---

### Panel 3. Memoria JVM

Consulta:

```promql
sum by (job) (jvm_memory_used_bytes) / 1024 / 1024
```

Tipo:

```text
Bar gauge
```

o:

```text
Time series
```

Titulo:

```text
Memoria JVM MB
```

---

### Panel 4. CPU

Consulta:

```promql
avg by (job) (system_cpu_usage)
```

Si no muestra bien:

```promql
avg by (job) (process_cpu_usage)
```

Tipo:

```text
Time series
```

Titulo:

```text
CPU por servicio
```

---

### Panel 5. Errores 5xx

Consulta:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

Tipo:

```text
Time series
```

Titulo:

```text
Errores 5xx
```

Leyenda:

```text
{{job}} - {{status}}
```

---

### Guardar dashboard

Nombre:

```text
ServiYa - Observabilidad DEV
```

### Evidencia

Guardar captura del dashboard con:

```text
Servicios vivos
Requests por segundo
Memoria JVM MB
CPU por servicio
Errores 5xx
```

---

## Paso 8. Falla controlada con herramientas

### Objetivo

Ver que pasa cuando un microservicio se cae usando Prometheus, Grafana, Loki y Eureka.

Se recomienda usar:

```text
MS3 Technician
```

---

### 8.1 Estado normal

Health:

```text
http://localhost:8083/actuator/health
```

Prometheus:

```promql
up{job="ms-technician-dev"}
```

Resultado esperado:

```text
1
```

Targets:

```text
http://localhost:19090/targets
```

Debe verse:

```text
ms-technician-dev UP
```

---

### 8.2 Generar trafico antes de apagar

```text
http://localhost:7091/api/v1/tecnicos/1
```

o:

```text
http://localhost:7091/api/v1/tecnicos/cercanos?solicitudId=1&radioKm=5
```

Prometheus:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Buscar:

```text
gateway-dev
ms-technician-dev
```

---

### 8.3 Detener MS3

En la terminal donde corre MS3:

```text
Ctrl + C
```

No detener MySQL.

---

### 8.4 Ver cambio en Prometheus

Esperar unos segundos.

Consulta:

```promql
up{job="ms-technician-dev"}
```

Resultado esperado:

```text
0
```

En:

```text
http://localhost:19090/targets
```

Debe cambiar a:

```text
DOWN
```

---

### 8.5 Probar Gateway con MS3 caido

```text
http://localhost:7091/api/v1/tecnicos/1
```

Resultado esperado:

```text
503 Service Unavailable
```

o error equivalente.

### Como explicarlo

```text
El Gateway recibio la peticion, pero no encontro una instancia disponible de MS3 Technician.
```

---

### 8.6 Ver logs en Loki

Grafana Explore:

```text
http://localhost:13000/explore
```

Datasource:

```text
Loki
```

Consulta:

```logql
{service=~"gateway|ms-technician"}
```

Filtros utiles:

```logql
{service=~"gateway|ms-technician"} |= "ERROR"
```

```logql
{service="gateway"} |= "MS-TECHNICIAN"
```

```logql
{service="gateway"} |= "503"
```

---

### 8.7 Ver dashboard

Abrir dashboard:

```text
ServiYa - Observabilidad DEV
```

Observar:

```text
Servicios vivos
Requests por segundo
Errores 5xx
```

---

### 8.8 Levantar MS3 nuevamente

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\services\MS3"
mvn spring-boot:run
```

Verificar:

```promql
up{job="ms-technician-dev"}
```

Resultado esperado:

```text
1
```

Targets:

```text
ms-technician-dev UP
```

### Evidencia

Guardar capturas de:

```text
up{job="ms-technician-dev"} = 1 antes
targets con ms-technician-dev UP
MS3 detenido
up{job="ms-technician-dev"} = 0
targets con ms-technician-dev DOWN
Gateway devolviendo error
Loki mostrando logs relacionados
Dashboard mostrando cambio
MS3 levantado nuevamente
```

---

## Paso 9. Crear alerta simple en Grafana

Este paso viene de la guia 07g2.

### Objetivo

Crear una alerta que avise cuando MS3 Technician se cae.

Alerta:

```text
MS3 Technician caido
```

---

### 9.1 Abrir Alert rules

Grafana:

```text
http://localhost:13000
```

Ir a:

```text
Alerting -> Alert rules -> New alert rule
```

---

### 9.2 Nombre de la regla

```text
MS3 Technician caido
```

Folder:

```text
Observabilidad
```

Si no existe:

```text
General
```

Evaluation group:

```text
microservicios
```

Si se debe crear:

```text
Evaluate every: 30s
```

---

### 9.3 Consulta Prometheus

Datasource:

```text
Prometheus
```

Consulta:

```promql
up{job="ms-technician-dev"}
```

Lectura:

```text
Prometheus, dime si MS3 Technician esta disponible.
```

Valor normal:

```text
1
```

Valor caido:

```text
0
```

---

### 9.4 Condicion

Configurar:

```text
IS BELOW 1
```

En algunas versiones de Grafana:

```text
A = consulta Prometheus
B = Reduce / Last de A
C = Threshold: B is below 1
```

Lectura:

```text
Si el ultimo valor de ms-technician-dev baja de 1, entonces el servicio esta caido.
```

---

### 9.5 Evaluacion

Usar:

```text
Every 30s for 1m
```

Interpretacion:

```text
Grafana revisa cada 30 segundos.
La condicion debe mantenerse fallando durante 1 minuto para activar la alerta.
```

---

### 9.6 Mensaje o annotation

Mensaje sugerido:

```text
Prometheus no puede consultar ms-technician-dev. Revisar si MS3 Technician esta levantado y si el puerto 8083 responde.
```

Descripcion:

```text
El microservicio MS3 Technician no esta disponible para Prometheus. Validar Eureka, health directo y terminal del servicio.
```

Guardar:

```text
Save rule and exit
```

No es obligatorio configurar correo, Slack o Teams para esta practica.

---

## Paso 10. Probar la alerta

### 10.1 Estado normal

Prometheus:

```promql
up{job="ms-technician-dev"}
```

Resultado:

```text
1
```

La alerta debe estar:

```text
Normal
```

---

### 10.2 Detener MS3

En la terminal de MS3:

```text
Ctrl + C
```

---

### 10.3 Esperar evaluacion

Esperar:

```text
1 minuto
```

o un poco mas.

En Grafana:

```text
Alerting -> Alert rules
```

La alerta debe cambiar:

```text
Normal -> Pending -> Firing
```

---

### 10.4 Levantar MS3 otra vez

```powershell
cd "C:\CICLO 5\ServiYa\ServiYa\services\MS3"
mvn spring-boot:run
```

Prometheus:

```promql
up{job="ms-technician-dev"}
```

Debe volver a:

```text
1
```

La alerta debe volver a:

```text
Normal
```

### Evidencia

Guardar capturas de:

```text
Regla de alerta creada
Consulta up{job="ms-technician-dev"}
Condicion IS BELOW 1
Alerta en Normal
MS3 detenido
Alerta en Pending o Firing
MS3 levantado nuevamente
Alerta vuelve a Normal
```

---

## Dashboard minimo recomendado

| Panel | Consulta | Para que sirve |
|---|---|---|
| Servicios vivos | `up` | Ver disponibilidad |
| Requests por segundo | `sum by (job) (rate(http_server_requests_seconds_count[1m]))` | Ver trafico HTTP |
| Memoria JVM | `sum by (job) (jvm_memory_used_bytes) / 1024 / 1024` | Ver memoria por servicio |
| CPU | `avg by (job) (system_cpu_usage)` | Ver CPU por servicio |
| Errores 5xx | `sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))` | Detectar fallas HTTP |

Orden de lectura:

```text
1. Esta vivo?
2. Recibe trafico?
3. Consume muchos recursos?
4. Tiene errores?
5. Que dicen los logs?
6. Hay alerta activa?
```

---

## Consultas utiles PromQL

Disponibilidad:

```promql
up
```

MS3 disponible:

```promql
up{job="ms-technician-dev"}
```

Requests por segundo:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Requests acumuladas:

```promql
sum by (job) (http_server_requests_seconds_count)
```

Memoria JVM MB:

```promql
sum by (job) (jvm_memory_used_bytes) / 1024 / 1024
```

CPU:

```promql
avg by (job) (system_cpu_usage)
```

CPU alternativa:

```promql
avg by (job) (process_cpu_usage)
```

Errores 5xx:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

---

## Consultas utiles LogQL

Todos los servicios:

```logql
{service=~"gateway|ms-auth|ms-user|ms-technician|ms-service-request|ms-assignment|ms-payment|ms-review|ms-notification"}
```

Gateway:

```logql
{service="gateway"}
```

MS3:

```logql
{service="ms-technician"}
```

MS6:

```logql
{service="ms-payment"}
```

Gateway y MS3:

```logql
{service=~"gateway|ms-technician"}
```

Gateway y MS6:

```logql
{service=~"gateway|ms-payment"}
```

Errores:

```logql
{service=~"gateway|ms-technician|ms-payment"} |= "ERROR"
```

Rutas:

```logql
{service="gateway"} |= "api/v1"
```

Codigo 401:

```logql
{service="gateway"} |= "401"
```

Codigo 503:

```logql
{service="gateway"} |= "503"
```

---

## Checklist de evaluacion

Para cerrar esta fase, presentar evidencia de:

- Prometheus abierto en `http://localhost:19090`
- Targets `UP`
- Consulta `up`
- Consulta de trafico HTTP
- Consulta de memoria JVM
- Consulta de CPU
- Consulta de errores 5xx
- Grafana abierto en `http://localhost:13000`
- Datasource Prometheus funcionando
- Datasource Loki funcionando
- Logs por servicio en Loki
- Caso end-to-end con Gateway y un MS
- Dashboard inicial en Grafana
- Falla controlada de MS3
- Prometheus detectando `ms-technician-dev` en `DOWN`
- Logs de la falla en Loki
- Alerta simple creada
- Alerta cambiando a `Pending` o `Firing`
- MS3 recuperado y alerta volviendo a `Normal`

---

## Cierre conceptual

Frase para explicar la sesion:

```text
Las metricas me dicen que algo paso.
Los logs me ayudan a entender por que paso.
Las alertas me avisan cuando debo mirar.
```

Aplicado a ServiYa:

```text
Prometheus permite observar disponibilidad, trafico, CPU, memoria y errores de los microservicios.
Loki permite revisar logs centralizados por servicio.
Grafana permite construir dashboards y alertas.
Con una falla controlada en MS3 Technician se demuestra que la observabilidad permite detectar, investigar y recuperar un servicio caido.
```
