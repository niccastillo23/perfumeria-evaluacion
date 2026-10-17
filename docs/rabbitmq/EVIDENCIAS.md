# Capturas para informe y presentación

Guardar las capturas reales en `docs/evidence/rabbitmq/`, usando los nombres
propuestos. No capturar ni publicar tokens, contraseñas, cookies, claves privadas
ni archivos `.env`.

## Capturas

| Archivo | Qué muestra | Origen |
|---|---|---|
| `01-compose-containers.png` | `docker compose ps`: los dos nodos saludables | EC2 |
| `02-cluster-status.png` | `rabbitmq-diagnostics cluster_status` con ambos nodos | EC2 |
| `03-management-login-local.png` | RabbitMQ Management, pantalla de acceso | Local |
| `04-management-overview-cloud.png` | RabbitMQ Management: overview del clúster en EC2 | EC2 |
| `05-exchanges-bindings.png` | Exchange `direct`, exchange `topic` y routing keys | EC2 |
| `06-queues-dlq.png` | Tres colas funcionales y sus tres DLQ | EC2 |
| `07-login-react-cloud.png` | Login React servido por HTTPS desde el hostname EC2 | EC2 |
| `08-catalogo-react.png` | Catálogo React con sesión Entra activa (token válido) | EC2 |
| `09-checkout-react.png` | Checkout React: carrito con productos y `CONFIRMAR Y PAGAR` | EC2 |
| `10-async-consumers.png` | Logs de notificación, documento y envío procesados | EC2 |
| `11-dlq-error.png` | Fallo registrado y mensaje visible en su DLQ | EC2 |
| `12-rabbit-admin-api.png` | Creación/eliminación de cola, exchange y binding + validación | EC2 |
| `13-api-gateway-jwt.png` | Ruta protegida: `/health` `200` y rechazo `401` sin token | EC2 |
| `14-cloud-deployment.png` | EC2, frontend y servicios backend activos | EC2 |

## Detalle de la evidencia cloud (2026-10-17, EC2 3.239.252.152)

| Captura | Comando / pantalla | Criterio demostrado | Resultado observado |
|---|---|---|---|
| `01-compose-containers.png` | `docker compose ps` | Clúster de 2 nodos arriba y saludables | `rabbitmq1` y `rabbitmq2` `Up (healthy)` |
| `02-cluster-status.png` | `rabbitmq-diagnostics cluster_status` | Ambos nodos se reconocen como disco y en ejecución | `rabbit@rabbitmq1`, `rabbit@rabbitmq2` `Running`, sin particiones |
| `04-management-overview-cloud.png` | Management UI | Panel accesible en EC2 | Overview de `rabbit@rabbitmq1`, RabbitMQ 4.2.9 |
| `05-exchanges-bindings.png` | Management → Exchanges | Exchanges `direct` y `topic` presentes | `amq.direct`, exchanges propios y `amq.topic` listados |
| `06-queues-dlq.png` | Management → Queues | Colas funcionales + DLQ | 6 colas: 3 funcionales + 3 DLQ |
| `07-login-react-cloud.png` | `https://3.239.252.152.sslip.io` | React servido por HTTPS con login Entra | Pantalla de acceso de PerfumerIA |
| `08-catalogo-react.png` | React tras iniciar sesión | Token válido consumiendo `Catalog.Read` vía API Gateway | Catálogo de 11 fragancias con usuario `(ADMIN)` |
| `09-checkout-react.png` | React → Carrito | UI de checkout lista para enviar a `Orders.Create` | 2 productos, total `$315.75`, botón `CONFIRMAR Y PAGAR` |
| `10-async-consumers.png` | `journalctl -u perfumeria-orders -u perfumeria-shipping` | Procesamiento asíncrono de los 3 eventos | Notificación, documento y envío procesados; ACK manual |
| `11-dlq-error.png` | Management → `perfumeria.shipping.requests.dlq` | Fallo derivado a DLQ | `Ready: 1`, mensaje conservado sin consumidores |
| `12-rabbit-admin-api.png` | CRUD `POST/DELETE /api/rabbitmq/*` | Alta/baja de cola, exchange y binding + validación | `201/204`; entrada inválida `amq.*` → `400` |
| `13-api-gateway-jwt.png` | `curl` vía API Gateway y BFF | Ruta protegida rechaza sin token | `/health` `200`; catálogo y admin `401` sin token |
| `14-cloud-deployment.png` | `systemctl` + `curl` HTTPS | Servicios backend y frontend activos | 5 servicios `active running`; frontend `200` |

## Comprobaciones con token válido vía API Gateway

| Caso | Resultado |
|---|---:|
| `GET /api/v1/health` | `200` |
| `GET /api/v1/shop/catalog` sin token | `401` |
| `ANY /api/v1/admin/rabbitmq/{proxy+}` sin token | `401` |
| `OPTIONS /api/v1/admin/rabbitmq/{proxy+}` (CORS) | `200` con `DELETE` permitido |
| Checkout real desde React (`POST /api/v1/shop/checkout`) | orden `PAID`, consumidores procesan envío y documentos |

## Notas

- El checkout en la app se confirma con un aviso transitorio; el resultado real
  queda registrado en `10-async-consumers.png` (logs) y en la orden `PAID`.
- El "Panel Admin" del front depende de `admin-service` (`:8085`), que no se
  desplegó en esta instancia por límite de memoria; no afecta al flujo RabbitMQ.
