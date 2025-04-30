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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${superuser.username}")
    private String superUsername;

    @Value("${superuser.password}")
    private String superPassword;

    @Value("${superuser.email}")
    private String superEmail;

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
            
            // Create superadmin user if it doesn't exist
            createSuperadminUser();
            
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
        
        if (!userService.existsByUsername("admin")) {

            String rawPassword = "92jd$s?P*";
            String encodedPassword = passwordEncoder.encode(rawPassword);
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(encodedPassword);
            adminUser.setEmail("admin@iestpffaa.edu.pe");
            adminUser.setFullName("Administrator");
            adminUser.setStatus(true);
            
            // Set admin role
            roleRepository.findByName(RoleEnum.SUPERADMIN.name())
                .ifPresent(adminUser::setRole);
            
            User savedUser = userService.createUser(adminUser);
            log.info("Admin user created successfully with ID: {}", savedUser.getId());
        } else {
            userService.getUserByUsername("admin").ifPresent(adminUser -> {
                String newEncodedPassword = passwordEncoder.encode("92jd$s?P*");
                adminUser.setPassword(newEncodedPassword);
                userService.updateUser(adminUser);
            });
        }
    }

    private void createSuperadminUser() {
        log.info("Checking for superadmin user...");
        if (!userService.existsByUsername(superUsername)) {
            log.info("Creating superadmin user...");
            String encodedPassword = passwordEncoder.encode(superPassword);
            log.info("Superadmin password (raw): {}", superPassword);
            log.info("Superadmin password (encoded): {}", encodedPassword);

            User superUser = new User();
            superUser.setUsername(superUsername);
            superUser.setPassword(encodedPassword);
            superUser.setEmail(superEmail);
            superUser.setFullName("Superadministrador");
            superUser.setStatus(true);

            // Set SUPERADMIN role
            roleRepository.findByName(RoleEnum.SUPERADMIN.name())
                .ifPresent(superUser::setRole);

            User savedSuperUser = userService.createUser(superUser);
            log.info("Superadmin user created successfully with ID: {}", savedSuperUser.getId());
        } else {
            log.info("Superadmin user already exists.");
            userService.getUserByUsername(superUsername).ifPresent(superUser -> {
                String newEncodedPassword = passwordEncoder.encode(superPassword);
                superUser.setPassword(newEncodedPassword);
                superUser.setEmail(superEmail);
                userService.updateUser(superUser);
                log.info("Superadmin password and email have been reset.");
            });
        }
    }
}
