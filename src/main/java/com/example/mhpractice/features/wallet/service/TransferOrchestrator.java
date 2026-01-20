package com.example.mhpractice.features.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOrchestrator {

    /**
     * Orchestrates the wallet transfer saga.
     * 
     * CRITICAL PATH: ALL business logic for transfers is here.
     * This ensures correctness, auditability, and easy debugging.
     */
    private final WalletService walletService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    /**
     * Scenario 1: Happy Path (Success)
     * 1. DB Updates (transferOut, transferIn) execute.
     * 2. Kafka Send executes (successfully queues message).
     * 3. Method Ends → @Transactional commits the DB changes.
     * Result: DB updated, Message sent. ✅ OK.
     * 
     * Scenario 2: DB Error
     * 1. DB Updates fail (e.g., insufficient funds).
     * 2. Exception thrown.
     * 3. Kafka Send is SKIPPED (code jumps to catch).
     * 4. DB Rolls back.
     * Result: No DB change, No Message sent. ✅ OK.
     * 
     * Scenario 3: Kafka Send Error
     * 1. DB Updates execute OK.
     * 2. Kafka Send fails (e.g., broker down).
     * 3. Exception thrown.
     * 4. Method Ends (with exception).
     * 5. DB Transaction Rolls back (because exception bubbled up).
     * Result: DB changes reverted, No Message sent. ✅ OK.
     * 
     */
    @Timed(value = "wallet.transfer.time", percentiles = { 0.5, 0.9, 0.95, 0.99 }, description = "Wallet transfer time")
    @Transactional
    public void executeTransfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String transactionId) {
        try {
            meterRegistry.counter("wallet.transfer.total", "status", "init").increment();

            walletService.transferOut(fromWalletId, amount, transactionId);
            walletService.transferIn(toWalletId, amount, transactionId);
            walletService.confirmTransfer(transactionId);

            kafkaTemplate.send("transfer.events.success", transactionId);

            // Metric: Transfer Success
            meterRegistry.counter("wallet.transfer.total", "status", "success").increment();

        } catch (BusinessException e) {
            log.error("Transfer failed: {}", e.getMessage());
            rollBackTransfer(transactionId, e.getMessage());
            kafkaTemplate.send("transfer.events.failed", transactionId);

            // Metric: Transfer Failed (Business Rule)
            meterRegistry.counter("wallet.transfer.total", "status", "failed", "reason", e.getErrorCode().name())
                    .increment();
            throw e;
        }

        catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            kafkaTemplate.send("transfer.events.failed", transactionId);
            rollBackTransfer(transactionId, e.getMessage());

            // Metric: Transfer Failed (Unexpected)
            meterRegistry.counter("wallet.transfer.total", "status", "rollback", "reason", "internal_error")
                    .increment();
            throw new BusinessException(ErrorCode.TRANSACTION_INTERNAL_ERROR);
        }
    }

    private void rollBackTransfer(String transactionId, String reason) {
        try {

            walletService.cancelTransfer(transactionId, reason);

            kafkaTemplate.send("transfer.events.rollback", transactionId);

        } catch (Exception e) {
            log.error("Rollback failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TRANSACTION_INTERNAL_ERROR);
        }
    }
}
