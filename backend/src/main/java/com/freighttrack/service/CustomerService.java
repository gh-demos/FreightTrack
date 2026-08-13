package com.freighttrack.service;

import com.freighttrack.exception.ResourceNotFoundException;
import com.freighttrack.model.entity.Customer;
import com.freighttrack.repository.CustomerRepository;
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

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
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
}
