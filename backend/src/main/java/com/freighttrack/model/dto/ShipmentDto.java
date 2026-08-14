package com.freighttrack.model.dto;

import com.freighttrack.model.entity.Shipment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShipmentDto(
        Long id,
        String trackingNumber,
        Long customerId,
        String customerName,
        Long assignedDriverId,
        String assignedDriverName,
        String origin,
        String destination,
        BigDecimal weight,
        String weightUnit,
        BigDecimal value,
        String currency,
        String description,
        Shipment.ShipmentStatus status,
        LocalDate pickupDate,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
