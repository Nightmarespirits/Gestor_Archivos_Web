package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.application.event.TransferCompletedEvent;
import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;
import com.ns.iestpffaaarchives.domain.repository.TransferenciaDocumentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransferenciaDocumentalService {

    private final TransferenciaDocumentalRepository transferenciaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TransferenciaDocumentalService(TransferenciaDocumentalRepository transferenciaRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.transferenciaRepository = transferenciaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferenciaDocumental completeTransfer(Long id) {
        TransferenciaDocumental transferencia = transferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transferencia not found"));
        if (transferencia.getFechaTransferencia() == null) {
            transferencia.setFechaTransferencia(LocalDate.now());
        }
        TransferenciaDocumental saved = transferenciaRepository.save(transferencia);
        // reload with items to ensure they are available for the event listener
        TransferenciaDocumental full = transferenciaRepository.findWithItemsById(saved.getId())
                .orElse(saved);
        eventPublisher.publishEvent(new TransferCompletedEvent(full));
        return saved;
    }

    @Transactional(readOnly = true)
    public TransferenciaDocumental findByIdWithItems(Long id) {
        return transferenciaRepository.findWithItemsById(id)
                .orElseThrow(() -> new RuntimeException("Transferencia not found"));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaDocumental> findCompletedTransfersWithoutReport() {
        return transferenciaRepository.findCompletedTransfersWithoutReport();
    }
}
