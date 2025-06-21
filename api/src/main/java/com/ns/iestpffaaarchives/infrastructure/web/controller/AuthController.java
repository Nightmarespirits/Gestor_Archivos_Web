package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.Role;
import com.ns.iestpffaaarchives.domain.enums.RoleEnum;
import com.ns.iestpffaaarchives.domain.repository.RoleRepository;
import com.ns.iestpffaaarchives.infrastructure.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
// Eliminamos la anotación @CrossOrigin, ya que la configuración global en WebConfig.java se encargará de esto
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registrationRequest) {
        try {
            // Extract user data from the request
            User user = new User();
            user.setUsername((String) registrationRequest.get("username"));
            user.setPassword((String) registrationRequest.get("password"));
            user.setEmail((String) registrationRequest.get("email"));
            user.setFullName((String) registrationRequest.get("fullName"));
            
            // Validar si el usuario ya existe
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: El nombre de usuario ya está en uso");
            }

            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: El correo electrónico ya está en uso");
            }
            
            // Check if role is provided
            if (registrationRequest.containsKey("roleId")) {
                Long roleId = Long.valueOf(registrationRequest.get("roleId").toString());
                Optional<Role> role = roleRepository.findById(roleId);
                if (role.isPresent()) {
                    user.setRole(role.get());
                } else {
                    return ResponseEntity
                            .badRequest()
                            .body("Error: El rol especificado no existe");
                }
            } else {
                // If no role is specified, assign the default USER role
                Optional<Role> defaultRole = roleRepository.findByName(RoleEnum.USER.getName());
                defaultRole.ifPresent(user::setRole);
            }

            // Crear nuevo usuario
            User newUser = userService.createUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el usuario: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            // 1. Authenticate using AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // 2. Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 4. Get User details for the response (optional, but often useful)
            // Fetching again ensures we have the full User entity if needed
            User userDetails = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Error: User not found after authentication. This should not happen."));

            // 5. Create response payload
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("id", userDetails.getId());
            response.put("username", userDetails.getUsername());
            response.put("email", userDetails.getEmail());
            response.put("fullName", userDetails.getFullName());
            
            // Include role information in the response
            if (userDetails.getRole() != null) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("id", userDetails.getRole().getId());
                roleInfo.put("name", userDetails.getRole().getName());
                response.put("role", roleInfo);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) { // Catch specific AuthenticationException for better error handling
            // Log the exception e.g., log.error("Authentication failed for user {}: {}", username, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid credentials"); // Provide a generic error message
        }
    }
}
