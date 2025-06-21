package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Permission;
import com.ns.iestpffaaarchives.domain.entity.Role;
import com.ns.iestpffaaarchives.domain.repository.PermissionRepository;
import com.ns.iestpffaaarchives.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing roles.
 */
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Get all roles.
     *
     * @return List of all roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get role by ID.
     *
     * @param id Role ID
     * @return Optional containing the role if found
     */
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    /**
     * Get role by name.
     *
     * @param name Role name
     * @return Optional containing the role if found
     */
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * Create a new role.
     *
     * @param role Role to create
     * @return Created role
     */
    @Transactional
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * Update an existing role.
     *
     * @param id   Role ID
     * @param role Updated role data
     * @return Updated role
     * @throws RuntimeException if role not found
     */
    @Transactional
    public Role updateRole(Long id, Role role) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    existingRole.setName(role.getName());
                    existingRole.setDescription(role.getDescription());
                    if (role.getPermissions() != null) {
                        existingRole.setPermissions(role.getPermissions());
                    }
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    /**
     * Delete a role.
     *
     * @param id Role ID
     * @throws RuntimeException if role not found
     */
    @Transactional
    public void deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
        } else {
            throw new RuntimeException("Role not found with id: " + id);
        }
    }

    /**
     * Add permissions to a role.
     *
     * @param roleId       Role ID
     * @param permissionIds Permission IDs to add
     * @return Updated role
     * @throws RuntimeException if role not found
     */
    @Transactional
    public Role addPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().addAll(permissions);

        return roleRepository.save(role);
    }

    /**
     * Remove permissions from a role.
     *
     * @param roleId       Role ID
     * @param permissionIds Permission IDs to remove
     * @return Updated role
     * @throws RuntimeException if role not found
     */
    @Transactional
    public Role removePermissionsFromRole(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().removeAll(permissions);

        return roleRepository.save(role);
    }
}
