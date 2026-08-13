package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.Shipment;
import com.freighttrack.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipmentService implements CrudService<Shipment, Long> {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    @Override
    public Shipment findById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    public Shipment findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for tracking number: " + trackingNumber));
    }

    @Override
    public Shipment create(Shipment entity) {
        entity.setId(null);
        return shipmentRepository.save(entity);
    }

    @Override
    public Shipment update(Long id, Shipment entity) {
        Shipment existing = findById(id);
        existing.setTrackingNumber(entity.getTrackingNumber());
        existing.setCustomer(entity.getCustomer());
        existing.setOrigin(entity.getOrigin());
        existing.setDestination(entity.getDestination());
        existing.setWeight(entity.getWeight());
        existing.setWeightUnit(entity.getWeightUnit());
        existing.setValue(entity.getValue());
        existing.setCurrency(entity.getCurrency());
        existing.setDescription(entity.getDescription());
        existing.setStatus(entity.getStatus());
        existing.setPickupDate(entity.getPickupDate());
        existing.setExpectedDeliveryDate(entity.getExpectedDeliveryDate());
        existing.setActualDeliveryDate(entity.getActualDeliveryDate());
        existing.setAssignedDriver(entity.getAssignedDriver());
        return shipmentRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Shipment existing = findById(id);
        shipmentRepository.delete(existing);
    }
}
