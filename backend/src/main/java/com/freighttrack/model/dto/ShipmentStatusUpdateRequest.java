package com.freighttrack.model.dto;

import com.freighttrack.model.entity.Shipment;
import jakarta.validation.constraints.NotNull;

public record ShipmentStatusUpdateRequest(
        @NotNull Shipment.ShipmentStatus status
) {
}
