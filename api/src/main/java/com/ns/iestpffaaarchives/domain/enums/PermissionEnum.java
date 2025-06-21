package com.ns.iestpffaaarchives.domain.enums;

/**
 * Enum that defines the standard permissions in the system.
 */
public enum PermissionEnum {
    // User management permissions
    USER_CREATE("Crear usuarios", "Permite crear nuevos usuarios"),
    USER_READ("Ver usuarios", "Permite ver información de los usuarios"),
    USER_UPDATE("Editar usuarios", "Permite editar información de los usuarios"),
    USER_DELETE("Eliminar usuarios", "Permite eliminar usuarios"),
    
    // Document management permissions
    DOCUMENT_CREATE("Crear documentos", "Permite crear nuevos documentos"),
    DOCUMENT_READ("Ver documentos", "Permite ver documentos"),
    DOCUMENT_UPDATE("Editar documentos", "Permite editar documentos"),
    DOCUMENT_DELETE("Eliminar documentos", "Permite eliminar documentos"),
    
    // File management permissions
    FILE_UPLOAD("Subir archivos", "Permite subir archivos"),
    FILE_DOWNLOAD("Descargar archivos", "Permite descargar archivos"),
    FILE_DELETE("Eliminar archivos", "Permite eliminar archivos"),
    
    // Archive management permissions
    ARCHIVE_MANAGE("Gestionar archivos históricos", "Permite gestionar la estructura de archivos históricos"),
    
    // System administration permissions
    ROLE_MANAGE("Gestionar roles", "Permite gestionar roles y permisos"),
    SYSTEM_CONFIG("Configurar el sistema", "Permite configurar ajustes del sistema"),
    
    // Additional suggested permissions
    AUDIT_LOG_VIEW("Ver auditoría", "Permite ver los registros de auditoría del sistema"),
    NOTIFICATION_MANAGE("Gestionar notificaciones", "Permite gestionar notificaciones y alertas"),
    BACKUP_MANAGE("Gestionar respaldos", "Permite gestionar copias de seguridad del sistema"),
    REPORT_VIEW("Ver reportes", "Permite ver y generar reportes"),
    API_ACCESS("Acceso a API", "Permite el acceso a endpoints especiales o integraciones");
    
    private final String displayName;
    private final String description;
    
    PermissionEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getName() {
        return this.name();
    }
}
