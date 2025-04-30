package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.PermissionService;
import com.ns.iestpffaaarchives.domain.entity.Permission;
import com.ns.iestpffaaarchives.domain.enums.PermissionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing permissions.
 */
@RestController
@RequestMapping("/api/permissions")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * Get all permissions.
     *
     * @return List of all permissions
     */
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /**
     * Get available permission types from enum.
     *
     * @return List of available permission types
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getPermissionTypes() {
        List<Map<String, String>> permissionTypes = Arrays.stream(PermissionEnum.values())
                .map(permission -> Map.of(
                        "name", permission.getName(),
                        "displayName", permission.getDisplayName(),
                        "description", permission.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissionTypes);
    }

    /**
     * Get permission by ID.
     *
     * @param id Permission ID
     * @return Permission if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        return permissionService.getPermissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new permission.
     *
     * @param permission Permission to create
     * @return Created permission
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(permissionService.createPermission(permission));
    }

    /**
     * Update an existing permission.
     *
     * @param id         Permission ID
     * @param permission Updated permission data
     * @return Updated permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        try {
            return ResponseEntity.ok(permissionService.updatePermission(id, permission));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a permission.
     *
     * @param id Permission ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
