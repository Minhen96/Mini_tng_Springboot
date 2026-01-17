package com.example.mhpractice.features.wallet.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferSuccessEvent {

    private String transactionId; // Idempotency key
    private UUID fromWalletId; // Sender wallet
    private UUID toWalletId; // Receiver wallet (needed for next step!)
    private BigDecimal amount; // Transfer amount
    private LocalDateTime timestamp; // When event occurred
    private String eventId; // Unique event ID

    // Helper factory method
    public static TransferSuccessEvent of(String txnId, UUID fromId, UUID toId, BigDecimal amt) {
        return TransferSuccessEvent.builder()
                .transactionId(txnId)
                .fromWalletId(fromId)
                .toWalletId(toId)
                .amount(amt)
                .timestamp(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();
    }
}