package com.ns.iestpffaaarchives.application.event;

import com.ns.iestpffaaarchives.application.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerationListener {

    private final ReportService reportService;

    @Autowired
    public ReportGenerationListener(ReportService reportService) {
        this.reportService = reportService;
    }

    @Async
    @EventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        reportService.generateReportForTransfer(event.getTransfer());
    }
}
