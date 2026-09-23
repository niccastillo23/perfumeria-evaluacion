# Plan de Branching - Estrategia Gitflow - SmartLogix (PerfumerIA)

## 1. Estrategia Principal: Gitflow

Se ha seleccionado **Gitflow** como la estrategia de branching para el proyecto SmartLogix (PerfumerIA) debido a su robustez para manejar ciclos de liberacion, mantenimiento de multiples versiones y trabajo paralelo de un equipo de desarrollo.

---

## 2. Ramas Principales

### `main`
- Contiene el codigo en estado de **produccion**.
- Cada commit en esta rama representa una version estable y desplegable.
- Solo se fusiona desde `release/` o `hotfix/`.
- Se etiqueta con version semantica (ej. `v1.0.0`, `v1.1.0`).

### `develop`
- Rama principal de **desarrollo**.
- Aqui se integran todas las nuevas funcionalidades terminadas.
- Es la fuente de verdad para el proximo release.
- Nunca se trabaja directamente sobre `develop`.

---

## 3. Ramas de Apoyo

### Ramas `feature/`
- Se crean a partir de `develop`.
- Se fusionan de vuelta a `develop` mediante Pull Request.
- Convencion: `feature/nombre-de-la-funcionalidad`

Ejemplos:
```
feature/catalogo-perfumes
feature/carrito-compras
feature/metodo-pago-paypal
feature/circuit-breaker-envios
```

### Ramas `release/`
- Se crean a partir de `develop` cuando se acerca un lanzamiento.
- Solo se permiten correcciones de bugs y tareas de release.
- Se fusionan en `main` y `develop`.
- Convencion: `release/vX.Y.Z`

Ejemplo:
```
release/v1.0.0
release/v1.1.0
```

### Ramas `hotfix/`
- Se crean a partir de `main` para corregir errores criticos en produccion.
- Se fusionan en `main` y `develop`.
- Convencion: `hotfix/descripcion-del-error`

Ejemplo:
```
hotfix/corregir-login-fallido
hotfix/fix-precio-cero
```

---

## 4. Flujo de Trabajo

```
main ────────────────────────────────────────────────────►
      \                         /
       \      hotfix/          /
        \─────►───────────────/
         \                   /
          \   release/v1.0  /
           \────►──────────/
            \             /
develop ─────\───────────/──────────────────────────────►
              \         /
 feature/A ────\───────/
                \     /
 feature/B ───────\──/
```

### Pasos:

1. Un desarrollador crea una rama `feature/` desde `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/nueva-funcionalidad
   ```

2. Desarrolla la funcionalidad con commits atomicos:
   ```bash
   git add .
   git commit -m "feat: agregar endpoint de busqueda por marca"
   ```

3. Al terminar, hace push y crea un Pull Request hacia `develop`:
   ```bash
   git push origin feature/nueva-funcionalidad
   ```

4. Tras revision de codigo y aprobacion, se fusiona en `develop`.

5. Para un release, se crea `release/vX.Y.Z` desde `develop`:
   ```bash
   git checkout -b release/v1.0.0 develop
   ```

6. Se realizan pruebas finales y correcciones en `release/`.

7. Se fusiona en `main` y `develop`, y se etiqueta:
   ```bash
   git checkout main
   git merge release/v1.0.0
   git tag -a v1.0.0 -m "Release v1.0.0 - PerfumerIA inicial"
   git push origin main --tags
   git checkout develop
   git merge release/v1.0.0
   ```

---

## 5. Ejemplo Realista de Resolucion de Conflicto (Merge Conflict)

### Contexto

Dos desarrolladores del equipo trabajan en paralelo sobre el archivo `Ms1Application.java` del microservicio de Inventario:

- **Desarrolladora Ana** (`feature/catalogo-filtros`): Agrega endpoints de filtrado por marca y rango de precios.
- **Desarrollador Bruno** (`feature/busqueda-nombre`): Agrega endpoint de busqueda por nombre de perfume.

### Estado inicial de `Ms1Application.java` en `develop`:

```java
package com.evaluacion.backend.ms1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class Ms1Application {

    @Autowired
    private PerfumeRepository perfumeRepository;

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }

    @GetMapping("/api/catalog")
    public List<Perfume> getCatalog() {
        return perfumeRepository.findAll();
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Catalog";
    }
}
```

### Ana modifica el archivo en su rama `feature/catalogo-filtros`:

```java
package com.evaluacion.backend.ms1;

import com.evaluacion.backend.ms1.service.PerfumeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class Ms1Application {

    private final PerfumeService perfumeService;

    public Ms1Application(PerfumeService perfumeService) {
        this.perfumeService = perfumeService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }

    @GetMapping("/catalog")
    public List<Perfume> getCatalog() {
        return perfumeService.getAllPerfumes();
    }

    @GetMapping("/catalog/brand/{brand}")
    public List<Perfume> getByBrand(@PathVariable String brand) {
        return perfumeService.getPerfumesByBrand(brand);
    }

    @GetMapping("/catalog/price-range")
    public List<Perfume> getByPriceRange(@RequestParam Double min, @RequestParam Double max) {
        return perfumeService.getPerfumesByPriceRange(min, max);
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Catalog";
    }
}
```

