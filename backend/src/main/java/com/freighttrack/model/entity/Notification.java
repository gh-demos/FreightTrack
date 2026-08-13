package com.freighttrack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@JsonIgnoreProperties({"shipment"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 500)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime readAt;

    @Column(length = 500)
    private String deliveryChannel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum NotificationType {
        SHIPMENT_CREATED, SHIPMENT_PICKED_UP, SHIPMENT_IN_TRANSIT, 
        SHIPMENT_OUT_FOR_DELIVERY, SHIPMENT_DELIVERED, DELIVERY_EXCEPTION, 
        DELIVERY_DELAY, DELIVERY_FAILED
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, DELIVERED, READ
    }
}
