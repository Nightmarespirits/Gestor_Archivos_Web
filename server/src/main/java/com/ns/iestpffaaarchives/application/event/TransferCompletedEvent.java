package com.ns.iestpffaaarchives.application.event;

import com.ns.iestpffaaarchives.domain.entity.Transfer;

public class TransferCompletedEvent {
    private final Transfer transfer;

    public TransferCompletedEvent(Transfer transfer) {
        this.transfer = transfer;
    }

    public Transfer getTransfer() {
        return transfer;
    }
}
