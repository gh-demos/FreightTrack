package com.freighttrack.repository;

import com.freighttrack.model.entity.Shipment;
import com.freighttrack.model.entity.ShippingException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingExceptionRepository extends JpaRepository<ShippingException, Long> {
    List<ShippingException> findByShipment(Shipment shipment);
    List<ShippingException> findByStatus(ShippingException.ExceptionStatus status);
}
