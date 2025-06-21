package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
    List<Tag> findByNameContainingIgnoreCase(String name);
}