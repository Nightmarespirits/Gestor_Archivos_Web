package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferenciaDocumentalRepository extends JpaRepository<TransferenciaDocumental, Long> {

    @EntityGraph(attributePaths = "items")
    @Query("SELECT t FROM TransferenciaDocumental t WHERE t.id = :id")
    Optional<TransferenciaDocumental> findWithItemsById(@Param("id") Long id);

    @EntityGraph(attributePaths = "items")
    @Query("SELECT t FROM TransferenciaDocumental t " +
           "WHERE t.fechaTransferencia IS NOT NULL " +
           "AND t.id NOT IN (SELECT r.transferencia.id FROM ReporteDocumentario r)")
    List<TransferenciaDocumental> findCompletedTransfersWithoutReport();
}
