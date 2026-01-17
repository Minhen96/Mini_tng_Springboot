package com.example.mhpractice.common.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mhpractice.common.model.AuditLog;

/**
 * Repository for audit logs.
 * 
 * IMPORTANT: Only SELECT and INSERT operations allowed.
 * NO UPDATE or DELETE methods should be added!
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find all audit logs for a specific transaction
     */
    List<AuditLog> findByTransactionIdOrderByTimestampAsc(String transactionId);

    /**
     * Find audit logs within a time range (for compliance reports)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp ASC")
    List<AuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find audit logs by severity (for alerting)
     */
    List<AuditLog> findBySeverityOrderByTimestampDesc(String severity);

    /**
     * Find recent high-severity events
     */
    @Query("SELECT a FROM AuditLog a WHERE a.severity IN ('HIGH', 'CRITICAL') AND a.timestamp > :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentHighSeverityEvents(@Param("since") LocalDateTime since);

    /**
     * Find all actions for a specific user (sender)
     */
    List<AuditLog> findByFromUser_IdOrderByTimestampDesc(UUID fromUserId);

    /**
     * Find all actions for a specific user (receiver)
     */
    List<AuditLog> findByToUser_IdOrderByTimestampDesc(UUID toUserId);

    /**
     * Find all actions of a specific type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
