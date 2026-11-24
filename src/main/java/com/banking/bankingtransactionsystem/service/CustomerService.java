package com.banking.bankingtransactionsystem.service;

import com.banking.bankingtransactionsystem.model.Customer;
import com.banking.bankingtransactionsystem.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void registerCustomer(Customer customer) {

        if (customer.getFullName() == null ||
                customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        if (customer.getEmail() == null ||
                customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!customer.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException(
                    "Customer email already exists"
            );
        }

        customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with ID: " + customerId
                        )
                );
    }
}
