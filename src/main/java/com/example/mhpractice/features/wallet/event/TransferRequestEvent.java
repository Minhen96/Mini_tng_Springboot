package com.example.mhpractice.features.wallet.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestEvent {

    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;

    // Helper factory method
    public static TransferRequestEvent of(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        return TransferRequestEvent.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .build();
    }
}