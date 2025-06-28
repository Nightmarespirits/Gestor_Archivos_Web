# Sistema de Gestion de Archivos

## Descripción General

Este proyecto es una solución integral para la gestión documental y archivística del IESTPFFAA. Permite administrar inventarios, catálogos y registros de transferencia de documentos, así como la gestión de usuarios, permisos y actividades. Incluye funcionalidades de búsqueda avanzada, exportación a Excel/PDF y control de acceso basado en roles.

## Tecnologías Utilizadas

- **Backend:** Java 17, Spring Boot, Spring Security, JPA/Hibernate, MySQL, Apache POI (Excel), iText (PDF), JWT, OpenAPI/Swagger
- **Frontend:** Vue 3, Vite, Vuetify 3, Pinia, Vue Router, ExcelJS, date-fns
- **Otros:** Docker,  Lombok

## Estructura del Proyecto

- `api/`: Backend Java Spring Boot
- `ui/`: Frontend Vue 3 + Vuetify

## Características Principales

- Gestión de documentos, inventarios, catálogos y registros de transferencia
- Exportación de inventarios, catálogos y registros a Excel y PDF
- Búsqueda avanzada y filtrado por múltiples criterios
- Control de acceso por roles y permisos
- Gestión de usuarios y bitácora de actividades
- Interfaz moderna y responsiva

## Instalación y Ejecución

### Backend (API)

1. Configura la base de datos en `api/src/main/resources/application.properties` (por defecto usa MySQL):
   - Usuario, contraseña, URL, etc.
2. Desde la carpeta `api/` ejecuta:
   ```bash
   ./mvnw spring-boot:run
   ```
   o con Maven instalado:
   ```bash
   mvn spring-boot:run
   ```
3. La API estará disponible en `http://localhost:8080`.

### Frontend (UI)

1. Ve a la carpeta `ui/` y ejecuta:
   ```bash
   npm install
   npm run dev
   ```
2. Accede a la interfaz en `http://localhost:5173` (o el puerto que indique Vite).

## Documentación de la API

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Funcionalidades Destacadas

- **Inventarios:** Registro, edición, filtrado y exportación de inventarios generales de transferencia.
- **Catálogos de Transferencia:** Gestión y exportación de catálogos (Anexo N° 06).
- **Registros de Transferencia:** Gestión y exportación de registros (Anexo N° 05).
- **Documentos:** Carga, descarga, Vizualizacion, búsqueda y control de versiones.
- **Usuarios y Permisos:** Administración de usuarios, roles y auditoría de actividades.

## Exportación a Excel/PDF

- Los módulos de inventario, catálogo y registro permiten exportar los datos a Excel y PDF usando plantillas personalizadas.

## Seguridad

- Autenticación JWT y control de acceso granular por permisos.
- Configuración de superusuario en `application.properties`.

---

> Si estas interesado en el proyecto, contactar a: clidertutayarivera@gmail.com
