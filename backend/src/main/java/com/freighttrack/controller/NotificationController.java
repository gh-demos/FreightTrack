package com.freighttrack.controller;

import com.freighttrack.model.entity.Notification;
import com.freighttrack.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.findAll();
    }

    @GetMapping("/{id}")
    public Notification getById(@PathVariable Long id) {
        return notificationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification create(@Valid @RequestBody Notification notification) {
        return notificationService.create(notification);
    }

    @PutMapping("/{id}")
    public Notification update(@PathVariable Long id, @Valid @RequestBody Notification notification) {
        return notificationService.update(id, notification);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }
}
