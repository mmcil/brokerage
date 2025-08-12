package firm.brokerage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer entity for user authentication and profile management
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@Slf4j
public class CustomerEntity {

    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "username", unique = true, nullable = false)
    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(name = "password", nullable = false)
    @NotNull(message = "Password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Will be encoded

    @Column(name = "email", nullable = false)
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be valid")
    private String email;

    @Column(name = "first_name", nullable = false)
    @NotNull(message = "First name cannot be null")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    /**
     * Constructor for creating new customer
     */
    public CustomerEntity(String username,
                          String password,
                          String email,
                          String firstName,
                          String lastName) {
        this.customerId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = true;
        this.createdDate = LocalDateTime.now();

        validateCustomer();

        log.debug("Created new customer {} with username {}", customerId, username);
    }

    // Business Methods

    /**
     * Activate customer account
     */
    public void activate() {
        this.active = true;
        log.info("Customer {} activated", customerId);
    }

    /**
     * Deactivate customer account
     */
    public void deactivate() {
        this.active = false;
        log.info("Customer {} deactivated", customerId);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
        log.debug("Updated last login for customer {}", customerId);
    }

    /**
     * Get customer's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if customer account is active
     */
    public boolean isActive() {
        return active;
    }

    // Validation
    private void validateCustomer() {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }
}
