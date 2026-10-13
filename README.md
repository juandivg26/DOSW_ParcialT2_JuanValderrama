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

## SOLUCION DEL PARCIAL
### 1
Excel en la ruta : docs/Punto1Parcial.xlsx

(a). ¿Diferencia entre validación de entrada y de negocio? Clasifique tres reglas. 

Una validación de entrada revisa que los datos enviados tengan la forma, el tipo y los límites exigidos por el contrato y valida los DTOs con @Valid. Una validación de negocio decide si la operación es permitida según el estado del sistema y las reglas del caso.

| Regla del caso | Clasificación | Justificación |
| --- | --- | --- |
| Edad de 0 a 360 meses | Entrada | Rango numérico conocido sin consultar otros documentos | 
| El nodo de origen debe existir y ser REFUGIO | Negocio | Depende de la jerarquía y datos persistidos en MongoDB. |
| Un adoptante solo puede tener una solicitud activa | Negocio | Requiere conocer solicitudes actuales del adoptante y prevenir carreras. |

 También son reglas de negocio que la mascota esté DISPONIBLE y que su compatibilidad coincida con la declaración de la persona. 

(b). Si fallan varias condiciones al adoptar, ¿qué error responde la API y qué evita modificar? 

Debe definirse una prioridad en las validaciones; no debe depender del orden accidental de consultas o fallos. Propuesta 1 autenticación y rol (401/403); 2 formato DTO (400); 3 existencia de mascota (404); 4 estado DISPONIBLE y solicitud activa (409); 5 compatibilidad y fecha (422). Si un mismo paso detecta múltiples errores de entrada, se pueden devolver todos los campos inválidos en un 400. 

Cualquiera que sea el primer error de negocio reportado, no debe crearse la solicitud, ni modificarse mascota.estado, ni registrarse aplazamiento o actividad parcial.













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


## PUNTO 4

**A)**

**UBICACION Y EJEMPLOS:**

`AUTENTICACION:` Login valida email/BCrypt y emite JWT firmado. Un filtro comprueba token en peticiones protegidas. 

`AUTORIZACION:` ADMIN puede crear mascota; ADOPTANTE puede adoptar. Aplazar exige además ser titular de la solicitud. 

`INTEGRIDAD:` @Valid, invariantes del service, índices, actualizaciones atómicas; firma JWT detecta alteración del token. 

La autenticación inválida genera normalmente 401; un usuario autenticado sin permiso genera 403. La integridad no equivale a confidencialidad: esta última se protege además mediante HTTPS, control de acceso y manejo seguro de secretos. 

**B)**

**Riesgo 1 — A01: Broken Access Control (control de acceso roto)**

`Escenario:` un ADOPTANTE llama PATCH /api/v1/adopciones/{id}/recibimiento con el ID de la solicitud de otro adoptante; o llama POST /api/v1/mascotas para crear una mascota, función reservada a ADMIN.

**Mitigación.** Verificar roles con Spring Security y comprobar en el servicio la titularidad usando el usuario autenticado del JWT. No confiar en adoptanteId enviado desde el body. Aplicar denegación por defecto.

**Cómo probarlo.** Enviar petición sin token → 401; token ADOPTANTE a crear mascota → 403; token de adoptante A sobre solicitud de B → 403 y sin cambio en Mongo. Token ADMIN a creación válida → 201.

**Riesgo 2 — A07: Identification and Authentication Failures**

`Escenario:` contraseñas almacenadas en texto plano, login sin control ante intentos masivos, JWT aceptado sin validar su firma o expiración, o mensajes que revelan si un correo existe.

**Mitigación.** Guardar contraseñas con BCrypt/PasswordEncoder; JWT firmado y de vida limitada; JWT_SECRET privado, HTTPS, errores genéricos, validación del usuario activo, límites de intentos y monitoreo de accesos fallidos.

**Cómo probarlo.** Contraseña incorrecta → 401 sin distinguir usuario existente/no existente; token expirado o firma modificada → 401; inspección de Mongo para comprobar hash y no password en texto; nunca mostrar secreto ni token completo en logs. 

**C)**

`Propuesta:` autenticación con correo/contraseña y JWT Bearer mediante Spring Security, API stateless. Los usuarios ya están precargados en MongoDB, el login público valida la contraseña comparando el hash BCrypt y devuelve un token firmado; el cliente lo envía en Authorization: Bearer <token>. En cada operación la API valida token, expiración, usuario, rol y titularidad según corresponda.

`Justificación:` permite separar autenticación de las operaciones REST y probar fácilmente el acceso en Swagger Authorize, sin sesiones de servidor. No sustituye la seguridad de transporte HTTPS. Para un MVP académico resulta apropiado si se controla la expiración, el secreto, la autorización y la protección de credenciales.

`Enunciado contradictorio:` la matriz dice “Iniciar sesión: sin token → 401”, pero el login necesita aceptar credenciales sin token previo. Debe corregirse esa fila en el contrato y justificarlo. 
