package com.ns.iestpffaaarchives.application.event;

import com.ns.iestpffaaarchives.application.service.ReportService;
import com.ns.iestpffaaarchives.application.service.TransferenciaDocumentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerationListener {

    private final ReportService reportService;
    private final TransferenciaDocumentalService transferenciaService;

    @Autowired
    public ReportGenerationListener(ReportService reportService,
                                   TransferenciaDocumentalService transferenciaService) {
        this.reportService = reportService;
        this.transferenciaService = transferenciaService;
    }

    @Async
    @EventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        var transferencia = transferenciaService.findByIdWithItems(event.getTransferencia().getId());
        reportService.generateReportForTransfer(transferencia, transferencia.getItems());
    }
}
