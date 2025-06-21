package com.ns.iestpffaaarchives.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private RoleDTO role;
    private Long roleId; // Campo añadido para facilitar las actualizaciones
}
