package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.RoleService;
import com.ns.iestpffaaarchives.domain.entity.Role;
import com.ns.iestpffaaarchives.domain.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for managing roles.
 */
@RestController
@RequestMapping("/api/roles")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Get all roles.
     *
     * @return List of all roles
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /**
     * Get available role types from enum.
     *
     * @return List of available role types
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getRoleTypes() {
        List<Map<String, String>> roleTypes = Arrays.stream(RoleEnum.values())
                .map(role -> Map.of(
                        "name", role.getName(),
                        "displayName", role.getDisplayName(),
                        "description", role.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleTypes);
    }

    /**
     * Get role by ID.
     *
     * @param id Role ID
     * @return Role if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new role.
     *
     * @param role Role to create
     * @return Created role
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(role));
    }

    /**
     * Update an existing role.
     *
     * @param id   Role ID
     * @param role Updated role data
     * @return Updated role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            return ResponseEntity.ok(roleService.updateRole(id, role));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a role.
     *
     * @param id Role ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add permissions to a role.
     *
     * @param id           Role ID
     * @param permissionIds Map containing permission IDs to add
     * @return Updated role
     */
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> addPermissionsToRole(
            @PathVariable Long id,
            @RequestBody Map<String, Set<Long>> permissionIds) {
        try {
            return ResponseEntity.ok(
                    roleService.addPermissionsToRole(id, permissionIds.get("permissionIds")));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove permissions from a role.
     *
     * @param id           Role ID
     * @param permissionIds Map containing permission IDs to remove
     * @return Updated role
     */
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> removePermissionsFromRole(
            @PathVariable Long id,
            @RequestBody Map<String, Set<Long>> permissionIds) {
        try {
            return ResponseEntity.ok(
                    roleService.removePermissionsFromRole(id, permissionIds.get("permissionIds")));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