Ana fusiona su PR en `develop` exitosamente.

### Bruno modifica el archivo en su rama `feature/busqueda-nombre`:

```java
package com.evaluacion.backend.ms1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class Ms1Application {

    @Autowired
    private PerfumeRepository perfumeRepository;

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }

    @GetMapping("/api/catalog")
    public List<Perfume> getCatalog() {
        return perfumeRepository.findAll();
    }

    @GetMapping("/api/catalog/search")
    public List<Perfume> searchByName(@RequestParam String name) {
        return perfumeRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Catalog";
    }
}
```

### El Conflicto

Cuando Bruno intenta hacer merge de su PR hacia `develop`, Git detecta un conflicto porque ambos modificaron las mismas lineas de `Ms1Application.java`:

```
Auto-merging Ms1Application.java
CONFLICT (content): Merge conflict in Ms1Application.java
Automatic merge failed; fix conflicts and then commit the result.
```

El archivo queda con marcadores de conflicto:

```java
<<<<<<< HEAD
import com.evaluacion.backend.ms1.service.PerfumeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class Ms1Application {

    private final PerfumeService perfumeService;

    public Ms1Application(PerfumeService perfumeService) {
        this.perfumeService = perfumeService;
    }
=======
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class Ms1Application {

    @Autowired
    private PerfumeRepository perfumeRepository;
>>>>>>> feature/busqueda-nombre
```

### Resolucion del Conflicto

**Paso 1: Analizar ambos cambios**

Bruno revisa que Ana introdujo:
- Inyeccion por constructor (mejor practica que `@Autowired` en campo)
- Capa de servicio `PerfumeService` (Repository Pattern)
- `@RequestMapping("/api")` a nivel de clase
- Endpoints de filtro por marca y rango de precios

Y el propio Bruno quiere agregar:
- Endpoint de busqueda por nombre

**Paso 2: Resolver manteniendo lo mejor de ambos**

Bruno decide que la arquitectura de Ana (inyeccion por constructor + service layer) es superior. Resuelve el conflicto combinando ambos aportes:

```java
package com.evaluacion.backend.ms1;

import com.evaluacion.backend.ms1.service.PerfumeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class Ms1Application {

    private final PerfumeService perfumeService;

    public Ms1Application(PerfumeService perfumeService) {
        this.perfumeService = perfumeService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }

    @GetMapping("/catalog")
    public List<Perfume> getCatalog() {
        return perfumeService.getAllPerfumes();
    }

    @GetMapping("/catalog/brand/{brand}")
    public List<Perfume> getByBrand(@PathVariable String brand) {
        return perfumeService.getPerfumesByBrand(brand);
    }

    @GetMapping("/catalog/price-range")
    public List<Perfume> getByPriceRange(@RequestParam Double min, @RequestParam Double max) {
        return perfumeService.getPerfumesByPriceRange(min, max);
    }

    @GetMapping("/catalog/search")
    public List<Perfume> searchByName(@RequestParam String name) {
        return perfumeService.searchPerfumesByName(name);
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Catalog";
    }
}
```

**Paso 3: Eliminar marcadores y commitear**

```bash
git add Ms1Application.java
git commit -m "feat: resolver merge conflict - integrar busqueda por nombre con service layer"
```

**Paso 4: Verificar que compila y los tests pasan**

```bash
cd backend/microservice-1
mvn clean test
```

**Paso 5: Push y completar el PR**

```bash
git push origin feature/busqueda-nombre
```

### Lecciones Aprendidas

1. **Comunicacion**: Ana y Bruno deberian haber coordinado antes de modificar el mismo archivo.
2. **Commits atomicos**: Si Bruno hubiera creado un controller separado (`CatalogSearchController.java`) en lugar de modificar `Ms1Application.java`, el conflicto se habria evitado.
3. **Integracion continua**: Hacer merge de `develop` en la feature branch frecuentemente (`git merge develop`) detecta conflictos temprano.
4. **Service layer**: La decision de Ana de introducir un service layer fue la correcta arquitectura. Bruno la adopto en la resolucion.

---

## 6. Convenciones de Commits

Se utiliza **Conventional Commits**:

| Tipo | Descripcion | Ejemplo |
|---|---|---|
| `feat` | Nueva funcionalidad | `feat: agregar endpoint de busqueda por nombre` |
| `fix` | Correccion de bug | `fix: corregir calculo de total en carrito` |
| `refactor` | Refactorizacion | `refactor: extraer service layer en inventario` |
| `test` | Tests | `test: agregar tests para PaymentMethodFactory` |
| `docs` | Documentacion | `docs: actualizar README con instrucciones de deploy` |
| `chore` | Tareas de mantenimiento | `chore: actualizar version de Spring Boot` |

---

## 7. Proteccion de Ramas

| Rama | Proteccion |
|---|---|
| `main` | Requiere PR aprobado, sin push directo, requiere CI verde |
| `develop` | Requiere PR aprobado, sin push directo |
| `release/*` | Solo tech leads pueden fusionar |
| `hotfix/*` | Requiere aprobacion rapida, CI verde obligatorio |

---

*Documento generado para la Evaluacion Parcial 2 - SmartLogix*
