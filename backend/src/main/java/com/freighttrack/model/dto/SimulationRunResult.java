package com.freighttrack.model.dto;

public record SimulationRunResult(
        int progressedShipments,
        Long createdShipmentId,
        String createdShipmentTrackingNumber
) {
}
