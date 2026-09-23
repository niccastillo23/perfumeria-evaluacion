# Plan de Branching - Estrategia Gitflow

Este documento detalla la estrategia de branching utilizada para el desarrollo del proyecto "Evaluación 2 - Full Stack".

## 1. Estrategia Principal: Gitflow

Se ha seleccionado **Gitflow** como la estrategia de branching debido a su robustez para manejar ciclos de liberación y mantenimiento de múltiples versiones.

### 2. Ramas Principales

- **main**: Contiene el código en estado de producción. Cada commit en esta rama debe ser una versión estable.
- **develop**: Rama principal de desarrollo. Aquí se integran todas las nuevas funcionalidades terminadas.

### 3. Ramas de Apoyo

- **feature/**: Ramas creadas a partir de `develop` para el desarrollo de nuevas funcionalidades.
  - *Convención*: `feature/nombre-de-la-funcionalidad`
- **release/**: Ramas creadas a partir de `develop` cuando se acerca un lanzamiento a producción. Solo se permiten correcciones de errores y tareas relacionadas con el lanzamiento.
  - *Convención*: `release/vX.Y.Z`
- **hotfix/**: Ramas creadas a partir de `main` para corregir errores críticos en producción de forma inmediata.
  - *Convención*: `hotfix/descripcion-del-error`

## 4. Flujo de Trabajo

1. Un desarrollador crea una rama `feature/` desde `develop`.
2. Una vez terminada la funcionalidad, se realiza un **Pull Request (PR)** hacia `develop`.
3. Tras la revisión y pruebas, se fusiona en `develop`.
4. Cuando se decide realizar un lanzamiento, se crea una rama `release/` desde `develop`.
5. Se realizan las pruebas finales en `release/` y se fusiona tanto en `main` como en `develop`.
6. Se etiqueta (tag) el commit en `main` con la versión correspondiente.

## 5. Gestión de Repositorios en GitHub

Cada componente (Frontend, BFF, Microservicios, Arquetipos) puede residir en su propio repositorio o en un monorepo, siguiendo esta misma estrategia de branching para mantener la consistencia.
