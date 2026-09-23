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

## Integración con Backend

La app se conecta a los siguientes microservicios:
- **Auth Service**: `http://localhost:8083` - Para login/registro
- **Catalog Service**: `http://localhost:8081` - Para catálogo de perfumes
- **Profile Service**: `http://localhost:8084` - Para perfiles de usuario
- **Admin Service**: `http://localhost:8085` - Para panel de administración

Si los microservicios no están disponibles, la app usa datos de mock para seguir funcionando.
