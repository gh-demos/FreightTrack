package com.freighttrack.model.dto;

import com.freighttrack.model.entity.Shipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShipmentUpsertRequest(
        @NotBlank @Size(max = 50) String trackingNumber,
        @NotNull Long customerId,
        @NotBlank @Size(max = 255) String origin,
        @NotBlank @Size(max = 255) String destination,
        @NotNull @Positive BigDecimal weight,
        @Size(max = 50) String weightUnit,
        @NotNull @Positive BigDecimal value,
        @NotBlank @Size(max = 50) String currency,
        @Size(max = 500) String description,
        @NotNull Shipment.ShipmentStatus status,
        @NotNull LocalDate pickupDate,
        @NotNull LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        Long assignedDriverId
) {
}
