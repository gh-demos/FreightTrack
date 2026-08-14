package com.freighttrack.controller;

import com.freighttrack.model.entity.ShippingException;
import com.freighttrack.service.ShippingExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/shipping-exceptions", "/exceptions"})
public class ShippingExceptionController {

    private final ShippingExceptionService shippingExceptionService;

    public ShippingExceptionController(ShippingExceptionService shippingExceptionService) {
        this.shippingExceptionService = shippingExceptionService;
    }

    @GetMapping
    public List<ShippingException> getAll() {
        return shippingExceptionService.findAll();
    }

    @GetMapping("/{id}")
    public ShippingException getById(@PathVariable Long id) {
        return shippingExceptionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShippingException create(@Valid @RequestBody ShippingException shippingException) {
        return shippingExceptionService.create(shippingException);
    }

    @PutMapping("/{id}")
    public ShippingException update(@PathVariable Long id, @Valid @RequestBody ShippingException shippingException) {
        return shippingExceptionService.update(id, shippingException);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shippingExceptionService.delete(id);
    }
}
