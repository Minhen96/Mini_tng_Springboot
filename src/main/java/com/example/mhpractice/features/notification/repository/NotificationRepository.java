package com.example.mhpractice.features.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mhpractice.features.notification.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
