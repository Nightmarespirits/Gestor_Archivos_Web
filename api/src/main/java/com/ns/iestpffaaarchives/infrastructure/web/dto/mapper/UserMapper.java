package com.ns.iestpffaaarchives.infrastructure.web.dto.mapper;

import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.infrastructure.web.dto.UserDTO;
import com.ns.iestpffaaarchives.infrastructure.web.dto.RoleDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setStatus(user.getStatus());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setCreatedAt(user.getCreatedAt());
        
        if (user.getRole() != null) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            roleDTO.setDescription(user.getRole().getDescription());
            userDTO.setRole(roleDTO);
            userDTO.setRoleId(user.getRole().getId()); // Establecer el roleId para facilitar las actualizaciones
        }
        
        return userDTO;
    }
    
    public static List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }
}
