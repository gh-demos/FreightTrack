package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.Notification;
import com.freighttrack.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService implements CrudService<Notification, Long> {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }

    @Override
    public Notification create(Notification entity) {
        entity.setId(null);
        return notificationRepository.save(entity);
    }

    @Override
    public Notification update(Long id, Notification entity) {
        Notification existing = findById(id);
        existing.setRecipient(entity.getRecipient());
        existing.setType(entity.getType());
        existing.setMessage(entity.getMessage());
        existing.setSubject(entity.getSubject());
        existing.setShipment(entity.getShipment());
        existing.setStatus(entity.getStatus());
        existing.setSentAt(entity.getSentAt());
        existing.setReadAt(entity.getReadAt());
        existing.setDeliveryChannel(entity.getDeliveryChannel());
        return notificationRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Notification existing = findById(id);
        notificationRepository.delete(existing);
    }
}
