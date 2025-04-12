package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Permission;
import com.ns.iestpffaaarchives.domain.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing permissions.
 */
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Get all permissions.
     *
     * @return List of all permissions
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Get permission by ID.
     *
     * @param id Permission ID
     * @return Optional containing the permission if found
     */
    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    /**
     * Get permission by name.
     *
     * @param name Permission name
     * @return Optional containing the permission if found
     */
    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    /**
     * Create a new permission.
     *
     * @param permission Permission to create
     * @return Created permission
     */
    @Transactional
    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    /**
     * Update an existing permission.
     *
     * @param id         Permission ID
     * @param permission Updated permission data
     * @return Updated permission
     * @throws RuntimeException if permission not found
     */
    @Transactional
    public Permission updatePermission(Long id, Permission permission) {
        return permissionRepository.findById(id)
                .map(existingPermission -> {
                    existingPermission.setName(permission.getName());
                    existingPermission.setDescription(permission.getDescription());
                    return permissionRepository.save(existingPermission);
                })
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    /**
     * Delete a permission.
     *
     * @param id Permission ID
     * @throws RuntimeException if permission not found
     */
    @Transactional
    public void deletePermission(Long id) {
        if (permissionRepository.existsById(id)) {
            permissionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Permission not found with id: " + id);
        }
    }
}
