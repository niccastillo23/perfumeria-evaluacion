# Patrones de Diseño y Arquetipos Maven - SmartLogix (PerfumerIA)

## 1. Introduccion

Este documento justifica tecnicamente la seleccion de patrones de diseño y arquetipos Maven implementados en la plataforma PerfumerIA, un eCommerce de fragancias de lujo desarrollado como parte de la Evaluacion Parcial 2 del caso SmartLogix.

---

## 2. Repository Pattern

### 2.1 Que es

El Repository Pattern es un patron de diseño que abstrae la logica de acceso a datos detras de una interfaz, desacoplando la capa de dominio de la capa de persistencia.

### 2.2 Implementacion en PerfumerIA

Se implementa en el **Microservicio de Inventario (microservice-1)** a traves de:

- `PerfumeRepository.java`: Interfaz que extiende `JpaRepository<Perfume, Long>`
- `PerfumeService.java`: Capa de servicio que consume el repositorio
- `Ms1Application.java`: Controller que delega en el servicio

```java
@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {
    List<Perfume> findByBrand(String brand);
    List<Perfume> findByPriceBetween(Double min, Double max);
    List<Perfume> findByNameContainingIgnoreCase(String name);
}
```

### 2.3 Problema que resuelve: Inconsistencia de Stock

En un sistema de eCommerce como SmartLogix, la inconsistencia de stock ocurre cuando multiples servicios acceden directamente a la base de datos sin una capa de abstraccion. Sin el Repository Pattern:

- **Acoplamiento directo**: Los controllers interactuan directamente con JDBC/Hibernate, mezclando logica de negocio con acceso a datos.
- **Dificultad para testear**: Sin una interfaz abstracta, las pruebas unitarias requieren una base de datos real.
- **Duplicacion de queries**: La misma consulta se replica en multiples lugares del codigo.

Con el Repository Pattern:

- **Desacoplamiento total**: La capa de dominio no conoce los detalles de persistencia. Si cambiamos de PostgreSQL a MongoDB, solo modificamos la implementacion del repositorio.
- **Testabilidad**: `PerfumeRepository` puede ser mockeado con Mockito, permitiendo pruebas unitarias sin base de datos.
- **Centralizacion de queries**: Todas las consultas de catalogo estan en un solo lugar, facilitando optimizaciones y cambios.

### 2.4 Beneficios para SmartLogix

| Beneficio | Impacto |
|---|---|
| Separacion de responsabilidades | El controller solo coordina, el repositorio solo persiste |
| Test unitario sin DB real | `@Mock PerfumeRepository` en tests con Mockito |
| Queries reutilizables | `findByBrand`, `findByPriceBetween` se usan desde cualquier servicio |
| Evolucion independiente | Se puede cambiar el ORM sin afectar la logica de negocio |

---

## 3. Factory Method Pattern

### 3.1 Que es

El Factory Method es un patron creacional que define una interfaz para crear objetos, pero permite a las subclases decidir que clase instanciar. Delega la instanciacion a metodos factory.

### 3.2 Implementacion en PerfumerIA

Se implementa en el **Microservicio de Pedidos (microservice-2)** para la gestion de metodos de pago:

- `PaymentMethod.java`: Interfaz que define el contrato de pago
- `CreditCardPayment.java`, `PayPalPayment.java`, `CashOnDeliveryPayment.java`: Implementaciones concretas
- `PaymentMethodFactory.java`: Factory que retorna la implementacion adecuada segun el tipo

```java
public class PaymentMethodFactory {
    private static final Map<String, PaymentMethod> registry = Map.of(
        "credit_card", new CreditCardPayment(),
        "paypal", new PayPalPayment(),
        "cash_on_delivery", new CashOnDeliveryPayment()
    );

    public static PaymentMethod getPaymentMethod(String type) {
        PaymentMethod method = registry.get(type.toLowerCase());
        if (method == null) {
            throw new IllegalArgumentException("Metodo de pago no soportado");
        }
        return method;
    }
}
```

### 3.3 Problema que resuelve: Fragilidad en Procesamiento de Pagos

En SmartLogix, el procesamiento de pagos es critico. Sin Factory Method:

