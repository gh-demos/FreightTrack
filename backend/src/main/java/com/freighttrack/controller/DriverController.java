package com.freighttrack.controller;

import com.freighttrack.model.entity.Driver;
import com.freighttrack.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<Driver> getAll() {
        return driverService.findAll();
    }

    @GetMapping("/{id}")
    public Driver getById(@PathVariable Long id) {
        return driverService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Driver create(@Valid @RequestBody Driver driver) {
        return driverService.create(driver);
    }

    @PutMapping("/{id}")
    public Driver update(@PathVariable Long id, @Valid @RequestBody Driver driver) {
        return driverService.update(id, driver);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }
}
