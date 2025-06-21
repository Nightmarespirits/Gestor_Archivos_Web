package com.ns.iestpffaaarchives.infrastructure.web.dto;

import com.ns.iestpffaaarchives.domain.entity.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private Long id;
    private Long userId;
    private String username; // Nombre de usuario
    private String userFullName; // Nombre completo del usuario (opcional)
    private String actionType;
    private Long documentId;
    private LocalDateTime timestamp;
    private String ipAddress;

    // Constructor para convertir desde la entidad
    public ActivityLogDTO(ActivityLog activityLog) {
        this.id = activityLog.getId();
        this.actionType = activityLog.getActionType();
        this.timestamp = activityLog.getTimestamp();
        this.ipAddress = activityLog.getIpAddress();

        // Manejar la relación con el usuario
        if (activityLog.getUser() != null) {
            this.userId = activityLog.getUser().getId();
            this.username = activityLog.getUser().getUsername();
            
            // Si tienes campos de nombre y apellido en la entidad User
            this.userFullName = (activityLog.getUser().getUsername() != null ? activityLog.getUser().getUsername() : "");
            
            // Eliminar espacios extra
            this.userFullName = this.userFullName.trim();
            // Si solo hay espacios, usar el username
            if (this.userFullName.isEmpty()) {
                this.userFullName = this.username;
            }
        }

        // Manejar la relación con el documento
        if (activityLog.getDocument() != null) {
            this.documentId = activityLog.getDocument().getId();
        }
    }
}
