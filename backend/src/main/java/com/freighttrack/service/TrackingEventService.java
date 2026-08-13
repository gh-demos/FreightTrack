package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.TrackingEvent;
import com.freighttrack.repository.TrackingEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingEventService implements CrudService<TrackingEvent, Long> {

    private final TrackingEventRepository trackingEventRepository;

    public TrackingEventService(TrackingEventRepository trackingEventRepository) {
        this.trackingEventRepository = trackingEventRepository;
    }

    @Override
    public List<TrackingEvent> findAll() {
        return trackingEventRepository.findAll();
    }

    @Override
    public TrackingEvent findById(Long id) {
        return trackingEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking event not found: " + id));
    }

    @Override
    public TrackingEvent create(TrackingEvent entity) {
        entity.setId(null);
        return trackingEventRepository.save(entity);
    }

    @Override
    public TrackingEvent update(Long id, TrackingEvent entity) {
        TrackingEvent existing = findById(id);
        existing.setShipment(entity.getShipment());
        existing.setEventType(entity.getEventType());
        existing.setLocation(entity.getLocation());
        existing.setLatitude(entity.getLatitude());
        existing.setLongitude(entity.getLongitude());
        existing.setDescription(entity.getDescription());
        existing.setEventTime(entity.getEventTime());
        return trackingEventRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        TrackingEvent existing = findById(id);
        trackingEventRepository.delete(existing);
    }
}
