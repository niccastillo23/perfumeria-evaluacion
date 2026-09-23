# Componentes Backend - PerfumerIA

Esta carpeta contiene los componentes backend de **PerfumerIA**, todos generados a partir del arquetipo Maven personalizado.

---

## Componentes
| Servicio | Puerto | Paquete | Descripción |
|----------|--------|---------|-------------|
| [Auth Service](./auth-service) | 8083 | com.perfumeria.auth | Gestión de usuarios y roles (CLIENT, ADMIN, EXECUTIVE) |
| [Profile Service](./profile-service) | 8084 | com.perfumeria.profile | Información detallada del perfil del usuario |
| [Admin Service](./admin-service) | 8085 | com.perfumeria.admin | Operaciones administrativas y estadísticas |
| [BFF (Backend For Frontend)](./bff) | 8080 | com.evaluacion.backend.bff | Orquestador principal de la API para frontend |
| [Microservice 1](./microservice-1) | 8081 | com.evaluacion.backend.ms1 | Catálogo de fragancias (productos) |
| [Microservice 2](./microservice-2) | 8082 | com.evaluacion.backend.ms2 | Gestión de pedidos y pagos (Factory Pattern) |
| [Shipping Service](./shipping-service) | 8086 | com.perfumeria.shipping | Gestión de envíos (Circuit Breaker con Resilience4j) |

---

## Estándar
Todos los componentes utilizan:
- Java 17
- Spring Boot 3.2.5
- Maven para la gestión de dependencias
- Spring Data JPA
- PostgreSQL (Supabase) como base de datos principal
- H2 Database para tests (solo en scope test)
- CORS habilitado para desarrollo

---

## Base de Datos

El proyecto utiliza **PostgreSQL en Supabase** con schemas separados por servicio:
- `auth_schema`: Usuarios y credenciales
- `catalog_schema`: Perfumes y catálogo
- `orders_schema`: Pedidos y transacciones
- `profile_schema`: Perfiles de usuario
- `shipping_schema`: Envíos

---

## Instrucciones de Ejecución

1. **Asegúrate de tener Java 17 y Maven instalados**
2. **Navega a la carpeta del servicio que quieras ejecutar**:
   ```bash
   cd auth-service
   ```
3. **Ejecuta el servicio con Maven**:
   ```bash
   mvn spring-boot:run
   ```

---

## Identidad

Los usuarios y roles se configurarán mediante el proveedor de identidad de la
evaluación. No se almacenan contraseñas de prueba en el repositorio.
