package com.freighttrack.repository;

import com.freighttrack.model.entity.DeliveryRoute;
import com.freighttrack.model.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    Optional<DeliveryRoute> findByRouteCode(String routeCode);
    List<DeliveryRoute> findByDriverAndRouteDate(Driver driver, LocalDate routeDate);
    List<DeliveryRoute> findByStatus(DeliveryRoute.RouteStatus status);
}
