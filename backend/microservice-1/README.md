# Microservice 1 - Catálogo de Perfumes

Microservicio encargado de la gestión del catálogo de fragancias.

---

## Puerto
8081

---

## Base de Datos
- Schema: `catalog_schema`
- Tabla: `perfume`

---

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/catalog` | Obtiene todo el catálogo |
| GET | `/api/catalog/{id}` | Obtiene un perfume por ID |
| GET | `/api/catalog/brand/{brand}` | Busca perfumes por marca |
| GET | `/api/catalog/search?name=...` | Busca perfumes por nombre |
| GET | `/api/catalog/price-range?min=...&max=...` | Busca por rango de precio |
| POST | `/api/catalog` | Crea un nuevo perfume |
| DELETE | `/api/catalog/{id}` | Elimina un perfume |
| GET | `/api/health` | Verifica el estado de salud |

---

## Ejecución
1. Asegúrese de tener JDK 17 y Maven.
2. Inicie el servicio:
   ```bash
   mvn spring-boot:run
   ```
