package firm.brokerage.exception;

/**
 * Thrown when trying to access a deactivated customer account
 */
public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException(String customerId) {
        super("Customer account is deactivated: " + customerId);
    }
}