package com.freighttrack.repository;

import com.freighttrack.model.entity.Customer;
import com.freighttrack.model.entity.Driver;
import com.freighttrack.model.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @EntityGraph(attributePaths = {"customer", "assignedDriver"})
    @Override
    Page<Shipment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "assignedDriver"})
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @EntityGraph(attributePaths = {"customer", "assignedDriver"})
    Page<Shipment> findByStatus(Shipment.ShipmentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "assignedDriver"})
    @Query("""
        select s from Shipment s
        left join s.customer c
        where lower(s.trackingNumber) like lower(concat('%', :q, '%'))
           or lower(s.origin) like lower(concat('%', :q, '%'))
           or lower(s.destination) like lower(concat('%', :q, '%'))
           or lower(coalesce(c.name, '')) like lower(concat('%', :q, '%'))
        """)
    Page<Shipment> search(@Param("q") String q, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "assignedDriver"})
    @Query("""
        select s from Shipment s
        left join s.customer c
        where s.status = :status
          and (
          lower(s.trackingNumber) like lower(concat('%', :q, '%'))
           or lower(s.origin) like lower(concat('%', :q, '%'))
           or lower(s.destination) like lower(concat('%', :q, '%'))
           or lower(coalesce(c.name, '')) like lower(concat('%', :q, '%'))
          )
        """)
    Page<Shipment> searchByStatus(@Param("q") String q,
                  @Param("status") Shipment.ShipmentStatus status,
                  Pageable pageable);

    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
    List<Shipment> findByCustomer(Customer customer);
    List<Shipment> findByAssignedDriver(Driver driver);
    List<Shipment> findByExpectedDeliveryDate(LocalDate expectedDeliveryDate);
}
