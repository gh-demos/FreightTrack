package com.freighttrack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@JsonIgnoreProperties({"shipment"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(length = 50)
    private String latitude;

    @Column(length = 50)
    private String longitude;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }

    public enum EventType {
        CREATED, PICKED_UP, SORTED, IN_TRANSIT, OUT_FOR_DELIVERY, 
        DELIVERY_ATTEMPTED, DELIVERED, EXCEPTION_OCCURRED, RETURNED, CANCELLED
    }
}
