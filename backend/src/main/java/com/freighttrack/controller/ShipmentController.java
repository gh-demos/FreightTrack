package com.freighttrack.controller;

import com.freighttrack.model.dto.SimulationRunResult;
import com.freighttrack.model.dto.ShipmentDto;
import com.freighttrack.model.dto.ShipmentStatusUpdateRequest;
import com.freighttrack.model.dto.ShipmentUpsertRequest;
import com.freighttrack.model.entity.Shipment;
import com.freighttrack.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public Page<ShipmentDto> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Shipment.ShipmentStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return shipmentService.findPage(q, status, pageable);
    }

    @GetMapping("/{id}")
    public ShipmentDto getById(@PathVariable Long id) {
        return shipmentService.findDtoById(id);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ShipmentDto getByTrackingNumber(@PathVariable String trackingNumber) {
        return shipmentService.findByTrackingNumber(trackingNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentDto create(@Valid @RequestBody ShipmentUpsertRequest shipment) {
        return shipmentService.create(shipment);
    }

    @PutMapping("/{id}")
    public ShipmentDto update(@PathVariable Long id, @Valid @RequestBody ShipmentUpsertRequest shipment) {
        return shipmentService.update(id, shipment);
    }

    @PatchMapping("/{id}/status")
    public ShipmentDto updateStatus(@PathVariable Long id,
                                    @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return shipmentService.updateStatus(id, request.status());
    }

    @PostMapping("/simulation/run")
    public SimulationRunResult runSimulationNow() {
        return shipmentService.runSimulationCycle();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shipmentService.delete(id);
    }
}
