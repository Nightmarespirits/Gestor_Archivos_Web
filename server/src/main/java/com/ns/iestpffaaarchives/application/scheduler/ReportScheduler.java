package com.ns.iestpffaaarchives.application.scheduler;

import com.ns.iestpffaaarchives.application.service.ReportService;
import com.ns.iestpffaaarchives.application.service.TransferenciaDocumentalService;
import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportScheduler {

    private final TransferenciaDocumentalService transferenciaService;
    private final ReportService reportService;

    @Autowired
    public ReportScheduler(TransferenciaDocumentalService transferenciaService, ReportService reportService) {
        this.transferenciaService = transferenciaService;
        this.reportService = reportService;
    }

    @Scheduled(fixedDelay = 3600000)
    public void generateMissingReports() {
        List<TransferenciaDocumental> transfers = transferenciaService.findCompletedTransfersWithoutReport();
        for (TransferenciaDocumental t : transfers) {
            var full = transferenciaService.findByIdWithItems(t.getId());
            reportService.generateReportForTransfer(full, full.getItems());
        }
    }
}
