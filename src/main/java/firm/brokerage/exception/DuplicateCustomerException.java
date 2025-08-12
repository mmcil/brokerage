package firm.brokerage.exception;

/**
 * Thrown when trying to create a customer that already exists
 */
public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String username) {
        super("Customer already exists with username: " + username);
    }

    public DuplicateCustomerException(String field, String value) {
        super(String.format("Customer already exists with %s: %s", field, value));
    }
}
