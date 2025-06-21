package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find a role by its name.
     *
     * @param name Role name
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if a role exists by its name.
     *
     * @param name Role name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}
