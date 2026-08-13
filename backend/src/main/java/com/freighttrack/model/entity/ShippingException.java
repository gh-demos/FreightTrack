package com.freighttrack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_exceptions")
@JsonIgnoreProperties({"shipment"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingException {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExceptionType exceptionType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExceptionStatus status = ExceptionStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(length = 500)
    private String resolutionNotes;

    @Column(length = 100)
    private String reportedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ExceptionType {
        DELIVERY_DELAY, DAMAGED_GOODS, LOST_SHIPMENT, ADDRESS_ISSUE, 
        WEATHER_DELAY, VEHICLE_BREAKDOWN, CUSTOMS_ISSUE, OTHER
    }

    public enum ExceptionStatus {
        OPEN, IN_PROGRESS, RESOLVED, ESCALATED
    }
}
