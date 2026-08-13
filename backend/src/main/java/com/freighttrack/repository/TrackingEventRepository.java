package com.freighttrack.repository;

import com.freighttrack.model.entity.Shipment;
import com.freighttrack.model.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByShipmentOrderByEventTimeDesc(Shipment shipment);
}
