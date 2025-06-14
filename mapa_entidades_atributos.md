# Mapa de Entidades y Atributos del Backend - ARCHIVO IESTPFFAA

## Resumen del Sistema

El sistema ARCHIVO_IESTPFFAA es una plataforma de gestión de archivos documentales para el Instituto de Educación Superior Tecnológico Público de las Fuerzas Armadas. El backend está construido con Java y utiliza JPA/Hibernate para la persistencia de datos.

## Entidades Principales

### 1. User (Usuario)
- **Descripción**: Representa a los usuarios del sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `username`: String (único, no nulo)
  - `password`: String (no nulo)
  - `email`: String (único)
  - `fullName`: String
  - `status`: Boolean (activo/inactivo)
  - `lastLogin`: LocalDateTime
  - `createdAt`: LocalDateTime (generado automáticamente)
- **Relaciones**:
  - ManyToOne con `Role` (role_id)

### 2. Role (Rol)
- **Descripción**: Define los roles de los usuarios en el sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `name`: String (único, no nulo)
  - `description`: String
- **Relaciones**:
  - ManyToMany con `Permission` (role_permissions)

### 3. Permission (Permiso)
- **Descripción**: Define los permisos disponibles en el sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `name`: String
  - `description`: String
- **Relaciones**:
  - ManyToMany con `Role` (role_permissions)

### 4. Document (Documento)
- **Descripción**: Representa los documentos gestionados en el sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `title`: String (no nulo)
  - `description`: String
  - `filePath`: String (no nulo)
  - `format`: String (no nulo)
  - `uploadDate`: LocalDateTime (no nulo)
  - `isDeleted`: Boolean (por defecto false)
  - `versionNumber`: Integer (por defecto 1)
- **Relaciones**:
  - ManyToOne con `User` como autor (author_id)
  - ManyToOne con `DocumentType` (type_id)
  - OneToOne con `DocumentSecurity` (security_id)
  - ManyToMany con `Tag` (document_tags)

### 5. DocumentType (Tipo de Documento)
- **Descripción**: Define los tipos de documentos en el sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `name`: String
  - `description`: String
- **Relaciones**:
  - OneToMany con `Document`

### 6. DocumentSecurity (Seguridad de Documento)
- **Descripción**: Define la configuración de seguridad para los documentos.
- **Atributos**:
  - `id`: Long (PK)
  - `accessLevel`: String/Enum
  - `isConfidential`: Boolean
  - `isClassified`: Boolean
- **Relaciones**:
  - OneToOne con `Document`

### 7. DocumentVersion (Versión de Documento)
- **Descripción**: Mantiene el historial de versiones de los documentos.
- **Atributos**:
  - `id`: Long (PK)
  - `versionNumber`: Integer
  - `filePath`: String
  - `createdAt`: LocalDateTime
  - `notes`: String
- **Relaciones**:
  - ManyToOne con `Document`
  - ManyToOne con `User` (modificador)

### 8. Tag (Etiqueta)
- **Descripción**: Etiquetas para categorizar documentos.
- **Atributos**:
  - `id`: Long (PK)
  - `name`: String
  - `color`: String
- **Relaciones**:
  - ManyToMany con `Document` (document_tags)

### 9. ActivityLog (Registro de Actividad)
- **Descripción**: Registro de actividades realizadas en el sistema.
- **Atributos**:
  - `id`: Long (PK)
  - `action`: String
  - `description`: String
  - `timestamp`: LocalDateTime
  - `ipAddress`: String
- **Relaciones**:
  - ManyToOne con `User` (usuario que realizó la acción)
  - ManyToOne con `Document` (documento afectado, opcional)

### 10. AccessControl (Control de Acceso)
- **Descripción**: Define accesos específicos a documentos para usuarios.
- **Atributos**:
  - `id`: Long (PK)
  - `canRead`: Boolean
  - `canWrite`: Boolean
  - `canDelete`: Boolean
  - `grantedAt`: LocalDateTime
  - `expiresAt`: LocalDateTime
- **Relaciones**:
  - ManyToOne con `User`
  - ManyToOne con `Document`

### 11. Notification (Notificación)
- **Descripción**: Notificaciones del sistema para los usuarios.
- **Atributos**:
  - `id`: Long (PK)
  - `message`: String
  - `type`: String/Enum
  - `isRead`: Boolean
  - `createdAt`: LocalDateTime
- **Relaciones**:
  - ManyToOne con `User` (destinatario)
  - ManyToOne con `Document` (documento relacionado, opcional)

