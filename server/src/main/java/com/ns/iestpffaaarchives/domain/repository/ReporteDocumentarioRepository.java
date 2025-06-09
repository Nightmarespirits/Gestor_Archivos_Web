package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for storing generated reports.
 */
@Repository
public interface ReporteDocumentarioRepository extends JpaRepository<ReporteDocumentario, Long> {
    List<ReporteDocumentario> findByTransferId(Long transferId);
}
