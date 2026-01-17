package com.example.mhpractice.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.wallet.model.Wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audit Log Entity - Cross-Feature Compliance Logging
 * 
 * CRITICAL: This is an append-only table for compliance.
 * - NEVER update records (no @UpdateTimestamp)
 * - NEVER delete records (kept forever for legal/compliance)
 * - Records every important event in the system
 * 
 * Used by ALL features:
 * - Wallet transfers (TRANSFER_INITIATED, TRANSFER_SUCCESS, etc.)
 * - User authentication (USER_LOGIN, USER_LOGOUT, etc.)
 * - Admin actions (ADMIN_DELETE_USER, etc.)
 * - Any critical business event
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * When the event occurred
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * What action occurred
     * Examples:
     * - Wallet: TRANSFER_INITIATED, TRANSFER_SUCCESS, TRANSFER_ROLLBACK
     * - User: USER_LOGIN, USER_LOGOUT, PASSWORD_CHANGED
     * - Admin: ADMIN_DELETE_USER, ADMIN_FREEZE_ACCOUNT
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Transaction ID being audited (for transfers)
     */
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    /**
     * The sender.
     */
    @ManyToOne
    @JoinColumn(name = "from_user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "password", "emailVerified", "status", "createdAt",
            "updatedAt" })
    private User fromUser;

    /**
     * The receiver.
     */
    @ManyToOne
    @JoinColumn(name = "to_user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "password", "emailVerified", "status", "createdAt",
            "updatedAt" })
    private User toUser;

    /**
     * Source wallet (for transfers)
     */
    @ManyToOne
    @JoinColumn(name = "from_wallet_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "user", "createdAt", "updatedAt" })
    private Wallet fromWallet;

    /**
     * Destination wallet (for transfers)
     */
    @ManyToOne
    @JoinColumn(name = "to_wallet_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "user", "createdAt", "updatedAt" })
    private Wallet toWallet;

    /**
     * Amount involved (for financial transactions)
     */
    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * Current status at time of audit
     */
    @Column(name = "status", length = 50)
    private String status;

    /**
     * Severity level for alerting
     * LOW, MEDIUM, HIGH, CRITICAL
     */
    @Column(name = "severity", length = 20)
    @Builder.Default
    private String severity = "LOW";

    /**
     * IP address of the request (for security)
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * User agent / device info
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Additional metadata (JSON format)
     * Can store full snapshots, error messages, domain-specific data
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Human-readable description
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Auto-populated timestamp (for queries)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
