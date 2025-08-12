package firm.brokerage.service;

import firm.brokerage.entity.CustomerEntity;
import firm.brokerage.exception.CustomerNotFoundException;
import firm.brokerage.exception.DuplicateCustomerException;
import firm.brokerage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new customer
     */
    public CustomerEntity registerCustomer(String username,
                                           String password,
                                           String email,
                                           String firstName,
                                           String lastName) {
        log.info("Registering new customer with username: {}", username);

        // Check if username already exists
        if (customerRepository.existsByUsername(username)) {
            throw new DuplicateCustomerException(username);
        }

        // Check if email already exists
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateCustomerException("email", email);
        }

        // Create new customer with encoded password
        CustomerEntity customer = new CustomerEntity(username, passwordEncoder.encode(password),
                email, firstName, lastName);

        CustomerEntity savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully: {}", savedCustomer.getCustomerId());
        return savedCustomer;
    }

    /**
     * Find customer by username
     */
    @Transactional(readOnly = true)
    public CustomerEntity findByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException(username, true));
    }

    /**
     * Find active customer by username
     */
    @Transactional(readOnly = true)
    public CustomerEntity findActiveByUsername(String username) {
        return customerRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new CustomerNotFoundException(username, true));
    }

    /**
     * Update last login
     */
    public void updateLastLogin(String customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.updateLastLogin();
        customerRepository.save(customer);
    }
}
