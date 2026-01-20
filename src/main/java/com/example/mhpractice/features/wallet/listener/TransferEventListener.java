package com.example.mhpractice.features.wallet.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.mhpractice.common.service.AuditService;
import com.example.mhpractice.features.wallet.event.TransferRequestEvent;
import com.example.mhpractice.features.wallet.model.Transaction;
import com.example.mhpractice.features.wallet.repository.TransactionRepository;
import com.example.mhpractice.features.wallet.service.TransferOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferEventListener {

    private final TransferOrchestrator transferOrchestrator;
    private final TransactionRepository transactionRepository;
    // private final SlackService slackService; // can implement alert to slack app
    private final AuditService auditService;

    @KafkaListener(topics = "transfer.events.request", groupId = "wallet-service-group")
    public void handleTransferRequest(TransferRequestEvent event) {
        log.info("📨 Processing transfer request");
        transferOrchestrator.executeTransfer(event.getFromWalletId(), event.getToWalletId(), event.getAmount(),
                event.getTransactionId());
    }

    @KafkaListener(topics = "transfer.events.success", groupId = "notification-group")
    public void handleTransferSuccess(String transactionId) {
        log.info("🎉 Transfer successful: {}", transactionId);
        Transaction txn = transactionRepository.findByTransactionId(transactionId).orElse(null);
        if (txn != null) {
            auditService.logTransferSuccess(txn);
        }
        // TODO: Broadcast via SSE to frontend
    }

    @KafkaListener(topics = "transfer.events.failed", groupId = "notification-group")
    public void handleTransferFailed(String transactionId) {
        log.error("❌ Transfer failed: {}", transactionId);
        Transaction txn = transactionRepository.findByTransactionId(transactionId).orElse(null);
        if (txn != null) {
            auditService.logTransferFailed(txn.getTransactionId(), txn.getCancelReason());
        } else {
            auditService.logTransferFailed(transactionId, "Unknown reason");
        }
        // TODO: Broadcast via SSE to frontend
    }

    @KafkaListener(topics = "transfer.events.rollback", groupId = "notification-group")
    public void handleTransferRollback(String transactionId) {
        log.warn("⚠️ Transfer rolled back: {}", transactionId);

        // Log rollback for compliance
        Transaction txn = transactionRepository.findByTransactionId(transactionId).orElse(null);
        if (txn != null) {
            // slackService.alertOps("Transfer rollback: " + transactionId + " - " +
            // txn.getCancelReason());
            auditService.logRollback(txn);
        }

        // TODO: Alert ops team (Slack integration)
    }
}
