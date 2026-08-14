package com.freighttrack.repository;

import com.freighttrack.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCase(String email);

    @Query("""
            select c from Customer c
            where lower(c.name) like lower(concat('%', :q, '%'))
               or lower(c.email) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.city, '')) like lower(concat('%', :q, '%'))
            """)
    Page<Customer> search(@Param("q") String q, Pageable pageable);
}
