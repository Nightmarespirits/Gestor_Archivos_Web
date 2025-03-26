# ARCHIVO_IESTPFFAA
El sistema de gestión de documentos para el IESTPFFAA está diseñado para centralizar y organizar de manera eficiente todos los documentos oficiales, como cartas, oficios, solicitudes y certificados. 

# Requerimientos para el Sistema de Gestión de Documentos Educativos

## Requerimientos Funcionales

### Autenticación y Autorización
- Registro e inicio de sesión de usuarios.
- Asignación de roles y permisos a los usuarios.
- Control de acceso basado en roles.

### Gestión de Documentos
- Subida de documentos en formatos DOC, PDF, etc.
- Visualización y descarga de documentos.
- Edición y eliminación de documentos (con control de permisos).

### Organización de Documentos
- Clasificación de documentos por tipo (carta, oficio, solicitud, certificado, etc.).
- Ordenación de documentos por fecha de subida, hora y autor.
- Etiquetado y categorización de documentos.

### Búsqueda y Filtrado
- Búsqueda de documentos por palabras clave, tipo, fecha, autor, etc.
- Filtrado avanzado de documentos.

### Seguridad
- Cifrado de documentos y datos sensibles.
- Registro de actividades de los usuarios.
- Historial de versiones de los documentos.

### Notificaciones
- Notificaciones automáticas para recordatorios y actualizaciones.
- Alertas de seguridad.

## Requerimientos No Funcionales

### Rendimiento
- Tiempo de respuesta rápido para la búsqueda y carga de documentos.
- Capacidad para manejar un gran volumen de documentos y usuarios simultáneos.

### Usabilidad
- Interfaz de usuario intuitiva y fácil de navegar.
- Accesibilidad para usuarios con discapacidades.

### Escalabilidad
- Capacidad para escalar el sistema conforme aumente el número de usuarios y documentos.

### Mantenibilidad
- Código limpio y bien documentado.
- Facilidad para realizar actualizaciones y mejoras.

### Seguridad
- Protección contra accesos no autorizados.
- Cumplimiento con normativas de protección de datos.

### Compatibilidad
- Compatibilidad con diferentes navegadores y dispositivos.
- Integración con otros sistemas de la institución (ERP, CRM, etc.).

### **Entidades y Atributos**

#### **1. Usuario (`User`)**
- **Atributos**:
  - `id` (Identificador único)
  - `username` (Nombre de usuario)
  - `password` (Contraseña cifrada)
  - `email` (Correo electrónico)
  - `role_id` (Rol asignado, FK a `Role`)
  - `full_name` (Nombre completo)
  - `status` (Activo/Inactivo)
  - `last_login` (Último inicio de sesión)
  - `created_at` (Fecha de registro)

#### **2. Rol (`Role`)**
- **Atributos**:
  - `id` (Identificador único)
  - `name` (Ej: "Administrador", "Docente", "Estudiante")
  - `description` (Descripción del rol)
  - `permissions` (Relación muchos-a-muchos con `Permission`)

#### **3. Permiso (`Permission`)**
- **Atributos**:
  - `id` (Identificador único)
  - `name` (Ej: "upload_document", "edit_document", "delete_document")
  - `description` (Descripción del permiso)

#### **4. Documento (`Document`)**
- **Atributos**:
  - `id` (Identificador único)
  - `title` (Título del documento)
  - `description` (Descripción breve)
  - `file_path` (Ruta del archivo en almacenamiento)
  - `format` (PDF, DOC, etc.)
  - `upload_date` (Fecha y hora de subida)
  - `author_id` (Usuario que subió el documento, FK a `User`)
  - `type_id` (Tipo de documento, FK a `DocumentType`)
  - `security_id` (Configuración de seguridad, FK a `DocumentSecurity`)
  - `is_deleted` (Soft delete: Sí/No)
  - `version_number` (Versión actual)

#### **5. Tipo de Documento (`DocumentType`)**
- **Atributos**:
  - `id` (Identificador único)
  - `name` (Ej: "Carta", "Oficio", "Certificado")
  - `description` (Descripción del tipo)

#### **6. Etiqueta (`Tag`)**
- **Atributos**:
  - `id` (Identificador único)
  - `name` (Ej: "Urgente", "Académico", "2024")
  - `description` (Descripción opcional)

#### **7. Documento-Etiqueta (`DocumentTag`)**
- **Atributos**:
  - `document_id` (FK a `Document`)
  - `tag_id` (FK a `Tag`)

#### **8. Metadatos (`Metadata`)**
- **Atributos**:
  - `id` (Identificador único)
  - `document_id` (FK a `Document`)
  - `keywords` (Palabras clave para búsqueda)
  - `department` (Departamento asociado, opcional)
  - `expiration_date` (Fecha de expiración, si aplica)

#### **9. Seguridad de Documento (`DocumentSecurity`)**
- **Atributos**:
  - `id` (Identificador único)
  - `document_id` (FK a `Document`)
  - `encryption_status` (Cifrado: Sí/No)
  - `encryption_method` (Ej: "AES-256")
  - `access_level` (Ej: "Público", "Privado")

#### **10. Control de Acceso (`AccessControl`)**
- **Atributos**:
  - `id` (Identificador único)
  - `document_id` (FK a `Document`)
  - `user_id` (FK a `User`, nullable)
  - `role_id` (FK a `Role`, nullable)
  - `permission_type` (Ej: "Lectura", "Escritura")

#### **11. Historial de Actividades (`ActivityLog`)**
- **Atributos**:
  - `id` (Identificador único)
  - `user_id` (Usuario que realizó la acción, FK a `User`)
  - `action_type` (Ej: "Subida", "Edición", "Eliminación")
  - `document_id` (FK a `Document`)
  - `timestamp` (Fecha y hora de la acción)
  - `ip_address` (Dirección IP del usuario)

#### **12. Versión de Documento (`DocumentVersion`)**
- **Atributos**:
  - `id` (Identificador único)
  - `document_id` (FK a `Document`)
  - `version_number` (Número incremental)
  - `changes` (Descripción de los cambios realizados)
  - `author_id` (Usuario que editó, FK a `User`)
  - `version_date` (Fecha de la versión)
  - `file_path` (Ruta del archivo actualizado)

#### **13. Notificación (`Notification`)**
- **Atributos**:
  - `id` (Identificador único)
  - `user_id` (Destinatario, FK a `User`)
  - `message` (Contenido de la notificación)
  - `type` (Ej: "Recordatorio", "Alerta de Seguridad")
  - `status` (Enviada/Leída/Pendiente)
  - `created_at` (Fecha de creación)
  - `related_document_id` (FK a `Document`, opcional)

#### **14. Integración de Sistemas (`SystemIntegration`)**
- **Atributos**:
  - `id` (Identificador único)
  - `system_name` (Ej: "ERP", "CRM")
  - `api_endpoint` (URL de la API externa)
  - `auth_token` (Token de autenticación cifrado)
  - `last_sync` (Fecha de última sincronización)
  - `sync_status` (Éxito/Error/Pendiente)

---

### **Relaciones Clave**
- **Usuario-Rol**: Un usuario tiene un rol, un rol puede tener muchos usuarios.
- **Rol-Permiso**: Un rol tiene múltiples permisos, un permiso puede estar en múltiples roles.
- **Documento-Tipo**: Un documento tiene un tipo, un tipo puede clasificar muchos documentos.
- **Documento-Etiqueta**: Un documento puede tener múltiples etiquetas, una etiqueta puede estar en múltiples documentos.
- **Documento-Versión**: Un documento tiene múltiples versiones, cada versión pertenece a un documento.

