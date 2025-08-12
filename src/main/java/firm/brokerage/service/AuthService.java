package firm.brokerage.service;

import firm.brokerage.entity.CustomerEntity;
import firm.brokerage.exception.AccountDeactivatedException;
import firm.brokerage.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

/**
 * Service for customer authentication
 * Simple JWT-like token generation for demonstration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate customer with username and password
     */
    public CustomerEntity authenticate(String username, String password) {
        CustomerEntity customer = customerService.findByUsername(username);

        if (!customer.isActive()) {
            throw new AccountDeactivatedException(customer.getCustomerId());
        }

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new InvalidCredentialsException();
        }

        log.info("Customer {} authenticated successfully", customer.getCustomerId());
        return customer;
    }

    /**
     * Generate a simple token for the customer
     * In production, use proper JWT with expiration
     */
    public String generateToken(CustomerEntity customer) {
        String tokenData = customer.getCustomerId() + ":" + customer.getUsername() + ":" + UUID.randomUUID();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    /**
     * Validate and decode token (simplified implementation)
     * In production, use proper JWT validation
     */
    public String validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            return decoded.split(":")[0]; // Return customer ID
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }
}
