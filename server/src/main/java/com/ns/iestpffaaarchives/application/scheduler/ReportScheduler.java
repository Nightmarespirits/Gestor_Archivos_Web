package com.ns.iestpffaaarchives.application.scheduler;

import com.ns.iestpffaaarchives.application.service.ReportService;
import com.ns.iestpffaaarchives.application.service.TransferService;
import com.ns.iestpffaaarchives.domain.entity.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportScheduler {

    private final TransferService transferService;
    private final ReportService reportService;

    @Autowired
    public ReportScheduler(TransferService transferService, ReportService reportService) {
        this.transferService = transferService;
        this.reportService = reportService;
    }

    @Scheduled(fixedDelay = 3600000)
    public void generateMissingReports() {
        List<Transfer> transfers = transferService.findCompletedTransfersWithoutReport();
        for (Transfer t : transfers) {
            reportService.generateReportForTransfer(t);
        }
    }
}
