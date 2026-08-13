package com.freighttrack.repository;

import com.freighttrack.model.entity.Notification;
import com.freighttrack.model.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
    List<Notification> findByShipment(Shipment shipment);
    List<Notification> findByStatus(Notification.NotificationStatus status);
}
