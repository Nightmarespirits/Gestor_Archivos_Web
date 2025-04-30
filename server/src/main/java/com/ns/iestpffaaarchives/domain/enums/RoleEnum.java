package com.ns.iestpffaaarchives.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum that defines the standard roles in the system.
 */
public enum RoleEnum {
    SUPERADMIN("Superadministrador", "Acceso total al sistema", 
        PermissionEnum.values()),
        
    ADMIN("Administrador", "Acceso administrativo sin gestión de roles ni configuración de sistema",
        PermissionEnum.USER_READ, PermissionEnum.USER_UPDATE, PermissionEnum.USER_DELETE,
        PermissionEnum.DOCUMENT_CREATE, PermissionEnum.DOCUMENT_READ, PermissionEnum.DOCUMENT_UPDATE, PermissionEnum.DOCUMENT_DELETE,
        PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD, PermissionEnum.FILE_DELETE,
        PermissionEnum.ARCHIVE_MANAGE),
        
    MANAGER("Gestor", "Puede gestionar documentos y usuarios",
        PermissionEnum.USER_READ, PermissionEnum.USER_UPDATE,
        PermissionEnum.DOCUMENT_CREATE, PermissionEnum.DOCUMENT_READ, PermissionEnum.DOCUMENT_UPDATE, PermissionEnum.DOCUMENT_DELETE,
        PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD, PermissionEnum.FILE_DELETE, PermissionEnum.ARCHIVE_MANAGE),
        
    ARCHIVIST("Archivista", "Puede gestionar documentos y archivos históricos",
        PermissionEnum.DOCUMENT_CREATE, PermissionEnum.DOCUMENT_READ, PermissionEnum.DOCUMENT_UPDATE, PermissionEnum.DOCUMENT_DELETE,
        PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD, PermissionEnum.ARCHIVE_MANAGE),
        
    USER("Usuario", "Usuario básico con acceso limitado",
        PermissionEnum.DOCUMENT_READ, PermissionEnum.FILE_DOWNLOAD);
    
    private final String displayName;
    private final String description;
    private final Set<PermissionEnum> permissions;
    
    RoleEnum(String displayName, String description, PermissionEnum... permissions) {
        this.displayName = displayName;
        this.description = description;
        this.permissions = new HashSet<>(Arrays.asList(permissions));
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Set<PermissionEnum> getPermissions() {
        return permissions;
    }
    
    public String getName() {
        return this.name();
    }
}
