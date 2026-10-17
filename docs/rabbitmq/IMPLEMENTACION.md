# Implementación RabbitMQ — PerfumerIA

Registro vivo del trabajo del Encargo de RabbitMQ. Se completa con evidencia de
la ejecución real antes de entregar.

## Alcance y diseño

- Conservar React, MSAL, BFF y los microservicios existentes.
- El checkout del frontend llama al BFF con el scope `Orders.Create`.
- `microservice-2` guarda la orden y publica eventos JSON persistentes.
- El exchange topic `perfumeria.orders.topic` dirige `orders.created` y
  `orders.paid` a consumidores de notificaciones y documentos.
- El exchange direct `perfumeria.shipping.direct` envía `shipping.requested` al
  consumidor del servicio de envíos.
- Cada cola funcional tiene una DLQ durable; el exchange
  `perfumeria.dead-letter.direct` recibe los rechazos definitivos.
- `rabbit-admin-service` ofrece operaciones para crear y eliminar colas,
  exchanges y bindings. El BFF las protege con `Admin.Read`.

## Topología declarada

| Propósito | Exchange | Enrutamiento | Cola durable | DLQ durable |
|---|---|---|---|---|
| Avisos de pedido | `perfumeria.orders.topic` (topic) | `orders.*` | `perfumeria.orders.notifications` | `perfumeria.orders.notifications.dlq` |
| Documento de compra | `perfumeria.orders.topic` (topic) | `orders.paid` | `perfumeria.orders.documents` | `perfumeria.orders.documents.dlq` |
| Solicitud de envío | `perfumeria.shipping.direct` (direct) | `shipping.requested` | `perfumeria.shipping.requests` | `perfumeria.shipping.requests.dlq` |
| Mensajes rechazados | `perfumeria.dead-letter.direct` (direct) | routing keys DLQ por dominio | — | bindings a las tres DLQ |

Los nombres y routing keys están definidos en `application.yml`; la declaración
de `Queue`, `Exchange` y `Binding` se encuentra en clases de configuración
separadas de la lógica de negocio.

## Paso a paso para reproducir en local

### 1. Preparar RabbitMQ

1. Iniciar Docker Desktop.
2. Desde `infra/rabbitmq`, copiar `.env.example` a `.env` y definir credenciales
   locales propias.
3. Ejecutar:

   ```bash
   docker compose up -d
   docker compose ps
   docker compose exec rabbitmq1 rabbitmq-diagnostics cluster_status
   docker compose exec rabbitmq2 rabbitmq-diagnostics ping
   ```

4. Abrir `http://localhost:15672` (nodo 1) y
   `http://localhost:15673` (nodo 2); comprobar nodos, exchanges, bindings,
   colas y consumidores.

### 2. Ejecutar los microservicios

RabbitMQ debe estar activo antes de iniciar los consumidores.
En cada terminal de Java, desde el directorio del microservicio, cargar los
mismos valores del `.env` usado por Compose:

```bash
set -a
source ../../infra/rabbitmq/.env
set +a
```

```bash
cd backend/microservice-2
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

```bash
cd backend/shipping-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

```bash
cd backend/rabbit-admin-service
mvn spring-boot:run
```

Pedidos y envíos usan H2 con el perfil `h2` para pruebas locales. En producción
se conserva PostgreSQL y se configuran las credenciales RabbitMQ mediante
variables de entorno.

### 3. Comprobar un pedido y las tareas asíncronas

Con el BFF iniciado en modo local de desarrollo (`SECURITY_ENABLED=false`), enviar
un checkout por el endpoint del BFF:

```bash
curl -i -X POST http://localhost:8080/api/v1/shop/checkout \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":1,"name":"Elegance Gold","quantity":1,"price":189.99}],"total":189.99,"payment":{"type":"cash_on_delivery"},"carrier":"dhl","destination":"Santiago"}'
```

Comprobar la respuesta de la orden y los logs de consumidores: aviso de pedido,
documento de compra y registro de envío. La interfaz React realiza la misma
operación con un token MSAL y muestra el número de pedido.

### 4. Comprobar ACK, reintentos y DLQ

- Un mensaje válido termina con ACK manual y desaparece de la cola funcional.
- Los errores recuperables se reintentan el número configurado en
  `RABBITMQ_CONSUMER_MAX_ATTEMPTS` (por defecto, 3).
- Los payloads inválidos y los reintentos agotados reciben NACK sin requeue y
  RabbitMQ los enruta a la DLQ correspondiente.
