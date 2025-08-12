package firm.brokerage.exception;

import firm.brokerage.entity.OrderStatus;

/**
 * Thrown when trying to perform an operation on an order with invalid status
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String orderId, OrderStatus currentStatus,
                                       String operation) {
        super(String.format("Cannot %s order %s with status %s",
                operation, orderId, currentStatus));
    }
}