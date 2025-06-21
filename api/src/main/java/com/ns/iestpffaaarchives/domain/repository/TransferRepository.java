package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("SELECT t FROM Transfer t WHERE t.state = 'COMPLETADA' AND t.id NOT IN (SELECT r.transferencia.id FROM ReporteDocumentario r)")
    List<Transfer> findCompletedTransfersWithoutReport();
}
