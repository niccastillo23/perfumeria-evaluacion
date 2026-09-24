# PerfumerIA - Evaluacion Parcial 1

Proyecto full stack para la Evaluacion Parcial 1 de Desarrollo Cloud Native I.
La aplicacion mantiene React como frontend y utiliza microservicios Spring Boot
con un BFF protegido mediante Microsoft Entra ID y JWT, desplegados en la nube
detras de AWS API Gateway.

## Objetivo de la evaluacion

La implementacion demuestra:

- Login y logout con Microsoft Entra ID mediante MSAL React.
- Flujo OAuth 2.0/OpenID Connect Authorization Code con PKCE.
- Obtencion de un Access Token para la API protegida.
- Envio del token como `Authorization: Bearer <token>`.
- Validacion del JWT en Spring Security.
- Validacion de firma, issuer, audience, expiracion y scopes.
- Autorizacion de operaciones mediante scopes.
- Respuestas verificables `200`, `401` y `403`.
- Registro de usuarios en el tenant de Entra ID desde el BFF (Microsoft Graph).
- Despliegue cloud: AWS API Gateway + EC2 con frontend servido por nginx sobre
  HTTPS y backend Spring Boot.

## Arquitectura

```text
Navegador / React + MSAL
    |
    | HTTPS  (https://<frontend-host>)
    v
nginx (EC2) --> dist/ del frontend
    |
    | Authorization: Bearer JWT
    v
AWS API Gateway  (authorizer JWT + CORS)
    |                              \
    | rutas protegidas              \  POST /api/v1/auth/register (publica)
    v                                v
BFF Spring Boot :8080  <-- Microsoft Graph (alta de usuarios)
    |
    | Spring Security / OAuth2 Resource Server
    v
Microservicios Spring Boot (H2)
    |
    v
Catalogo :8081 | Pedidos :8082 | Perfil :8084 | Administracion :8085
```

El frontend no llama directamente a los microservicios. Las solicitudes pasan
por el API Gateway y luego por el BFF, que valida el token y delega la operacion
al servicio interno correspondiente.

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
   - Permiso de aplicacion `User.ReadWrite.All` de Microsoft Graph (para el
     registro de usuarios desde el BFF).
2. **PerfumerIA Frontend**
   - Plataforma SPA.
   - Redirect URI: `http://localhost:5173/` y el origen del frontend desplegado.
   - Permisos delegados para los scopes de la API.

### Roles de aplicacion

En **PerfumerIA API** se definen App Roles asignables a usuarios y grupos:

- `ADMIN`: acceso al panel de administracion.
- `CLIENT`: usuario final con acceso al catalogo y sus pedidos.
- `EXECUTIVE`: rol ejecutivo.

El rol viaja en el claim `roles` del token y el frontend lo usa para el control
de acceso basado en roles (RBAC).

### Registro de usuarios

En un tenant workforce los usuarios no se auto-registran. El alta se realiza
desde el BFF contra Microsoft Graph con un client secret que **solo existe en
el servidor** (nunca en React):

```text
Frontend "Crear cuenta"
    -> API Gateway (ruta publica POST /api/v1/auth/register)
    -> BFF -> Microsoft Graph -> usuario creado en el tenant
    -> el usuario inicia sesion con MSAL (rol CLIENT por defecto)
```

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
export AZURE_TENANT_ID=<tenant-id>
export AZURE_CLIENT_ID=<api-client-id>
export AZURE_CLIENT_SECRET=<client-secret-de-solo-servidor>
mvn spring-boot:run
```

`AZURE_TENANT_ID`, `AZURE_CLIENT_ID` y `AZURE_CLIENT_SECRET` habilitan el
registro de usuarios contra Microsoft Graph y nunca se versionan.

Las URLs de microservicios se pueden sobrescribir con:

```text
CATALOG_SERVICE_URL
ORDERS_SERVICE_URL
PROFILE_SERVICE_URL
ADMIN_SERVICE_URL
```

## Rutas del BFF

- `GET /api/v1/health`: publico para health checks.
- `POST /api/v1/auth/register`: publica, alta de usuarios en Entra ID via Graph.
- `GET /api/v1/shop/catalog`: requiere `Catalog.Read`.
- `POST /api/v1/shop/checkout`: requiere `Orders.Create`.
- `GET /api/v1/profile/{username}`: requiere autenticacion.
- `GET /api/v1/admin/stats`: requiere `Admin.Read`.
- `GET /api/v1/admin/users`: requiere `Admin.Read`.

## Despliegue cloud

La arquitectura cloud se compone de:

- **AWS API Gateway** con un authorizer JWT (valida issuer y audience) y CORS
  habilitado. Expone las rutas del BFF, incluida la ruta publica de registro.
- **Instancia EC2** que ejecuta:
  - el **BFF** en `:8080`;
  - los microservicios de catalogo (`:8081`), pedidos (`:8082`), perfil
    (`:8084`) y administracion (`:8085`) con base de datos H2;
- **nginx** como servidor del build de React (`dist/`) sobre HTTPS.
- **Certificado TLS** emitido por Let's Encrypt, con redireccion HTTP -> HTTPS.

Variables usadas por el frontend en el build de produccion:

```text
VITE_AZURE_CLIENT_ID=<spa-client-id>
VITE_AZURE_TENANT_ID=<tenant-id>
VITE_API_BASE_URL=https://<api-gateway-id>.execute-api.<region>.amazonaws.com
VITE_API_SCOPE=api://<api-client-id>/Catalog.Read
```

## Evidencia de seguridad

Con Entra ID y el API Gateway configurados se obtuvieron las respuestas
verificables a traves del API Gateway:

| Caso | Resultado |
|---|---:|
| Catalogo sin Bearer Token | `401` |
| Catalogo con token y `Catalog.Read` | `200` |
| Panel Admin con token sin `Admin.Read` | `403` |
| Preflight `OPTIONS` (CORS) | `204` |
| Registro de usuario (`POST /api/v1/auth/register`) | `201` |

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
