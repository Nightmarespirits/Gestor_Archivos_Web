package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Transfer;
import com.ns.iestpffaaarchives.domain.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;

    @Autowired
    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Transfer updateTransferState(Long id, String state) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        transfer.setState(state);
        return transferRepository.save(transfer);
    }

    @Transactional(readOnly = true)
    public List<Transfer> findCompletedTransfersWithoutReport() {
        return transferRepository.findCompletedTransfersWithoutReport();
    }
}
