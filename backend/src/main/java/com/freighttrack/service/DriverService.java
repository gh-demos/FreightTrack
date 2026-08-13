package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.Driver;
import com.freighttrack.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService implements CrudService<Driver, Long> {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    @Override
    public Driver findById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    @Override
    public Driver create(Driver entity) {
        entity.setId(null);
        return driverRepository.save(entity);
    }

    @Override
    public Driver update(Long id, Driver entity) {
        Driver existing = findById(id);
        existing.setFirstName(entity.getFirstName());
        existing.setLastName(entity.getLastName());
        existing.setLicenseNumber(entity.getLicenseNumber());
        existing.setLicenseExpiryDate(entity.getLicenseExpiryDate());
        existing.setEmail(entity.getEmail());
        existing.setPhone(entity.getPhone());
        existing.setLicenseClass(entity.getLicenseClass());
        existing.setAddress(entity.getAddress());
        existing.setCity(entity.getCity());
        existing.setState(entity.getState());
        existing.setPostalCode(entity.getPostalCode());
        existing.setStatus(entity.getStatus());
        existing.setActive(entity.getActive());
        return driverRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Driver existing = findById(id);
        driverRepository.delete(existing);
    }
}
