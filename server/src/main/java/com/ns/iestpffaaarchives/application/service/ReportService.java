package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.ReporteDocumentario;

import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.enums.TipoReporte;
import com.ns.iestpffaaarchives.domain.repository.ReporteDocumentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {

    private final ReporteDocumentarioRepository reporteRepository;

    @Autowired
    public ReportService(ReporteDocumentarioRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional
    public ReporteDocumentario generateReportForTransfer(Transfer transfer) {
        ReporteDocumentario reporte = new ReporteDocumentario();
        reporte.setTransfer(transfer);
        reporte.setTipo(TipoReporte.INVENTARIO);
        reporte.setFechaGeneracion(LocalDateTime.now());
        return reporteRepository.save(reporte);
    }
}
