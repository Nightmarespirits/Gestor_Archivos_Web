package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.Role;
import com.ns.iestpffaaarchives.infrastructure.web.dto.UserDTO;
import com.ns.iestpffaaarchives.infrastructure.web.dto.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(UserMapper.toDTOList(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(UserMapper.toDTO(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDetails) {
        Optional<User> userOptional = userService.getUserById(id);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Actualizar solo los campos que se permiten cambiar según la documentación
            if (userDetails.getFullName() != null) {
                user.setFullName(userDetails.getFullName());
            }
            if (userDetails.getEmail() != null) {
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getStatus() != null) {
                user.setStatus(userDetails.getStatus());
            }
            
            // Si se proporciona un roleId, actualizar el rol
            if (userDetails.getRoleId() != null) {
                // Obtener el servicio de roles para buscar el rol por ID
                try {
                    // Verificar si el usuario ya tiene un rol
                    if (user.getRole() == null) {
                        // Si el usuario no tiene rol, necesitamos obtener el rol del servicio de roles
                        // Por ahora, crearemos un nuevo objeto de rol con el ID proporcionado
                        Role newRole = new Role();
                        newRole.setId(userDetails.getRoleId());
                        // Aquí idealmente se debería buscar el nombre y descripción del rol desde un servicio
                        // pero por simplicidad solo establecemos el ID
                        user.setRole(newRole);
                    } else {
                        // Si el usuario ya tiene un rol, actualizamos solo el ID
                        user.getRole().setId(userDetails.getRoleId());
                    }
                } catch (Exception e) {
                    // Loguear el error pero continuar con la actualización
                    System.err.println("Error al actualizar el rol del usuario: " + e.getMessage());
                }
            }
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);
        
        if (userOptional.isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(u -> ResponseEntity.ok(UserMapper.toDTO(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
