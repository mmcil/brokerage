package firm.brokerage.exception;

/**
 * Thrown when a customer cannot be found
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }

    public CustomerNotFoundException(String identifier, boolean isUsername) {
        super(isUsername ? "Customer not found with username: " + identifier
                : "Customer not found: " + identifier);
    }
}
