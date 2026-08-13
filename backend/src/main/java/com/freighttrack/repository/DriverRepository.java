package com.freighttrack.repository;

import com.freighttrack.model.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    List<Driver> findByStatus(Driver.DriverStatus status);
    List<Driver> findByLicenseExpiryDateBefore(LocalDate date);
}
