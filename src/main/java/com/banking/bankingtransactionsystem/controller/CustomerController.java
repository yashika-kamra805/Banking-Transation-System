package com.banking.bankingtransactionsystem.controller;

import jakarta.validation.Valid;
import com.banking.bankingtransactionsystem.model.Customer;
import com.banking.bankingtransactionsystem.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<String> registerCustomer(
            @Valid @RequestBody Customer customer) {

        customerService.registerCustomer(customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Customer registered successfully");
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(
                customerService.getAllCustomers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                customerService.getCustomerById(id)
        );
    }
}
