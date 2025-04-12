package com.ns.iestpffaaarchives.infrastructure.config;

import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.Permission;
import com.ns.iestpffaaarchives.domain.entity.Role;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.enums.PermissionEnum;
import com.ns.iestpffaaarchives.domain.enums.RoleEnum;
import com.ns.iestpffaaarchives.domain.repository.PermissionRepository;
import com.ns.iestpffaaarchives.domain.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Configuration class for initializing basic data in the database.
 */
@Configuration
@Slf4j
public class DataInitializationConfig {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Initialize database with basic roles and permissions.
     */
    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Initializing database with basic roles and permissions...");
            
            // Initialize permissions
            initializePermissions();
            
            // Initialize roles
            initializeRoles();
            
            // Create admin user if it doesn't exist
            createAdminUser();
            
            log.info("Database initialization completed.");
        };
    }

    private void initializePermissions() {
        log.info("Initializing permissions...");
        
        for (PermissionEnum permEnum : PermissionEnum.values()) {
            Optional<Permission> existingPermission = permissionRepository.findByName(permEnum.getName());
            
            if (existingPermission.isEmpty()) {
                Permission permission = new Permission();
                permission.setName(permEnum.getName());
                permission.setDescription(permEnum.getDescription());
                permissionRepository.save(permission);
                log.info("Created permission: {}", permEnum.getName());
            }
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        for (RoleEnum roleEnum : RoleEnum.values()) {
            Optional<Role> existingRole = roleRepository.findByName(roleEnum.getName());
            Role role;
            
            if (existingRole.isEmpty()) {
                role = new Role();
                role.setName(roleEnum.getName());
                role.setDescription(roleEnum.getDescription());
                log.info("Created role: {}", roleEnum.getName());
            } else {
                role = existingRole.get();
                log.info("Updating existing role: {}", roleEnum.getName());
            }
            
            // Set permissions for the role
            Set<Permission> permissions = new HashSet<>();
            for (PermissionEnum permEnum : roleEnum.getPermissions()) {
                permissionRepository.findByName(permEnum.getName())
                    .ifPresent(permissions::add);
            }
            
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }

    private void createAdminUser() {
        log.info("Checking for admin user...");
        
        if (!userService.existsByUsername("admin")) {
            log.info("Creating admin user...");
            
            // Codificar la contraseña y mostrarla en los logs
            String rawPassword = "admin";
            String encodedPassword = passwordEncoder.encode(rawPassword);
            log.info("Admin password (raw): {}", rawPassword);
            log.info("Admin password (encoded): {}", encodedPassword);
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(encodedPassword);
            adminUser.setEmail("admin@iestpffaa.edu.pe");
            adminUser.setFullName("Administrator");
            adminUser.setStatus(true);
            
            // Set admin role
            roleRepository.findByName(RoleEnum.ADMIN.getName())
                .ifPresent(adminUser::setRole);
            
            User savedUser = userService.createUser(adminUser);
            log.info("Admin user created successfully with ID: {}", savedUser.getId());
        } else {
            log.info("Admin user already exists.");
            
            // Opcional: Actualizar la contraseña del usuario admin existente
            userService.getUserByUsername("admin").ifPresent(adminUser -> {
                String newEncodedPassword = passwordEncoder.encode("admin");
                adminUser.setPassword(newEncodedPassword);
                userService.updateUser(adminUser);
                log.info("Admin password has been reset.");
            });
        }
    }
}
