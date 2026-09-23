# Auth Service - PerfumerIA

Este microservicio gestiona el registro e inicio de sesión de usuarios, utilizando **PostgreSQL en Supabase** como base de datos.

---

## Puerto
8083

---

## Base de Datos
- Schema: `auth_schema`
- Tabla: `users`

---

## Endpoints

### 1. Registro de Usuario
- **URL**: `POST /api/auth/register`
- **Cuerpo (JSON)**:
```json
{
  "username": "usuario1",
  "password": "mi_password",
  "email": "usuario1@ejemplo.com",
  "role": "CLIENT"
}
```
- Si `role` no se especifica, se usa `CLIENT` por defecto.

### 2. Inicio de Sesión
- **URL**: `POST /api/auth/login`
- **Cuerpo (JSON)**:
```json
{
  "username": "usuario1",
  "password": "mi_password"
}
```

### 3. Listar Usuarios
- **URL**: `GET /api/auth/users`

### 4. Salud del Servicio
- **URL**: `GET /api/auth/health`

---

## Identidad

Las credenciales y roles de prueba se configurarán mediante el proveedor de
identidad de la evaluación y no se almacenan en este repositorio.

---

## Ejecución
1. Asegúrese de tener JDK 17 y Maven instalados.
2. Inicie el servicio:
   ```bash
   mvn spring-boot:run
   ```