- Confirmar el log del consumidor y el contador de mensajes de la DLQ en
  Management UI.

### 5. Probar el administrador RabbitMQ

El servicio interno escucha en `8087`; usar los endpoints protegidos del BFF en
`8080` con un token que tenga `Admin.Read`.

| Operación | Endpoint |
|---|---|
| Crear cola | `POST /api/v1/admin/rabbitmq/queues` |
| Eliminar cola | `DELETE /api/v1/admin/rabbitmq/queues/{name}` |
| Crear exchange | `POST /api/v1/admin/rabbitmq/exchanges` |
| Eliminar exchange | `DELETE /api/v1/admin/rabbitmq/exchanges/{name}` |
| Crear binding | `POST /api/v1/admin/rabbitmq/bindings` |
| Eliminar binding | `DELETE /api/v1/admin/rabbitmq/bindings` |

Los DTO validan nombres, tipo de exchange, flags y routing keys. La creación de
cola permite incluir el exchange y la routing key de su DLQ.

Ejemplo de DTO para crear recursos (enviar con `Authorization: Bearer <token>` al
BFF; `type` acepta `DIRECT` o `TOPIC`):

```json
{
  "name": "demo.orders.queue",
  "durable": true,
  "exclusive": false,
  "autoDelete": false,
  "deadLetterExchange": "perfumeria.dead-letter.direct",
  "deadLetterRoutingKey": "demo.orders.dead"
}
```

```json
{"name":"demo.orders.topic","type":"TOPIC","durable":true,"autoDelete":false}
```

```json
{"queueName":"demo.orders.queue","exchangeName":"demo.orders.topic","routingKey":"orders.*"}
```

### 6. Preparar la demostración cloud

1. Copiar el código a EC2 y configurar variables de entorno seguras.
2. Levantar ambos nodos con Docker Compose.
3. Comprobar el clúster con `rabbitmq-diagnostics cluster_status`.
4. Iniciar los servicios backend y publicar el build React como en la
   Evaluación 1.
5. En AWS API Gateway, agregar las rutas de administración RabbitMQ con el
   autorizador JWT y conservar los scopes existentes.
6. No exponer públicamente los puertos `5672`, `5673`, `15672`, `15673` ni `8087`.
   Para abrir Management UI remotamente, usar un túnel SSH al loopback.

## Verificaciones ejecutadas

- `mvn test` en `microservice-2`, `shipping-service`, `rabbit-admin-service` y
  `bff`.
- `npm test -- --run` y `npm run build` en `frontend/web-app`.
- `docker compose config` para validar la sintaxis de Compose.
- Clúster RabbitMQ 4.2.9 iniciado con dos nodos; ambos aparecen saludables y
  `cluster_status` muestra los dos nodos en estado running.
- La EC2 `3.239.252.152` en `us-east-1` tiene Docker/Compose, el clúster de dos
  nodos y los servicios de catálogo, pedidos, envíos, Rabbit Admin y BFF activos.
- Nginx sirve el build React en `https://3.239.252.152.sslip.io`; la respuesta
  HTTPS fue verificada con certificado válido.
- Los tres exchanges, tres colas funcionales, tres DLQ, bindings y consumidores
  se declararon correctamente al iniciar pedidos y envíos.
- Checkout en local y en EC2 devolvió `200` con estado `PAID`; se observaron los
  consumidores de notificación, documento y envío procesando los eventos.
- En local y en EC2, un payload JSON inválido fue rechazado y terminó en
  `perfumeria.shipping.requests.dlq`.
- La API administrativa local y de EC2 creó/eliminó una cola, un exchange y un
  binding; un nombre vacío fue rechazado con `400`, también a través del BFF.
- La seguridad del BFF se verificó con pruebas `401` sin token y `403` sin
  `Admin.Read`; en EC2, health responde `200` y las rutas protegidas rechazan una
  llamada sin token con `401`.
- Pendiente para cerrar la demostración: actualizar la integración/rutas/CORS del
  API Gateway a la IP nueva y añadir el nuevo redirect URI en Entra ID. No hay
  credenciales de administración AWS/Azure disponibles desde esta sesión.

## Registro para el informe final

Las capturas y sus descripciones se registran en
[`EVIDENCIAS.md`](EVIDENCIAS.md). Después de completar las pruebas cloud, se
actualizará esta guía con los resultados, marcas de tiempo y conclusiones reales.
