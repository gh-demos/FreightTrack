package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.dto.SimulationRunResult;
import com.freighttrack.model.dto.ShipmentDto;
import com.freighttrack.model.dto.ShipmentUpsertRequest;
import com.freighttrack.model.entity.Customer;
import com.freighttrack.model.entity.Driver;
import com.freighttrack.model.entity.Shipment;
import com.freighttrack.model.entity.TrackingEvent;
import com.freighttrack.repository.CustomerRepository;
import com.freighttrack.repository.DriverRepository;
import com.freighttrack.repository.ShipmentRepository;
import com.freighttrack.repository.TrackingEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ShipmentService implements CrudService<Shipment, Long> {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final TrackingEventRepository trackingEventRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           CustomerRepository customerRepository,
                           DriverRepository driverRepository,
                           TrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.driverRepository = driverRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Override
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    public Page<ShipmentDto> findPage(String q, Shipment.ShipmentStatus status, Pageable pageable) {
        Page<Shipment> shipments;
        String query = q == null ? "" : q.trim();
        boolean hasQ = !query.isBlank();

        if (hasQ && status != null) {
            shipments = shipmentRepository.searchByStatus(query, status, pageable);
        } else if (hasQ) {
            shipments = shipmentRepository.search(query, pageable);
        } else if (status != null) {
            shipments = shipmentRepository.findByStatus(status, pageable);
        } else {
            shipments = shipmentRepository.findAll(pageable);
        }

        return shipments.map(this::toDto);
    }

    @Override
    public Shipment findById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    public ShipmentDto findDtoById(Long id) {
        return toDto(findById(id));
    }

    public ShipmentDto findByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for tracking number: " + trackingNumber));
        return toDto(shipment);
    }

    public ShipmentDto create(ShipmentUpsertRequest request) {
        Shipment shipment = new Shipment();
        apply(shipment, request);
        shipment.setId(null);
        return toDto(shipmentRepository.save(shipment));
    }

    public ShipmentDto update(Long id, ShipmentUpsertRequest request) {
        Shipment existing = findById(id);
        apply(existing, request);
        return toDto(shipmentRepository.save(existing));
    }

    @Transactional
    public ShipmentDto updateStatus(Long id, Shipment.ShipmentStatus status) {
        Shipment shipment = findById(id);
        shipment.setStatus(status);
        if (status == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(LocalDate.now());
        }

        Shipment saved = shipmentRepository.save(shipment);
        appendTrackingEvent(saved, mapStatusToEventType(status), "Shipment status updated to " + status);
        return toDto(saved);
    }

    @Transactional
    public SimulationRunResult runSimulationCycle() {
        int progressed = 0;
        progressed += progressShipments(Shipment.ShipmentStatus.PENDING, Shipment.ShipmentStatus.SHIPPED,
                TrackingEvent.EventType.PICKED_UP, "Shipment marked as shipped");
        progressed += progressShipments(Shipment.ShipmentStatus.SHIPPED, Shipment.ShipmentStatus.IN_TRANSIT,
                TrackingEvent.EventType.IN_TRANSIT, "Shipment departed origin hub");
        progressed += progressShipments(Shipment.ShipmentStatus.IN_TRANSIT, Shipment.ShipmentStatus.OUT_FOR_DELIVERY,
                TrackingEvent.EventType.OUT_FOR_DELIVERY, "Shipment out for delivery");
        progressed += progressShipments(Shipment.ShipmentStatus.OUT_FOR_DELIVERY, Shipment.ShipmentStatus.DELIVERED,
                TrackingEvent.EventType.DELIVERED, "Shipment delivered");

        Shipment created = createIncomingShipment();
        return new SimulationRunResult(
                progressed,
                created == null ? null : created.getId(),
                created == null ? null : created.getTrackingNumber()
        );
    }

    @Scheduled(cron = "${freighttrack.simulation.cron:0 0 */5 * * *}")
    @Transactional
    public void runScheduledSimulation() {
        runSimulationCycle();
    }

    @Override
    public Shipment create(Shipment entity) {
        entity.setId(null);
        return shipmentRepository.save(entity);
    }

    @Override
    public Shipment update(Long id, Shipment entity) {
        Shipment existing = findById(id);
        existing.setTrackingNumber(entity.getTrackingNumber());
        existing.setCustomer(entity.getCustomer());
        existing.setOrigin(entity.getOrigin());
        existing.setDestination(entity.getDestination());
        existing.setWeight(entity.getWeight());
        existing.setWeightUnit(entity.getWeightUnit());
        existing.setValue(entity.getValue());
        existing.setCurrency(entity.getCurrency());
        existing.setDescription(entity.getDescription());
        existing.setStatus(entity.getStatus());
        existing.setPickupDate(entity.getPickupDate());
        existing.setExpectedDeliveryDate(entity.getExpectedDeliveryDate());
        existing.setActualDeliveryDate(entity.getActualDeliveryDate());
        existing.setAssignedDriver(entity.getAssignedDriver());
        return shipmentRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Shipment existing = findById(id);
        shipmentRepository.delete(existing);
    }

    private ShipmentDto toDto(Shipment shipment) {
        Customer customer = shipment.getCustomer();
        Driver driver = shipment.getAssignedDriver();

        String driverName = null;
        if (driver != null) {
            String firstName = driver.getFirstName() == null ? "" : driver.getFirstName();
            String lastName = driver.getLastName() == null ? "" : driver.getLastName();
            driverName = (firstName + " " + lastName).trim();
            if (driverName.isBlank()) {
                driverName = null;
            }
        }

        return new ShipmentDto(
                shipment.getId(),
                shipment.getTrackingNumber(),
                customer == null ? null : customer.getId(),
                customer == null ? null : customer.getName(),
                driver == null ? null : driver.getId(),
                driverName,
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getWeight(),
                shipment.getWeightUnit(),
                shipment.getValue(),
                shipment.getCurrency(),
                shipment.getDescription(),
                shipment.getStatus(),
                shipment.getPickupDate(),
                shipment.getExpectedDeliveryDate(),
                shipment.getActualDeliveryDate(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }

    private void apply(Shipment shipment, ShipmentUpsertRequest request) {
        shipment.setTrackingNumber(request.trackingNumber());
        shipment.setCustomer(resolveCustomer(request.customerId()));
        shipment.setOrigin(request.origin());
        shipment.setDestination(request.destination());
        shipment.setWeight(request.weight());
        shipment.setWeightUnit(request.weightUnit());
        shipment.setValue(request.value());
        shipment.setCurrency(request.currency());
        shipment.setDescription(request.description());
        shipment.setStatus(request.status());
        shipment.setPickupDate(request.pickupDate());
        shipment.setExpectedDeliveryDate(request.expectedDeliveryDate());
        shipment.setActualDeliveryDate(request.actualDeliveryDate());
        shipment.setAssignedDriver(resolveDriver(request.assignedDriverId()));
    }

    private Customer resolveCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    private Driver resolveDriver(Long driverId) {
        if (driverId == null) {
            return null;
        }

        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
    }

    private int progressShipments(Shipment.ShipmentStatus from,
                                  Shipment.ShipmentStatus to,
                                  TrackingEvent.EventType eventType,
                                  String description) {
        List<Shipment> shipments = shipmentRepository.findByStatus(from);
        if (shipments.isEmpty()) {
            return 0;
        }

        AtomicInteger count = new AtomicInteger();
        shipments.forEach(shipment -> {
            shipment.setStatus(to);
            if (to == Shipment.ShipmentStatus.DELIVERED) {
                shipment.setActualDeliveryDate(LocalDate.now());
            }
            Shipment saved = shipmentRepository.save(shipment);
            appendTrackingEvent(saved, eventType, description);
            count.incrementAndGet();
        });

        return count.get();
    }

    private Shipment createIncomingShipment() {
        Customer customer = customerRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
        if (customer == null) {
            return null;
        }

        Driver driver = driverRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setCustomer(customer);
        shipment.setAssignedDriver(driver);
        shipment.setOrigin("Seattle, WA");
        shipment.setDestination("Portland, OR");
        shipment.setWeight(new java.math.BigDecimal("600.00"));
        shipment.setWeightUnit("KG");
        shipment.setValue(new java.math.BigDecimal("18000.00"));
        shipment.setCurrency("USD");
        shipment.setDescription("Auto-generated incoming shipment");
        shipment.setStatus(Shipment.ShipmentStatus.SHIPPED);
        shipment.setPickupDate(LocalDate.now());
        shipment.setExpectedDeliveryDate(LocalDate.now().plusDays(2));

        Shipment saved = shipmentRepository.save(shipment);
        appendTrackingEvent(saved, TrackingEvent.EventType.PICKED_UP, "Incoming shipment registered and shipped");
        return saved;
    }

    private String generateTrackingNumber() {
        String base = "SIM-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        int suffix = (int) (Math.random() * 9000) + 1000;
        return base + "-" + suffix;
    }

    private void appendTrackingEvent(Shipment shipment, TrackingEvent.EventType eventType, String description) {
        TrackingEvent event = new TrackingEvent();
        event.setShipment(shipment);
        event.setEventType(eventType);
        event.setLocation(shipment.getDestination() == null ? "FreightTrack Hub" : shipment.getDestination());
        event.setDescription(description);
        event.setEventTime(LocalDateTime.now());
        trackingEventRepository.save(event);
    }

    private TrackingEvent.EventType mapStatusToEventType(Shipment.ShipmentStatus status) {
        return switch (status) {
            case SHIPPED, PICKED_UP -> TrackingEvent.EventType.PICKED_UP;
            case IN_TRANSIT -> TrackingEvent.EventType.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> TrackingEvent.EventType.OUT_FOR_DELIVERY;
            case DELIVERED -> TrackingEvent.EventType.DELIVERED;
            case CANCELLED -> TrackingEvent.EventType.CANCELLED;
            case RETURNED -> TrackingEvent.EventType.RETURNED;
            default -> TrackingEvent.EventType.CREATED;
        };
    }
}
