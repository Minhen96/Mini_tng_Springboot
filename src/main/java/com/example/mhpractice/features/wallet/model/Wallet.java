package com.example.mhpractice.features.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.mhpractice.features.user.models.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @com.fasterxml.jackson.annotation.JsonIgnore // Prevent infinite loop in JSON serialization
    private User user;

    @Column(precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unreleasedBalance = BigDecimal.ZERO;

    @Column(name = "is_frozen", nullable = false)
    @Builder.Default
    private boolean isFrozen = false;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.subtract(frozenBalance).compareTo(amount) >= 0;
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void addFrozenBalance(BigDecimal amount) {
        this.frozenBalance = this.frozenBalance.add(amount);
    }

    public void addUnreleasedBalance(BigDecimal amount) {
        this.unreleasedBalance = this.unreleasedBalance.add(amount);
    }

    public void deductBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void deductFrozenBalance(BigDecimal amount) {
        this.frozenBalance = this.frozenBalance.subtract(amount);
    }

    public void deductUnreleasedBalance(BigDecimal amount) {
        this.unreleasedBalance = this.unreleasedBalance.subtract(amount);
    }

    public void releaseUnreleasedBalance(BigDecimal amount) {
        this.unreleasedBalance = this.unreleasedBalance.subtract(amount);
        this.balance = this.balance.add(amount);
    }

    public void updateIsFrozen(boolean isFrozen) {
        this.isFrozen = isFrozen;
    }

}
