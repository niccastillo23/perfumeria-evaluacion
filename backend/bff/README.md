# Backend For Frontend (BFF) - PerfumerIA

Este componente actúa como intermediario entre el frontend y los microservicios.
El frontend debe enviar un Access Token como `Authorization: Bearer <token>`.
El BFF valida el JWT antes de permitir el acceso a las rutas protegidas.

---

## Puerto
8080

---

## Ejecución
1. Asegúrese de tener JDK 17 y Maven.
2. Inicie el servicio:
   ```bash
    mvn spring-boot:run
   ```

## Variables de entorno

Antes de ejecutar el BFF con Microsoft Entra ID, configura:

```text
SECURITY_ENABLED=true
AZURE_ISSUER_URI=https://login.microsoftonline.com/<tenant-id>/v2.0
AZURE_API_AUDIENCE=api://<api-client-id>
CORS_ALLOWED_ORIGIN=http://localhost:5173
```

Mientras Microsoft Entra ID no esté disponible, `SECURITY_ENABLED` queda en
`false` únicamente para permitir el trabajo local. Esta configuración no debe
usarse en el entorno evaluado o desplegado.

Las URLs de los microservicios pueden sobrescribirse con `CATALOG_SERVICE_URL`,
`ORDERS_SERVICE_URL`, `PROFILE_SERVICE_URL` y `ADMIN_SERVICE_URL`.

## Rutas protegidas

- `GET /api/v1/shop/catalog` requiere `Catalog.Read`.
- `POST /api/v1/shop/checkout` requiere `Orders.Create`.
- `GET /api/v1/profile/{username}` requiere autenticación.
- `GET /api/v1/admin/stats` requiere `Admin.Read`.
- `GET /api/v1/admin/users` requiere `Admin.Read`.
- `GET /api/v1/health` es público para health checks.
