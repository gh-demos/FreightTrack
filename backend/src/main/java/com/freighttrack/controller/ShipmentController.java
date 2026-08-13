package com.freighttrack.controller;

import com.freighttrack.model.entity.Shipment;
import com.freighttrack.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<Shipment> getAll() {
        return shipmentService.findAll();
    }

    @GetMapping("/{id}")
    public Shipment getById(@PathVariable Long id) {
        return shipmentService.findById(id);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public Shipment getByTrackingNumber(@PathVariable String trackingNumber) {
        return shipmentService.findByTrackingNumber(trackingNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shipment create(@Valid @RequestBody Shipment shipment) {
        return shipmentService.create(shipment);
    }

    @PutMapping("/{id}")
    public Shipment update(@PathVariable Long id, @Valid @RequestBody Shipment shipment) {
        return shipmentService.update(id, shipment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shipmentService.delete(id);
    }
}