- **Condicionales gigantes**: `if (type.equals("credit_card")) { ... } else if (type.equals("paypal")) { ... }` se repite en cada endpoint.
- **Violacion de Open/Closed**: Cada nuevo metodo de pago requiere modificar codigo existente en multiples archivos.
- **Falta de extensibilidad**: Agregar un nuevo metodo de pago (ej. MercadoPago) implica buscar y modificar todos los `if/else`.

Con Factory Method:

- **Principio Open/Closed**: Abierto a extension (nuevos PaymentMethod), cerrado a modificacion (la factory no cambia).
- **Responsabilidad unica**: Cada clase de pago encapsula su propia logica de validacion.
- **Registro centralizado**: La factory mantiene un registry claro de todos los metodos disponibles.

### 3.4 Beneficios para SmartLogix

| Beneficio | Impacto |
|---|---|
| Nuevo metodo de pago en 3 pasos | Crear clase, implementar interfaz, registrar en factory |
| Validacion encapsulada | CreditCard valida tarjeta, PayPal valida email, cada uno su logica |
| Sin condicionales dispersos | Un solo punto de decision en PaymentMethodFactory |
| Test unitario por metodo | Cada PaymentMethod tiene sus propios tests aislados |

---

## 4. Circuit Breaker Pattern

### 4.1 Que es

El Circuit Breaker es un patron de resiliencia que previene fallos en cascada cuando un servicio dependiente falla. Funciona como un interruptor electrico: cuando detecta fallos repetidos, "abre el circuito" y devuelve respuestas fallback sin intentar llamar al servicio caido.

### 4.2 Implementacion en PerfumerIA

Se implementa en el **Microservicio de Envios (shipping-service)** usando **Resilience4j**:

- `CarrierService.java`: Interfaz para servicios de transportistas externos
- `FedexCarrierService.java`, `DhlCarrierService.java`, `UpsCarrierService.java`: Implementaciones
- `ShippingService.java`: Usa `@CircuitBreaker(name = "carrierApi", fallbackMethod = "createShipmentFallback")`
- `application.yml`: Configuracion de Resilience4j

```yaml
resilience4j:
  circuitbreaker:
    instances:
      carrierApi:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

```java
@CircuitBreaker(name = "carrierApi", fallbackMethod = "createShipmentFallback")
public Shipment createShipment(String orderId, String carrierName,
                               Double orderTotal, String destination) {
    CarrierService carrier = carrierFactory.getCarrier(carrierName);
    String result = carrier.requestShipment(orderId, orderTotal, destination);
    // ...
}

public Shipment createShipmentFallback(String orderId, String carrierName,
                                       Double orderTotal, String destination, Throwable t) {
    // Retorna envio con tracking "FALLBACK" sin costo
    return shipmentRepository.save(fallbackShipment);
}
```

### 4.3 Estados del Circuit Breaker

| Estado | Comportamiento |
|---|---|
| **CLOSED** | Operacion normal. Las llamadas al transportista se ejecutan normalmente. |
| **OPEN** | Se detectaron fallos >= 50% en las ultimas 10 llamadas. Las llamadas se redirigen inmediatamente al fallback sin intentar contactar al transportista. |
| **HALF-OPEN** | Tras 30 segundos en OPEN, se permiten 3 llamadas de prueba. Si exitan, vuelve a CLOSED. Si fallan, vuelve a OPEN. |

### 4.4 Problema que resuelve: Fallos en Cascada

En SmartLogix, los envios dependen de APIs externas de transportistas (FedEx, DHL, UPS). Sin Circuit Breaker:

- **Timeouts infinitos**: Si la API de FedEx tarda 30 segundos en responder, cada request del usuario bloquea un hilo durante 30s.
- **Efecto dominó**: Si 100 usuarios hacen checkout simultaneo y FedEx esta caido, 100 hilos se bloquean, agotando el thread pool del servidor.
- **Degradacion total**: El microservicio de pedidos depende del de envios. Si envios falla, pedidos tambien falla.

Con Circuit Breaker:

- **Fail-fast**: Despues de detectar fallos, responde inmediatamente con fallback sin esperar timeouts.
- **Aislamiento**: El fallo de un transportista no afecta a los demas ni al resto del sistema.
- **Recuperacion automatica**: El sistema vuelve a intentar la conexion periodicamente (cada 30s).
- **Experiencia de usuario**: El usuario recibe confirmacion de envio (aunque sea fallback) en lugar de un error 500.

### 4.5 Beneficios para SmartLogix

| Beneficio | Impacto |
|---|---|
| Sin fallos en cascada | La caida de FedEx no tumba todo el sistema de envios |
| Respuesta inmediata | Fallback responde en ms, no en segundos de timeout |
| Auto-recuperacion | Vuelve a intentar automaticamente cada 30s |
| Monitoreo | Estado del circuit breaker expuesto via `/api/shipping/circuit-breaker/state` |

---

## 5. Arquetipos Maven

### 5.1 Que es un Arquetipo Maven

Un arquetipo Maven es un template o plantilla de proyecto que permite generar nuevos proyectos con una estructura estandarizada, dependencias preconfiguradas y convenciones establecidas.

### 5.2 Implementacion en PerfumerIA

Se creo el arquetipo `spring-boot-microservice-archetype` en la carpeta `archetypes/`:

```
archetypes/spring-boot-microservice-archetype/
├── pom.xml
├── README.md
└── src/main/resources/
    ├── META-INF/maven/archetype-metadata.xml
    └── archetype-resources/
        ├── pom.xml
        └── src/main/java/App.java
