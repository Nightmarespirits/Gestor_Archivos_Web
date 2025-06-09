package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * Repository for storing generated reports.
 */
@Repository
public interface ReporteDocumentarioRepository extends JpaRepository<ReporteDocumentario, Long> {
    List<ReporteDocumentario> findByTransferId(Long transferId);

    @Query("SELECT r.pdfContent FROM ReporteDocumentario r WHERE r.id = :id")
    byte[] findPdfContentById(@Param("id") Long id);

    @Query("SELECT r.excelContent FROM ReporteDocumentario r WHERE r.id = :id")
    byte[] findExcelContentById(@Param("id") Long id);
}
