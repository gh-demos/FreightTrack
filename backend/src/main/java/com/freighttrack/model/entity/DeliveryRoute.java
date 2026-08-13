package com.freighttrack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "delivery_routes")
@JsonIgnoreProperties({"driver"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String routeCode;

    @Column(nullable = false, length = 100)
    private String routeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private LocalDate routeDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 255)
    private String startLocation;

    @Column(nullable = false, length = 255)
    private String endLocation;

    @Column(nullable = false)
    private Integer plannedStops;

    @Column
    private Integer actualStops = 0;

    @Column
    private Double estimatedDistance = 0.0;

    @Column
    private Double actualDistance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status = RouteStatus.PENDING;

    @Column(length = 500)
    private String notes;

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

    public enum RouteStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED
    }
}
