package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.application.event.TransferCompletedEvent;
import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TransferService(TransferRepository transferRepository, ApplicationEventPublisher eventPublisher) {
        this.transferRepository = transferRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Transfer updateTransferState(Long id, String state) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        transfer.setState(state);
        Transfer saved = transferRepository.save(transfer);
        if ("COMPLETADA".equalsIgnoreCase(state)) {
            eventPublisher.publishEvent(new TransferCompletedEvent(saved));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Transfer> findCompletedTransfersWithoutReport() {
        return transferRepository.findCompletedTransfersWithoutReport();
    }
}
