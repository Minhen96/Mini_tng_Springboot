package com.example.mhpractice.features.wallet.event;

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
public class TransferFailedEvent {

    private String transactionId; // Idempotency key
    private String reason; // Transfer amount
    private LocalDateTime timestamp; // When event occurred
    private String eventId; // Unique event ID

    // Helper factory method
    public static TransferFailedEvent of(String txnId, String reason) {
        return TransferFailedEvent.builder()
                .transactionId(txnId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();
    }
}