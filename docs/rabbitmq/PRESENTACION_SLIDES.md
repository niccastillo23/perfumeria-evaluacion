# PerfumerIA — RabbitMQ
## Evaluación Parcial N°4 | Desarrollo Cloud Native I (DSY1107)
Integrantes: (completar) · Octubre 2026

# Contexto y arquitectura
- Mensajería asíncrona para tareas de **notificación, documento y envío**.
- Flujo: **React → API Gateway → BFF → microservicios → RabbitMQ**.
- React con MSAL (OAuth2/OIDC); sin alterar la lógica existente.

# API Manager: instancia y configuración
- Instancia de **AWS API Gateway** en funcionamiento (mostrar en consola).
- Rutas integradas al **BFF** (`3.239.252.152:8080`).
- Ruta admin `ANY /api/v1/admin/rabbitmq/{proxy+}` con JWT.

# Validación JWT
- Sin token → **401**; con token válido → **200**.
- Autorizador `azure-ad` (emisor Entra, audiencia del API, scope `Admin.Read`).

![Ruta protegida](../evidence/rabbitmq/13-api-gateway-jwt.png)

# IDaaS: tenant y usuarios
- **Microsoft Entra ID**: tenant y usuarios registrados (mostrar en portal).
- App SPA con redirect URI `https://3.239.252.152.sslip.io`.

# Frontend con OAuth 2.0 / OIDC
- Login con MSAL y JWT en cada llamada al API Manager.

![Login](../evidence/rabbitmq/07-login-react-cloud.png)

![Catálogo](../evidence/rabbitmq/08-catalogo-react.png)

# Despliegue en la nube
- Backend (BFF, catálogo, pedidos, envíos, Rabbit Admin) y frontend **activos**.
- Frontend React por **HTTPS** con nginx + Let's Encrypt.

![Despliegue](../evidence/rabbitmq/14-cloud-deployment.png)

# RabbitMQ con docker-compose
- Dos nodos (`rabbitmq1`, `rabbitmq2`) en **clúster**, saludables.

![compose ps](../evidence/rabbitmq/01-compose-containers.png)

![cluster_status](../evidence/rabbitmq/02-cluster-status.png)

# Exchanges direct y topic
- `perfumeria.orders.topic` (topic) y `perfumeria.shipping.direct` (direct).
- Dead-letter exchange direct para los rechazos.

![Exchanges](../evidence/rabbitmq/05-exchanges-bindings.png)

# Colas y DLQ
- Tres colas funcionales y sus tres **DLQ** durables.

![Colas y DLQ](../evidence/rabbitmq/06-queues-dlq.png)

# Consumidores y ACK
- `@RabbitListener` por dominio: notificación, documento y envío.
- **ACK manual** tras procesar; reintentos configurables.

![Consumidores](../evidence/rabbitmq/10-async-consumers.png)

# Manejo de errores → DLQ
- Payload inválido o reintentos agotados → **NACK sin requeue** a la DLQ.

![DLQ](../evidence/rabbitmq/11-dlq-error.png)

# Administrador REST
- `POST/DELETE` de colas, exchanges y bindings.
- Lógica en `RabbitAdminService`; DTO validados → `400` si el nombre es inválido.

![Admin REST](../evidence/rabbitmq/12-rabbit-admin-api.png)

# Cierre
- Desacoplamiento real con **reintentos, DLQ y seguridad JWT**.
- Sin afectar la lógica existente del sistema.
