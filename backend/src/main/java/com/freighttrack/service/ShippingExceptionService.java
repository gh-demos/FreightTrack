package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.ShippingException;
import com.freighttrack.repository.ShippingExceptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingExceptionService implements CrudService<ShippingException, Long> {

    private final ShippingExceptionRepository shippingExceptionRepository;

    public ShippingExceptionService(ShippingExceptionRepository shippingExceptionRepository) {
        this.shippingExceptionRepository = shippingExceptionRepository;
    }

    @Override
    public List<ShippingException> findAll() {
        return shippingExceptionRepository.findAll();
    }

    @Override
    public ShippingException findById(Long id) {
        return shippingExceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping exception not found: " + id));
    }

    @Override
    public ShippingException create(ShippingException entity) {
        entity.setId(null);
        return shippingExceptionRepository.save(entity);
    }

    @Override
    public ShippingException update(Long id, ShippingException entity) {
        ShippingException existing = findById(id);
        existing.setShipment(entity.getShipment());
        existing.setExceptionType(entity.getExceptionType());
        existing.setDescription(entity.getDescription());
        existing.setLocation(entity.getLocation());
        existing.setStatus(entity.getStatus());
        existing.setReportedAt(entity.getReportedAt());
        existing.setResolvedAt(entity.getResolvedAt());
        existing.setResolutionNotes(entity.getResolutionNotes());
        existing.setReportedBy(entity.getReportedBy());
        return shippingExceptionRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        ShippingException existing = findById(id);
        shippingExceptionRepository.delete(existing);
    }
}
