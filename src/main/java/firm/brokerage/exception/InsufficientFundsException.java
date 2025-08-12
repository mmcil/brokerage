package firm.brokerage.exception;

/**
 * Thrown when customer doesn't have sufficient funds or assets for an operation
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String customerId, String assetName,
                                      String required, String available) {
        super(String.format("Customer %s has insufficient %s. Required: %s, Available: %s",
                customerId, assetName, required, available));
    }
}
