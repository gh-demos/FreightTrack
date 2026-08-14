package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.dto.CustomerDto;
import com.freighttrack.model.dto.CustomerUpsertRequest;
import com.freighttrack.model.entity.Customer;
import com.freighttrack.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService implements CrudService<Customer, Long> {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Page<CustomerDto> findPage(String q, Pageable pageable) {
        Page<Customer> customers = (q == null || q.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.search(q.trim(), pageable);
        return customers.map(this::toDto);
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public CustomerDto findDtoById(Long id) {
        return toDto(findById(id));
    }

    public CustomerDto create(CustomerUpsertRequest request) {
        Customer customer = new Customer();
        apply(customer, request);
        customer.setId(null);
        return toDto(customerRepository.save(customer));
    }

    public CustomerDto update(Long id, CustomerUpsertRequest request) {
        Customer existing = findById(id);
        apply(existing, request);
        return toDto(customerRepository.save(existing));
    }

    @Override
    public Customer create(Customer entity) {
        entity.setId(null);
        return customerRepository.save(entity);
    }

    @Override
    public Customer update(Long id, Customer entity) {
        Customer existing = findById(id);
        existing.setName(entity.getName());
        existing.setEmail(entity.getEmail());
        existing.setPhone(entity.getPhone());
        existing.setAddress(entity.getAddress());
        existing.setCity(entity.getCity());
        existing.setState(entity.getState());
        existing.setPostalCode(entity.getPostalCode());
        existing.setActive(entity.getActive());
        return customerRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Customer existing = findById(id);
        customerRepository.delete(existing);
    }

    private CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPostalCode(),
                customer.getActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private void apply(Customer customer, CustomerUpsertRequest request) {
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setState(request.state());
        customer.setPostalCode(request.postalCode());
        customer.setActive(request.active() == null ? Boolean.TRUE : request.active());
    }
}