```

### 5.3 Problema que resuelve: Inconsistencia entre Microservicios

Sin arquetipos Maven:

- **Estructuras inconsistentes**: Cada desarrollador crea los microservicios con diferente estructura de paquetes.
- **Configuracion duplicada**: Las dependencias de Spring Boot, JPA, PostgreSQL se copian manualmente en cada pom.xml.
- **Errores de configuracion**: Un pom.xml mal copiado puede tener versiones incompatibles.

Con arquetipos Maven:

- **Estandarizacion**: Todos los microservicios generados tienen la misma estructura base.
- **Configuracion centralizada**: Las dependencias y versiones se definen una vez en el arquetipo.
- **Velocidad**: `mvn archetype:generate` crea un nuevo microservicio en segundos.

### 5.4 Beneficios para SmartLogix

| Beneficio | Impacto |
|---|---|
| Consistencia | Todos los MS siguen la misma estructura de paquetes |
| Rapidez | Nuevo MS en un comando, no en 30 minutos de configuracion |
| Calidad | Dependencias verificadas y versiones compatibles por defecto |
| Onboarding | Nuevos desarrolladores generan proyectos sin errores de configuracion |

---

## 6. Resumen de Patrones y Problemas Resueltos

| Patron | Microservicio | Problema Resuelto |
|---|---|---|
| Repository | Inventario (MS1) | Inconsistencia de stock, acoplamiento a DB |
| Factory Method | Pedidos (MS2) | Fragilidad en procesamiento de pagos |
| Circuit Breaker | Envios (shipping-service) | Fallos en cascada por APIs de transportistas |
| Maven Archetype | Todos los MS | Inconsistencia estructural entre servicios |

---

## 7. Arquitectura General

```
                    ┌─────────────────┐
                    │   Frontend      │
                    │   React/Vite    │
                    │   :5173         │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │      BFF        │
                    │   Spring Boot   │
                    │   :8080         │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
 ┌────────▼───────┐ ┌───────▼───────┐ ┌────────▼────────┐
 │  Inventario    │ │   Pedidos     │ │    Envios       │
 │  :8081         │ │   :8082       │ │    :8086        │
 │ Repository     │ │ Factory Method│ │ Circuit Breaker │
 └────────┬───────┘ └───────┬───────┘ └────────┬────────┘
          │                  │                  │
 ┌────────▼──────────────────▼──────────────────▼────────┐
 │                 Supabase PostgreSQL                    │
 │         catalog_schema | orders_schema | shipping_schema │
 └────────────────────────────────────────────────────────┘
```

---

*Documento generado para la Evaluacion Parcial 2 - SmartLogix*
