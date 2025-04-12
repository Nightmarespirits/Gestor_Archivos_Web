package com.ns.iestpffaaarchives.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum that defines the standard roles in the system.
 */
public enum RoleEnum {
    ADMIN("Administrator", "Full system access", 
        PermissionEnum.values()),
        
    MANAGER("Manager", "Can manage documents and users", 
        PermissionEnum.USER_READ, PermissionEnum.USER_UPDATE,
        PermissionEnum.DOCUMENT_CREATE, PermissionEnum.DOCUMENT_READ, 
        PermissionEnum.DOCUMENT_UPDATE, PermissionEnum.DOCUMENT_DELETE,
        PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD, 
        PermissionEnum.FILE_DELETE, PermissionEnum.ARCHIVE_MANAGE),
        
    ARCHIVIST("Archivist", "Can manage documents and archives", 
        PermissionEnum.DOCUMENT_CREATE, PermissionEnum.DOCUMENT_READ, 
        PermissionEnum.DOCUMENT_UPDATE, PermissionEnum.DOCUMENT_DELETE,
        PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD, 
        PermissionEnum.ARCHIVE_MANAGE),
        
    USER("User", "Basic user with limited access", 
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
