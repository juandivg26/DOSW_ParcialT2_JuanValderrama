# DOSW_ParcialT2_JuanValderrama

## Índice
1. [Datos del estudiante](#1-datos-del-estudiante)
2. [Descripción del proyecto](#2-descripción-del-proyecto)
3. [Despliegue](#3-despliegue)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Tecnologías y dependencias](#5-tecnologías-y-dependencias)
6. [Configuración y variables de entorno](#6-configuración-y-variables-de-entorno)
7. [Cómo ejecutar](#7-cómo-ejecutar)
8. [Pruebas](#8-pruebas)
9. [Docker](#9-docker)
10. [CI/CD](#10-cicd)
11. [Herramientas de modelado y diseño (evidencias)](#11-herramientas-de-modelado-y-diseño-evidencias)

## 1. Datos del estudiante
| Campo | Valor |
|---|---|
| Nombre completo | Juan Valderrama <!-- TODO: nombre completo --> |
| Grupo | TODO |
| Profesor agregado al repositorio | ✔ <!-- TODO: confirmar --> |

## 2. Descripción del proyecto
Servicio REST base del Parcial Práctico del segundo tercio de DOSW. Está construido con Spring Boot y MongoDB siguiendo una arquitectura MVC por capas (controller, core, persistence).
Por ahora solo expone un endpoint de salud y la documentación Swagger; la lógica de negocio, la seguridad con JWT y los roles se implementan durante el laboratorio.

## 3. Despliegue
| Recurso | URL |
|---|---|
| Servicio en Azure | `https://<nombre>.azurewebsites.net` <!-- TODO --> |
| Health | `https://<nombre>.azurewebsites.net/api/v1/health` |
| Swagger | `https://<nombre>.azurewebsites.net/swagger-ui/index.html` |

## 4. Estructura del proyecto
```
DOSW_ParcialT2_JuanValderrama/
├── docs/
│   ├── uml/                      # diagramas fuente
│   └── images/                   # exportes y evidencias del README
├── src/main/java/edu/dosw/parcial/
│   ├── controller/               # endpoints REST
│   │   ├── dtos/request|response # objetos de entrada/salida de la API
│   │   ├── mappers/              # Mapper IN: DTO ↔ Dominio
│   │   └── handlers/             # GlobalExceptionHandler
│   ├── core/
│   │   ├── services/             # lógica de negocio
│   │   ├── models/               # objetos de dominio
│   │   ├── validators/           # reglas de validación de negocio
│   │   └── utils/                # utilidades compartidas
│   ├── persistence/
│   │   ├── entities/             # clases @Document (MongoDB)
│   │   ├── repositories/         # MongoRepository
│   │   └── mappers/              # Mapper OUT: Dominio ↔ Document
│   ├── config/                   # SecurityConfig, SwaggerConfig
│   └── main/                     # clase @SpringBootApplication
├── src/main/resources/application.yaml
├── src/test/java/edu/dosw/parcial/{controller,core,persistence}
├── Dockerfile · .dockerignore
├── .github/workflows/ci-cd.yml
└── pom.xml
```

## 5. Tecnologías y dependencias
| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 3.5.6 | Framework base (Web, Validation, Security) |
| Spring Data MongoDB | (BOM Spring Boot) | Persistencia |
| JJWT | 0.12.6 | Generación/validación de tokens JWT |
| springdoc-openapi | 2.8.13 | Swagger UI |
| Lombok | (BOM Spring Boot) | Reducir boilerplate, `@Slf4j` para logs |
| MapStruct | 1.6.3 | Mappers DTO ↔ Dominio ↔ Document |
| JUnit 5 + Mockito | (spring-boot-starter-test) | Pruebas unitarias |
| Jacoco | 0.8.13 | Cobertura de pruebas |
| SonarQube (sonar-maven-plugin) | 5.1.0.4751 | Análisis estático |

## 6. Configuración y variables de entorno
| Variable | Descripción | Ejemplo |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión a MongoDB | `mongodb://localhost:27017/dosw_parcial` |
| `JWT_SECRET` | Secret JWT en Base64 (mín. 44 caracteres) | `<secret_base64>` |
| `PORT` | Puerto HTTP (por defecto 8080) | `8080` |
| `WEBSITES_PORT` | Solo Azure: puerto al que enruta el App Service | `8080` |

## 7. Cómo ejecutar
**Mongo local con Docker**
```bash
docker run -d -p 27017:27017 --name mongo-parcial mongo:7
```
**Local con Maven**
```bash
JWT_SECRET=<secret_base64> mvn spring-boot:run
```
**Local con Docker** — ver [sección 9](#9-docker).

Luego abrir `http://localhost:8080/api/v1/health` y `http://localhost:8080/swagger-ui/index.html`.

## 8. Pruebas
```bash
mvn test
```
Reporte Jacoco: `target/site/jacoco/index.html`.

<!-- TODO: ![Pruebas en verde](docs/images/tests.png) -->
<!-- TODO: ![Reporte Jacoco](docs/images/jacoco.png) -->

## 9. Docker
```bash
docker build -t dosw-parcial:local .
docker run -p 8080:8080 -e JWT_SECRET=<secret_base64> dosw-parcial:local
```
<!-- TODO: ![Contenedor corriendo](docs/images/docker.png) -->

## 10. CI/CD
Workflow en `.github/workflows/ci-cd.yml`, se ejecuta con cada push a `main` y `develop`:
1. **test**: compila y ejecuta `mvn verify` (pruebas + Jacoco).
2. **docker**: construye la imagen y la publica en Docker Hub como `<usuario>/dosw-parcial-t2:latest`.
3. **deploy**: despliega la imagen en Azure App Service.

Secrets usados: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `AZURE_CREDENTIALS`, `AZURE_WEBAPP_NAME`, `JWT_SECRET`.

<!-- TODO: ![Workflow en verde](docs/images/pipeline.png) -->

## 11. Herramientas de modelado y diseño (evidencias)
<!-- TODO: ![Cuenta Lucidchart/Draw.io/Miro](docs/images/modelado.png) -->
<!-- TODO: ![Cuenta Figma](docs/images/figma.png) -->