### 12. SystemIntegration (Integración del Sistema)
- **Descripción**: Configuración para integraciones con sistemas externos.
- **Atributos**:
  - `id`: Long (PK)
  - `name`: String
  - `apiKey`: String
  - `endpoint`: String
  - `isActive`: Boolean
  - `lastSyncAt`: LocalDateTime

### 13. TransferenciaDocumental
- **Descripción**: Registro de transferencias documentales.
- **Atributos**:
  - `id`: Long (PK)
  - `entidad`: String
  - `unidadOrganica`: String
  - `seccion`: String
  - `nivelDescripcion`: String
  - `serieDocumental`: String
  - `codigoReferencia`: String
  - `soporte`: String
  - `volumenMetrosLineales`: BigDecimal
  - `responsable`: String
  - `elaboradoPor`: String
  - `numeroAnioRemision`: String
  - `lugarFecha`: String
  - `fechaTransferencia`: LocalDate
- **Relaciones**:
  - OneToMany con `ItemTransferencia` (items)
  - OneToMany con `ReporteDocumentario` (reportes)

### 14. ItemTransferencia
- **Descripción**: Detalles específicos de cada ítem en una transferencia documental.
- **Atributos**:
  - `id`: Long (PK)
  - `codigo`: String
  - `descripcion`: String
  - `numeroItem`: String
  - `nombreSerieDocumental`: String
  - `fechaExtremaDesde`: LocalDate
  - `fechaExtremaHasta`: LocalDate
  - `tipoUnidadArchivamiento`: String
  - `cantidadUnidadArchivamiento`: Integer
  - `volumenMetrosLineales`: BigDecimal
  - `soporte`: String
  - `numeroFolios`: Integer
  - `fechasExtremas`: String
  - `numeroCaja`: String
  - `numeroTomoPaquete`: String
  - `alcanceContenido`: String
  - `rangoExtremoDesde`: String
  - `rangoExtremoHasta`: String
  - `cantidadFolios`: Integer
  - `numeroUnidadDocumental`: String
  - `fechaUnidadDocumental`: LocalDate
  - `informacionAdicional`: String
  - `ubicacionTopografica`: String
  - `observaciones`: String
- **Relaciones**:
  - ManyToOne con `TransferenciaDocumental` (transferencia_id)

### 15. ReporteDocumentario
- **Descripción**: Informes generados a partir de las transferencias documentales.
- **Atributos**:
  - `id`: Long (PK)
  - `tipoReporte`: TipoReporte (Enum)
  - `filePath`: String
  - `tamanioPdf`: Long
  - `tamanioExcel`: Long
  - `archivoPdf`: byte[] (BLOB)
  - `archivoExcel`: byte[] (BLOB)
  - `fechaGeneracion`: LocalDateTime
  - `generadoPor`: String
- **Relaciones**:
  - ManyToOne con `TransferenciaDocumental` (transferencia_documental_id)

### 16. Transfer
- **Descripción**: Registro de transferencias de documentos entre usuarios o departamentos.
- **Atributos**:
  - `id`: Long (PK)
  - `reason`: String
  - `status`: String/Enum
  - `requestedAt`: LocalDateTime
  - `completedAt`: LocalDateTime
- **Relaciones**:
  - ManyToOne con `User` (remitente)
  - ManyToOne con `User` (destinatario)
  - ManyToMany con `Document` (documentos transferidos)

## Diagrama de Relaciones
```
User <--> Role <--> Permission
User <--> Document
Document <--> DocumentType
Document <--> DocumentSecurity
Document <--> DocumentVersion
Document <--> Tag
User <--> ActivityLog <--> Document
User <--> AccessControl <--> Document
User <--> Notification <--> Document
User <--> Transfer <--> Document
TransferenciaDocumental <--> ItemTransferencia
TransferenciaDocumental <--> ReporteDocumentario
```

## Análisis de Dominio

El sistema está diseñado para gestionar documentos institucionales con un enfoque en:

1. **Gestión de usuarios y permisos**: Sistema de roles y permisos granular
2. **Gestión documental**: Documentos con versionado, etiquetado y metadatos
3. **Seguridad**: Controles de acceso y confidencialidad
4. **Trazabilidad**: Registro de actividades y notificaciones
5. **Transferencias documentales**: Gestión del proceso de transferencia de documentos entre entidades
6. **Generación de reportes**: Informes en PDF y Excel sobre las transferencias
