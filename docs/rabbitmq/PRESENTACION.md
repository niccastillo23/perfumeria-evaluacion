# Guion de presentación — Evaluación Parcial N°4 (RabbitMQ)

**Duración objetivo: 6:40 (límite 7:00).** Orden e hilo conductor: contexto →
API Manager/seguridad → frontend → despliegue → RabbitMQ → cierre.

## Checklist de los 11 puntos exigidos por la pauta

| # | Requisito de la pauta | Dónde se evidencia |
|---|---|---|
| 1 | Instancia de API Manager en funcionamiento | Diapo 3 (consola AWS en vivo) |
| 2 | Configuración del API Manager para llamar al backend | Diapo 3 (integraciones/rutas) |
| 3 | Frontend consume endpoints a través del API Manager | Diapo 6 |
| 4 | API Manager valida JWT (rechaza y acepta) | Diapo 4 |
| 5 | Tenant en IDaaS y usuarios registrados | Diapo 5 (Entra en vivo) |
| 6 | Frontend usa OAuth 2.0 / OIDC para login y JWT | Diapo 6 |
| 7 | Backend y frontend desplegados, activos e integrados | Diapo 7 |
| 8 | Dos nodos RabbitMQ en clúster | Diapo 8 |
| 9 | Tres colas con sus DLQ funcionando | Diapo 10 |
| 10 | Exchanges direct y topic configurados | Diapo 9 |
| 11 | `docker-compose` levantando RabbitMQ en la nube | Diapo 8 |

## Guion minuto a minuto (con qué decir)

| Tiempo | Diapo | Demostración | Frases clave |
|---|---|---|---|
| 0:00–0:35 | 1–2 | Presentar PerfumerIA y la arquitectura React → API Gateway → BFF → microservicios → RabbitMQ | "Mensajería desacoplada sin alterar la lógica previa" |
| 0:35–1:20 | 3 | En consola, instancia de API Manager y su configuración de rutas hacia el backend | "Cada ruta llama al BFF en el puerto 8080" |
| 1:20–1:55 | 4 | Pedir una ruta protegida sin token (`401`) y con token válido (`200`); mostrar `az azure-ad` | "El API Manager valida el JWT contra Entra ID" |
| 1:55–2:25 | 5 | Mostrar tenant de Entra ID y los usuarios registrados | "IDaaS: tenant y usuarios del sistema" |
| 2:25–3:00 | 6 | Iniciar sesión en React (OIDC), mostrar catálogo y confirmar un pedido | "El frontend usa OAuth2/OIDC y manda `Orders.Create`" |
| 3:00–3:35 | 7 | Mostrar EC2, servicios activos y frontend por HTTPS | "Backend y frontend desplegados e integrados en la nube" |
| 3:35–4:25 | 8 | `docker compose ps` y `cluster_status`: dos nodos saludables | "Clúster de dos nodos levantado con docker-compose" |
| 4:25–5:05 | 9 | Management: exchanges `direct` y `topic` con sus routing keys | "Un topic para pedidos y un direct para envíos" |
| 5:05–5:45 | 10 | Management: tres colas y sus tres DLQ | "Cada cola tiene su DLQ durable" |
| 5:45–6:10 | 11 | Logs de notificación, documento y envío; destacar ACK manual | "Cada consumidor confirma con ACK explícito" |
| 6:10–6:30 | 12 | Enviar mensaje inválido, mostrar el log y el mensaje en la DLQ | "Error no recuperable → NACK sin requeue a la DLQ" |
| 6:30–6:40 | 13 | Crear y eliminar cola/exchange/binding y probar nombre inválido | "API REST administrativa con validación" |

Cierre (incluido en 6:30–6:40): "El sistema desacopla notificación, documento y
envío con reintentos, DLQ y seguridad por JWT, sin tocar la lógica existente."

## Antes de presentar

- Verificar activos: frontend, BFF, catálogo, pedidos, envíos, Rabbit Admin y el
  clúster RabbitMQ.
- Tener abiertas: consola de AWS API Gateway, Entra ID, RabbitMQ Management y una
  terminal con `docker compose ps` y `cluster_status` listos.
- Token con `Orders.Create` para checkout y `Admin.Read` para el CRUD.
- Vaciar las DLQ de pruebas previas o explicar el mensaje que se mostrará.
- Tener las capturas de `docs/evidence/rabbitmq/` como respaldo por si falla una
  demo en vivo.
- **Nunca** mostrar `.env`, secretos, client secrets, tokens ni la clave de EC2.

## Respaldo (si algo falla en vivo)

- Nodos → `01-compose-containers.png`, `02-cluster-status.png`,
  `04-management-overview-cloud.png`.
- Exchanges → `05-exchanges-bindings.png`. Colas/DLQ → `06-queues-dlq.png`.
- Consumidores/ACK → `10-async-consumers.png`. DLQ → `11-dlq-error.png`.
- Admin REST → `12-rabbit-admin-api.png`. JWT → `13-api-gateway-jwt.png`.
- Despliegue → `14-cloud-deployment.png`. Login/catálogo/checkout → `07`, `08`, `09`.
