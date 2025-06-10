package com.ns.iestpffaaarchives.application.event;

import com.ns.iestpffaaarchives.domain.entity.TransferenciaDocumental;

public class TransferCompletedEvent {
    private final TransferenciaDocumental transferencia;

    public TransferCompletedEvent(TransferenciaDocumental transferencia) {
        this.transferencia = transferencia;
    }

    public TransferenciaDocumental getTransferencia() {
        return transferencia;
    }
}
