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
| Nombre completo | Juan Diego Valderrama Gaviria |
| Grupo | Grupo 1 |
| Profesor agregado al repositorio | si |

## 2. Descripción del proyecto
Servicio REST base del Parcial Práctico del segundo tercio de DOSW. Está construido con Spring Boot y MongoDB siguiendo una arquitectura MVC por capas (controller, core, persistence).
Por ahora solo expone un endpoint de salud y la documentación Swagger; la lógica de negocio, la seguridad con JWT y los roles se implementan durante el laboratorio.

## 3. Despliegue
| Recurso | URL |
|---|---|
| Servicio en Azure | https://dosw-parcialt2-juanvalderrama.azurewebsites.net |
| Health | https://dosw-parcialt2-juanvalderrama.azurewebsites.net/api/v1/health |
| Swagger | https://dosw-parcialt2-juanvalderrama.azurewebsites.net/swagger-ui/index.html |

![Health en Azure](docs/images/azure-health.png)

![Swagger en Azure](docs/images/azure-swagger.png)

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

![Pruebas en verde](docs/images/tests.png)
![Reporte Jacoco](docs/images/jacoco.png)

## 9. Docker
```bash
docker build -t dosw-parcial:local .
docker run -p 8080:8080 -e JWT_SECRET=<secret_base64> dosw-parcial:local
```
![Contenedor corriendo](docs/images/docker.png)

## 10. CI/CD
Workflow en `.github/workflows/ci-cd.yml`, se ejecuta con cada push a `main` y `develop`:
1. **test**: compila y ejecuta `mvn verify` (pruebas + Jacoco).
2. **docker**: construye la imagen y la publica en Docker Hub como `<usuario>/dosw-parcial-t2:latest`.
3. **deploy**: despliega la imagen en Azure App Service.

Secrets usados: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `AZURE_CREDENTIALS`, `AZURE_WEBAPP_NAME`, `JWT_SECRET`.

![Workflow en verde](docs/images/pipeline.png)

## 11. Herramientas de modelado y diseño (evidencias)
![Cuenta Draw.io](docs/images/modelado.png)
![Cuenta Figma](docs/images/figma.png)














## PUNTO 3
**Propuesta:** usar ambos. Embebidos para datos pequeños que se consultan con su padre y cambian con él; referenciados para entidades compartidas, consultadas independientemente o que crecen con el tiempo. 

| Documento | Modelo Recomendado | Justificacion                                                                   |
|---------|--------------------|---------------------------------------------------------------------------------|
| Macotas |Documento mascota con compatibilidad embebida y refugioId referenciado | Compatibilidad viaja con la mascota; refugio es compartido por muchas mascotas  |
| Nodos de jerarquía | Colección nodos con tipo y parentId | Permite recorrer RED→CIUDAD→REFUGIO y cambiar nodos sin reescribir todos los hijos |
| Solicitudes adopción | Documento separado con mascotaId y adoptanteId referenciados; aplazamientos contador embebido | Permite consultar por usuario, estado, fecha; crece sin inflar Mascota |
| Usuarios | Colección separada con email, passwordHash y rol | Acceso y permisos propios; contraseñas nunca embebidas en mascota/solicitud |

**Ventaja:** Los documentos pequeños como compatibilidad se recuperan con la mascota sin consultas adicionales; las referencias reducen duplicación y evitan que un documento crezca ilimitadamente por solicitudes históricas. 

**Desventaja:** Las referencias requieren consultas extra o agregaciones ($lookup); actualizar una adopción puede involucrar varios documentos y exigir medidas de consistencia. Embeber historial ilimitado inflaría los documentos y dificultaría las actualizaciones.


### Punto 6

**A)**

| Etapa | Propósito y acciones típicas |
| --- | --- |
| Trigger / checkout | Push o integración en develop; descargar versión exacta del commit. |
| Setup | Instalar Java 21 y preparar caché/dependencias Maven. |
| Build y pruebas | Compilar, ejecutar JUnit/Mockito y producir JaCoCo (>80 % según enunciado). |
| Calidad y seguridad | Análisis estático, validación de dependencias y rechazo de hallazgos graves. |
| Empaquetar imagen | Construir JAR y Dockerfile reproducible; etiquetar imagen con SHA. |
| Publicar artefacto | Autenticación segura y push a Docker Hub. |
| Deploy Azure | Desplegar la imagen exacta en App Service usando secretos. |
| Smoke / verificación | Probar endpoint de health/Swagger y caso básico; guardar evidencia y logs. |

**B)**

No. El pipeline debe interrumpirse y marcar la ejecución como fallida. En Actions el job de build/deploy depende de test (needs: test); si este falla, no se publica ni despliega esa versión. Esto evita distribuir un cambio que ya incumple un comportamiento automatizado. Solo un proceso de excepción documentado y aprobado podría alterar la política, no un ignore de fallos sin control.

**C)**

Permiten diagnosticar incidentes, seguir la operación entre capas y conocer qué regla rechazó una petición. En una adopción fallida registrar timestamp, correlationId/requestId, ruta, código HTTP, código de error, mascotaId, adoptanteId seudonimizado, regla de rechazo y tiempo de respuesta. En autenticación registrar fecha, resultado exitoso/fallido, identificador técnico de usuario si aplica, IP con medidas de privacidad, razón técnica controlada, intentos y correlationId. No registrar la contraseña ni el JWT.

**D)**

| Registrar | Nunca registrar |
| --- | --- |
| requestId, traceId, timestamp, método HTTP, ruta, status, latencia | Contraseñas, passwordHash BCrypt, JWT, refresh token, claves de firma. |
| mascotaId, solicitudId, adoptanteId interno cuando sea necesario | URI Mongo con usuario/contraseña, connection strings completas, secretos de Azure/GitHub. |
| errorCode, nombre de regla, transición de estados, identificador de servicio | Body completo del login, direcciones, teléfonos y datos personales innecesarios. |
| Versión de despliegue/commit, excepción en registros internos protegidos | Stack trace completo devuelto al cliente o tokens en URLs. |

**E)**

El log interno debe aportar información técnica suficiente para investigar sin exponer secretos y estar protegido por controles de acceso/retención. El cliente debe recibir un código HTTP y un mensaje estable, claro y breve, sin detalles de infraestructura ni stack trace. Ejemplo: log seguro “requestId=abc123, adopcion_rechazada, codigo=PET_INCOMPATIBLE, mascotaId=masc-013, status=422”; respuesta HTTP “422, code=PET_INCOMPATIBLE, message=La mascota no es compatible con la declaración”.