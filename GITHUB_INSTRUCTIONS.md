# Instrucciones para Versionamiento en GitHub

Para subir este proyecto a GitHub, siga estos pasos:

## Opción 1: Monorepo (Un solo repositorio para todo)

1. Cree un repositorio vacío en GitHub (ej. `Evaluacion_2_FullStack`).
2. En la raíz de este proyecto local, ejecute:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Proyecto completo Evaluación 2"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/Evaluacion_2_FullStack.git
   git push -u origin main
   ```

## Opción 2: Repositorios Independientes (Recomendado para microservicios)

Si prefiere tener repositorios separados, debe inicializar git en cada subcarpeta:

### Frontend
```bash
cd frontend/web-app
git init
git add .
git commit -m "Initial commit: Frontend Component"
# ... vincular a repo de GitHub ...
```

### Backend (BFF y Microservicios)
Repita el proceso para cada carpeta en `backend/` y para el arquetipo en `archetypes/`.

## Notas Adicionales
- Asegúrese de tener configurado un archivo `.gitignore` adecuado (se incluye uno en la raíz en el siguiente paso).
- Siga el [Plan de Branching](./docs/Plan_de_Branching.md) para las futuras contribuciones.
