package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.DeliveryRoute;
import com.freighttrack.repository.DeliveryRouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryRouteService implements CrudService<DeliveryRoute, Long> {

    private final DeliveryRouteRepository deliveryRouteRepository;

    public DeliveryRouteService(DeliveryRouteRepository deliveryRouteRepository) {
        this.deliveryRouteRepository = deliveryRouteRepository;
    }

    @Override
    public List<DeliveryRoute> findAll() {
        return deliveryRouteRepository.findAll();
    }

    @Override
    public DeliveryRoute findById(Long id) {
        return deliveryRouteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
    }

    @Override
    public DeliveryRoute create(DeliveryRoute entity) {
        entity.setId(null);
        return deliveryRouteRepository.save(entity);
    }

    @Override
    public DeliveryRoute update(Long id, DeliveryRoute entity) {
        DeliveryRoute existing = findById(id);
        existing.setRouteCode(entity.getRouteCode());
        existing.setRouteName(entity.getRouteName());
        existing.setDriver(entity.getDriver());
        existing.setRouteDate(entity.getRouteDate());
        existing.setStartTime(entity.getStartTime());
        existing.setEndTime(entity.getEndTime());
        existing.setStartLocation(entity.getStartLocation());
        existing.setEndLocation(entity.getEndLocation());
        existing.setPlannedStops(entity.getPlannedStops());
        existing.setActualStops(entity.getActualStops());
        existing.setEstimatedDistance(entity.getEstimatedDistance());
        existing.setActualDistance(entity.getActualDistance());
        existing.setStatus(entity.getStatus());
        existing.setNotes(entity.getNotes());
        return deliveryRouteRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        DeliveryRoute existing = findById(id);
        deliveryRouteRepository.delete(existing);
    }
}
