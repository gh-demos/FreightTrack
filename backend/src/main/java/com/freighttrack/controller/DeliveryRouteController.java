package com.freighttrack.controller;

import com.freighttrack.model.entity.DeliveryRoute;
import com.freighttrack.service.DeliveryRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    public DeliveryRouteController(DeliveryRouteService deliveryRouteService) {
        this.deliveryRouteService = deliveryRouteService;
    }

    @GetMapping
    public List<DeliveryRoute> getAll() {
        return deliveryRouteService.findAll();
    }

    @GetMapping("/{id}")
    public DeliveryRoute getById(@PathVariable Long id) {
        return deliveryRouteService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryRoute create(@Valid @RequestBody DeliveryRoute route) {
        return deliveryRouteService.create(route);
    }

    @PutMapping("/{id}")
    public DeliveryRoute update(@PathVariable Long id, @Valid @RequestBody DeliveryRoute route) {
        return deliveryRouteService.update(id, route);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deliveryRouteService.delete(id);
    }
}
