package com.freighttrack.controller;

import com.freighttrack.model.entity.TrackingEvent;
import com.freighttrack.service.TrackingEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracking-events")
public class TrackingEventController {

    private final TrackingEventService trackingEventService;

    public TrackingEventController(TrackingEventService trackingEventService) {
        this.trackingEventService = trackingEventService;
    }

    @GetMapping
    public List<TrackingEvent> getAll() {
        return trackingEventService.findAll();
    }

    @GetMapping("/{id}")
    public TrackingEvent getById(@PathVariable Long id) {
        return trackingEventService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingEvent create(@Valid @RequestBody TrackingEvent event) {
        return trackingEventService.create(event);
    }

    @PutMapping("/{id}")
    public TrackingEvent update(@PathVariable Long id, @Valid @RequestBody TrackingEvent event) {
        return trackingEventService.update(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        trackingEventService.delete(id);
    }
}
