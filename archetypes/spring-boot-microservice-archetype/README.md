# Arquetipo Maven - Spring Boot Microservice

## Descripcion

Este arquetipo Maven permite generar nuevos microservicios Spring Boot con una estructura estandarizada, dependencias preconfiguradas y convenciones establecidas para el proyecto SmartLogix (PerfumerIA).

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior

## Instalacion del Arquetipo

### Paso 1: Instalar el arquetipo en el repositorio local

```bash
cd archetypes/spring-boot-microservice-archetype
mvn clean install
```

### Paso 2: Generar un nuevo microservicio

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.perfumeria \
  -DarchetypeArtifactId=spring-boot-microservice-archetype \
  -DarchetypeVersion=1.0.0-SNAPSHOT \
  -DgroupId=com.perfumeria \
  -DartifactId=nuevo-microservicio \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.perfumeria.nuevo
```

### Paso 3: Verificar la estructura generada

```bash
cd nuevo-microservicio
find . -type f | head -20
```

La estructura generada sera:

```
nuevo-microservicio/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── perfumeria/
                    └── nuevo/
                        └── App.java
```

## Estructura del Arquetipo

### `pom.xml` (del arquetipo)

Define las dependencias base que tendra todo microservicio generado:

- `spring-boot-starter-web`: Servidor web embebido (Tomcat)
- `spring-boot-starter-data-jpa`: Persistencia con JPA/Hibernate
- `postgresql`: Driver de PostgreSQL (Supabase)
- `spring-boot-starter-test`: Dependencias de testing (JUnit, Mockito)

### `archetype-resources/pom.xml`

Template del `pom.xml` que se genera en cada nuevo microservicio. Usa variables Maven (`${groupId}`, `${artifactId}`, etc.) que se reemplazan durante la generacion.

### `archetype-metadata.xml`

Define que archivos del arquetipo se incluyen en la generacion y como se procesan las variables.

## Personalizacion

Para modificar el arquetipo y que los nuevos microservicios incluyan dependencias adicionales:

1. Editar `archetype-resources/pom.xml` y agregar las dependencias deseadas.
2. Agregar nuevos archivos template en `archetype-resources/src/`.
3. Actualizar `archetype-metadata.xml` para incluir los nuevos archivos.
4. Reinstalar: `mvn clean install`

### Ejemplo: Agregar Resilience4j al arquetipo

Editar `archetype-resources/pom.xml`:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

## Uso en Equipo

Para compartir el arquetipo con el equipo:

### Opcion A: Repositorio Maven local compartido

```bash
# En la maquina del desarrollador que crea el arquetipo
mvn deploy -DaltDeploymentRepository=local::default::file:///shared/maven-repo

# En las maquinas del equipo, configurar settings.xml
# <mirror>
#   <id>shared-repo</id>
#   <url>file:///shared/maven-repo</url>
#   <mirrorOf>*</mirrorOf>
# </mirror>
```

### Opcion B: GitHub Packages

```bash
mvn deploy -DrepositoryId=github \
  -Durl=https://maven.pkg.github.com/usuario/repo
```

## Microservicios Generados con este Arquetipo

| Microservicio | Grupo | Artifact | Puerto |
|---|---|---|---|
| Inventario | com.evaluacion.backend | microservice-1 | 8081 |
| Pedidos | com.evaluacion.backend | microservice-2 | 8082 |
| Envios | com.perfumeria | shipping-service | 8086 |

---

*Arquetipo Maven para SmartLogix - Evaluacion Parcial 2*
