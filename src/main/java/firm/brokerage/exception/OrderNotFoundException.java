package firm.brokerage.exception;

/**
 * Thrown when an order cannot be found
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }

    public OrderNotFoundException(String orderId, String customerId) {
        super(String.format("Order %s not found for customer %s", orderId, customerId));
    }
}