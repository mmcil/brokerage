package firm.brokerage.controller;

import firm.brokerage.dto.AuthResponse;
import firm.brokerage.dto.LoginRequest;
import firm.brokerage.dto.RegisterRequest;
import firm.brokerage.entity.CustomerEntity;
import firm.brokerage.service.AuthService;
import firm.brokerage.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for customer authentication operations
 * Handles customer registration and login
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final CustomerService customerService;
    private final AuthService authService;

    /**
     * Register a new customer
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new customer with username: {}", request.getUsername());

        CustomerEntity customer = customerService.registerCustomer(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName()
        );

        // Generate token for the new customer
        String token = authService.generateToken(customer);

        AuthResponse response = new AuthResponse(
                token,
                customer.getCustomerId(),
                customer.getUsername(),
                "Customer registered successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Customer login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        CustomerEntity customer = authService.authenticate(request.getUsername(), request.getPassword());
        String token = authService.generateToken(customer);

        // Update last login
        customerService.updateLastLogin(customer.getCustomerId());

        AuthResponse response = new AuthResponse(
                token,
                customer.getCustomerId(),
                customer.getUsername(),
                "Login successful"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Logout (mainly for client-side token cleanup)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        log.info("Customer logout");

        AuthResponse response = new AuthResponse(
                null,
                null,
                null,
                "Logout successful"
        );

        return ResponseEntity.ok(response);
    }
}