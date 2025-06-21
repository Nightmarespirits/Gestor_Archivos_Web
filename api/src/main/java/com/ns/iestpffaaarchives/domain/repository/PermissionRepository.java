package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Find a permission by its name.
     *
     * @param name Permission name
     * @return Optional containing the permission if found
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if a permission exists by its name.
     *
     * @param name Permission name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}
