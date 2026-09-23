# Proyecto: PerfumerIA - E-commerce de Fragancias

Este repositorio contiene la arquitectura Full Stack de **PerfumerIA**, una tienda virtual de perfumes de lujo, implementando microservicios y un patrón BFF (Backend for Frontend).

---

## 1. Análisis de la Solución (PerfumerIA)

La plataforma permite a los usuarios navegar por un catálogo de perfumes exclusivos, gestionar sus perfiles y administrar la plataforma según su rol.

### Componentes del Negocio:
- **Frontend (web-app)**: Interfaz de usuario minimalista y profesional para la compra de perfumes.
- **BFF (Backend For Frontend)**: Puerta de enlace para la web, orquestando datos de catálogo y pedidos.
- **MS Autenticación (auth-service)**: Gestiona el registro, inicio de sesión y roles (CLIENT, ADMIN, EXECUTIVE) con persistencia en PostgreSQL (Supabase).
- **MS Perfil (profile-service)**: Gestiona la información detallada de los perfiles de usuario.
- **MS Administración (admin-service)**: Panel de control restringido para la gestión de ventas y usuarios.
- **MS Catálogo (microservice-1)**: Gestiona el inventario de fragancias premium.
- **MS Pedidos (microservice-2)**: Gestiona la simulación de pagos y órdenes de compra (utiliza patrón Factory para métodos de pago).
- **MS Envíos (shipping-service)**: Gestiona envíos con patrón Circuit Breaker y Factory para transportistas.

---

## 2. Patrones y Estrategias

- **BFF Pattern**: Optimización de la API para el cliente web.
- **Microservices Architecture**: Separación clara de dominios y responsabilidades.
- **Role-Based Access Control (RBAC)**: Visibilidad condicional y acceso restringido basado en roles de usuario.
- **Factory Pattern**: Para métodos de pago y transportistas.
- **Circuit Breaker Pattern**: Para manejo de fallos en shipping-service.
- **Estrategia de Branching**: Gitflow para el control de versiones.

---

## 3. Arquitectura de Puertos

### Backend
| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| BFF | 8080 | Orquestador API para frontend |
| MS Catálogo | 8081 | Gestión de inventario de perfumes |
| MS Pedidos | 8082 | Gestión de órdenes y pagos |
| MS Auth | 8083 | Autenticación y autorización |
| MS Perfil | 8084 | Gestión de perfiles de usuario |
| MS Admin | 8085 | Panel de administración |
| MS Envíos | 8086 | Gestión de envíos (Resilience4j) |

### Frontend
- **Web App**: `http://localhost:5173` (Vite default)

---

## 4. Tecnologías Principales

### Frontend
- React 18
- Vite 5
- NPM

### Backend
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- Maven
- PostgreSQL (Supabase, DB principal)
- H2 Database (solo para tests)
- Resilience4j (Circuit Breaker para shipping-service)

---

## 5. Configuración sensible

Las credenciales y URLs de infraestructura no se almacenan en el repositorio.
Configúralas mediante variables de entorno antes de ejecutar los servicios.
Las credenciales de prueba se documentarán cuando se configure el proveedor
de identidad de la evaluación.

---

## 6. Instrucciones Rápidas de Ejecución

### Frontend
```bash
cd frontend/web-app
npm install
npm run dev
```

### Backend (cada microservicio)
```bash
cd backend/<nombre-servicio>
mvn spring-boot:run
```

---

## 7. Estructura del Proyecto

```
PerfumerIA/
├── frontend/
│   └── web-app/          # React + Vite SPA
├── backend/
│   ├── auth-service/     # Autenticación y Roles
│   ├── profile-service/  # Perfiles de Usuario
│   ├── admin-service/    # Panel de Admin
│   ├── bff/              # Backend for Frontend
│   ├── microservice-1/   # Catálogo de Perfumes
│   ├── microservice-2/   # Pedidos y Pagos
│   └── shipping-service/ # Envíos (Circuit Breaker)
└── archetypes/           # Arquetipos Maven
```

---

*PerfumerIA - Arte en fragancias. Plataforma desarrollada para Evaluación Full Stack.*
