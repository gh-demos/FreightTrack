package com.freighttrack.repository;

import com.freighttrack.model.entity.Customer;
import com.freighttrack.model.entity.Driver;
import com.freighttrack.model.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
    List<Shipment> findByCustomer(Customer customer);
    List<Shipment> findByAssignedDriver(Driver driver);
    List<Shipment> findByExpectedDeliveryDate(LocalDate expectedDeliveryDate);
}
