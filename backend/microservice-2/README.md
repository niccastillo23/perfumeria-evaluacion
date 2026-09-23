# Microservice 2 - Pedidos y Pagos

Microservicio encargado de la gestión de órdenes de compra y simulación de pagos, usando el **Factory Pattern** para métodos de pago.

---

## Puerto
8082

---

## Base de Datos
- Schema: `orders_schema`
- Tabla: `orders`

---

## Métodos de Pago Soportados
- `cash_on_delivery`: Pago contra entrega
- `credit_card`: Tarjeta de crédito
- `paypal`: PayPal

---

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/orders` | Crea una nueva orden |
| GET | `/api/orders/{id}` | Obtiene una orden por ID |
| GET | `/api/orders` | Obtiene todas las órdenes |
| GET | `/api/orders/health` | Verifica el estado de salud |

---

## Ejemplo de Cuerpo para Crear Orden
```json
{
  "items": [...],
  "total": 299.99,
  "payment": {
    "type": "credit_card",
    "cardNumber": "4111-1111-1111-1111"
  }
}
```

---

## Ejecución
1. Asegúrese de tener JDK 17 y Maven.
2. Inicie el servicio:
   ```bash
   mvn spring-boot:run
   ```
