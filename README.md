# PerfumerIA - Evaluacion Parcial 1

Proyecto full stack para la Evaluacion Parcial 1 de Desarrollo Cloud Native I.
La aplicacion mantiene React como frontend y utiliza microservicios Spring Boot
con un BFF protegido mediante Microsoft Entra ID y JWT.

## Objetivo de la evaluacion

La implementacion debe demostrar:

- Login y logout con Microsoft Entra ID mediante MSAL React.
- Flujo OAuth 2.0/OpenID Connect Authorization Code con PKCE.
- Obtencion de un Access Token para la API protegida.
- Envio del token como `Authorization: Bearer <token>`.
- Validacion del JWT en Spring Security.
- Validacion de firma, issuer, audience, expiracion y scopes.
- Autorizacion de operaciones mediante scopes.
- Respuestas verificables `200`, `401` y `403`.

La configuracion de AWS API Gateway y el despliegue cloud corresponden a la
segunda etapa de la evaluacion.

## Arquitectura

```text
React + MSAL
    |
    | Authorization: Bearer JWT
    v
BFF Spring Boot :8080
    |
    | Spring Security / OAuth2 Resource Server
    v
Microservicios Spring Boot
    |
    v
PostgreSQL cloud
```

El frontend no llama directamente a los microservicios. Las solicitudes pasan
por el BFF, que valida el token y luego delega la operacion al servicio interno
correspondiente.

## Componentes

| Componente | Puerto | Responsabilidad |
|---|---:|---|
| Frontend React/Vite | 5173 | Interfaz, MSAL y consumo del BFF |
| BFF Spring Boot | 8080 | Seguridad, CORS y orquestacion |
| Catalogo | 8081 | Productos y perfumes |
| Pedidos | 8082 | Ordenes y pagos simulados |
| Auth legacy | 8083 | Servicio anterior, no usado para el login evaluado |
| Perfil | 8084 | Perfiles de usuario |
| Administracion | 8085 | Estadisticas y usuarios |
| Envios | 8086 | Envios, Factory y Circuit Breaker |

Microsoft Entra ID administra la identidad de los usuarios del flujo evaluado.
El `auth-service` existente se conserva como componente legacy, pero no debe
utilizarse como segundo sistema de autenticacion.

## Tecnologias

### Frontend

- React 18.
- Vite 5.
- `@azure/msal-browser`.
- `@azure/msal-react`.
- Vitest y Testing Library.

### Backend

- Java 17.
- Spring Boot 3.2.5.
- Spring Security.
- OAuth2 Resource Server.
- Spring Data JPA.
- Maven.
- PostgreSQL cloud.
- Resilience4j en `shipping-service`.

## Configuracion de Microsoft Entra ID

Se requieren dos registros de aplicacion:

1. **PerfumerIA API**
   - Application ID URI.
   - Scope `Catalog.Read`.
   - Scope `Orders.Create`.
   - Scope `Admin.Read`.
2. **PerfumerIA Frontend**
   - Plataforma SPA.
   - Redirect URI: `http://localhost:5173/`.
   - Permisos delegados para los scopes de la API.

No se debe crear ni usar un client secret en React. Los siguientes valores son
identificadores publicos, no secretos:

- Tenant ID.
- API client ID.
- SPA client ID.
- Application ID URI.

## Configuracion del frontend

Desde `frontend/web-app`:

```bash
npm install
cp .env.example .env
```

Completa `.env` con los valores reales de Entra ID:

```text
VITE_AZURE_CLIENT_ID=<spa-client-id>
VITE_AZURE_TENANT_ID=<tenant-id>
VITE_API_BASE_URL=http://localhost:8080
VITE_API_SCOPE=api://<api-client-id>/Catalog.Read
```

El archivo `.env` esta excluido por `.gitignore` y nunca debe publicarse.

Inicia el frontend con:

```bash
npm run dev
```

La aplicacion queda disponible en `http://localhost:5173/`.

## Configuracion del BFF

Desde `backend/bff` se puede ejecutar el modo local de scaffolding mientras no
existan los datos reales de Entra ID:

```bash
export SECURITY_ENABLED=false
mvn spring-boot:run
```

Este modo permite levantar el BFF localmente, pero **no cumple la validacion
final de la evaluacion**. Para activar la seguridad real se deben configurar:

```bash
export SECURITY_ENABLED=true
export AZURE_ISSUER_URI=https://login.microsoftonline.com/<tenant-id>/v2.0
export AZURE_API_AUDIENCE=api://<api-client-id>
export CORS_ALLOWED_ORIGIN=http://localhost:5173
mvn spring-boot:run
```

Las URLs de microservicios se pueden sobrescribir con:

```text
CATALOG_SERVICE_URL
ORDERS_SERVICE_URL
PROFILE_SERVICE_URL
ADMIN_SERVICE_URL
```

## Rutas del BFF

- `GET /api/v1/health`: publico para health checks.
- `GET /api/v1/shop/catalog`: requiere `Catalog.Read`.
- `POST /api/v1/shop/checkout`: requiere `Orders.Create`.
- `GET /api/v1/profile/{username}`: requiere autenticacion.
- `GET /api/v1/admin/stats`: requiere `Admin.Read`.
- `GET /api/v1/admin/users`: requiere `Admin.Read`.

## Pruebas

Frontend:

```bash
cd frontend/web-app
npm test -- --run
npm run build
```

BFF:

```bash
cd backend/bff
mvn test
```

Con Entra ID configurado, se deben comprobar estos casos:

| Caso | Resultado esperado |
|---|---:|
| Solicitud sin Bearer Token | 401 |
| Token valido con scope requerido | 200 |
| Token valido sin scope requerido | 403 |
| Token con audience incorrecta | 401 |
| Token expirado | 401 |

## Estructura

```text
PerfumerIA/
├── frontend/
│   └── web-app/
│       └── src/auth/          # Configuracion MSAL y cliente Bearer
├── backend/
│   ├── bff/                   # Resource Server y rutas protegidas
│   ├── microservice-1/        # Catalogo
│   ├── microservice-2/        # Pedidos
│   ├── auth-service/          # Componente legacy
│   ├── profile-service/       # Perfiles
│   ├── admin-service/         # Administracion
│   └── shipping-service/      # Envios
└── docs/
```

## Seguridad

- No se almacenan contrasenas, tokens ni client secrets en el repositorio.
- No se deben publicar archivos `.env`.
- Los valores de infraestructura se configuran mediante variables de entorno.
- `SECURITY_ENABLED=false` solo sirve para trabajo local inicial.
- La demostracion evaluada debe ejecutarse con `SECURITY_ENABLED=true` y un
  issuer, audience y scopes reales de Microsoft Entra ID.
