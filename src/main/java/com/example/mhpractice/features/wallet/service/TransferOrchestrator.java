package com.example.mhpractice.features.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Transactional
    public void executeTransfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        try {

            walletService.transferOut(fromWalletId, amount, transactionId);
            walletService.transferIn(toWalletId, amount, transactionId);
            walletService.confirmTransfer(transactionId);

            kafkaTemplate.send("transfer.events.success", transactionId);

        } catch (BusinessException e) {
            log.error("Transfer failed: {}", e.getMessage());
            rollBackTransfer(transactionId, e.getMessage());
            kafkaTemplate.send("transfer.events.failed", transactionId);
            throw e;
        }

        catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            kafkaTemplate.send("transfer.events.failed", transactionId);
            rollBackTransfer(transactionId, e.getMessage());
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
