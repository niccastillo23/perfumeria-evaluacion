# Frontend Web App

Este es el componente frontend de la aplicación, desarrollado con React y Vite siguiendo el estándar NPM.

---

## Características Principales
- Single Page Application (SPA) con React 18
- Vite para construcción y servidor de desarrollo rápido
- Carrito de compras funcional con cálculo de total
- Inicio de sesión y registro de usuarios
- Vistas por roles (CLIENT, ADMIN, EXECUTIVE)
- Catálogo con imágenes únicas y relevantes por producto
- Vista de perfil de usuario
- Panel de administración

---

## Instalación y Ejecución

1. Asegúrese de tener instalado Node.js (v18+ recomendado).
2. Entre al directorio del proyecto:
   ```bash
   cd frontend/web-app
   ```
3. Instale las dependencias:
   ```bash
   npm install
   ```
4. Inicie el servidor de desarrollo:
   ```bash
   npm run dev
   ```
   La app estará disponible en `http://localhost:5173`

---

## Scripts Disponibles
| Script | Descripción |
|--------|-------------|
| `npm run dev` | Inicia el servidor de desarrollo |
| `npm run build` | Genera la versión de producción |
| `npm run preview` | Previsualiza la versión de producción localmente |
| `npm run lint` | Ejecuta el linter para verificar la calidad del código |
| `npm run test` | Ejecuta los tests con Vitest |
| `npm run test:watch` | Ejecuta los tests en modo watch |

---

## Estructura de Archivos
```
web-app/
├── public/             # Archivos estáticos (logo, etc.)
├── src/
│   ├── App.jsx         # Componente principal (toda la lógica)
│   ├── App.css         # Estilos globales
│   ├── main.jsx        # Punto de entrada
│   └── test/           # Tests
├── index.html
├── package.json
└── vite.config.js
```

---

## Configuración (.env)

Copia `.env.example` a `.env` y completa los valores públicos de Entra ID:

```text
VITE_AZURE_CLIENT_ID=<spa-client-id>
VITE_AZURE_TENANT_ID=<tenant-id>
VITE_API_BASE_URL=http://localhost:8080
VITE_API_SCOPE=api://<api-client-id>/Catalog.Read
```

- En desarrollo, `VITE_API_BASE_URL` apunta al BFF (`http://localhost:8080`).
- En producción apunta al API Gateway
  (`https://<api-gateway-id>.execute-api.<region>.amazonaws.com`).

El archivo `.env` está excluido por `.gitignore` y nunca debe publicarse.

## Autenticación

- Login/logout con Microsoft Entra ID usando `@azure/msal-react`
  (Authorization Code + PKCE).
- El Access Token se envía al BFF como `Authorization: Bearer <token>`.
- Formulario "Crear cuenta" que llama a `POST /api/v1/auth/register` en el BFF.
- Navegación por roles (RBAC) según el claim `roles` del token
  (`ADMIN`, `CLIENT`, `EXECUTIVE`).

## Integración con Backend

La app no llama directamente a los microservicios. Todas las solicitudes pasan
por el **BFF** (local) o por el **API Gateway** (desplegado), que valida el JWT
y delega a los servicios internos:

- `GET /api/v1/shop/catalog` - catálogo de perfumes (`Catalog.Read`)
- `POST /api/v1/shop/checkout` - pedidos (`Orders.Create`)
- `GET /api/v1/profile/{username}` - perfil (autenticado)
- `GET /api/v1/admin/stats`, `GET /api/v1/admin/users` - panel (`Admin.Read`)

Si el backend no está disponible, la app usa datos de mock para seguir funcionando.
