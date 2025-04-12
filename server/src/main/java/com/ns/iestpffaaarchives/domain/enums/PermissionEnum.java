package com.ns.iestpffaaarchives.domain.enums;

/**
 * Enum that defines the standard permissions in the system.
 */
public enum PermissionEnum {
    // User management permissions
    USER_CREATE("Create users", "Allows creating new users"),
    USER_READ("View users", "Allows viewing user information"),
    USER_UPDATE("Edit users", "Allows editing user information"),
    USER_DELETE("Delete users", "Allows deleting users"),
    
    // Document management permissions
    DOCUMENT_CREATE("Create documents", "Allows creating new documents"),
    DOCUMENT_READ("View documents", "Allows viewing documents"),
    DOCUMENT_UPDATE("Edit documents", "Allows editing documents"),
    DOCUMENT_DELETE("Delete documents", "Allows deleting documents"),
    
    // File management permissions
    FILE_UPLOAD("Upload files", "Allows uploading files"),
    FILE_DOWNLOAD("Download files", "Allows downloading files"),
    FILE_DELETE("Delete files", "Allows deleting files"),
    
    // Archive management permissions
    ARCHIVE_MANAGE("Manage archives", "Allows managing archive structure"),
    
    // System administration permissions
    ROLE_MANAGE("Manage roles", "Allows managing roles and permissions"),
    SYSTEM_CONFIG("Configure system", "Allows configuring system settings");
    
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
