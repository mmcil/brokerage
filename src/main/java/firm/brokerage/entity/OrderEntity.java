package firm.brokerage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order entity representing trading orders
 * Contains business logic for order state transitions
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@Slf4j
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "Customer ID cannot be null")
    private String customerId;

    @Column(name = "asset_name", nullable = false)
    @NotNull(message = "Asset name cannot be null")
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false)
    @NotNull(message = "Order side cannot be null")
    private OrderSide orderSide;

    @Column(name = "size", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Size cannot be null")
    @Positive(message = "Size must be positive")
    private BigDecimal size;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status cannot be null")
    private OrderStatus status;

    @Column(name = "create_date", nullable = false)
    @NotNull(message = "Create date cannot be null")
    private LocalDateTime createDate;

    /**
     * Constructor for creating new order
     */
    public OrderEntity(String customerId,
                       String assetName,
                       OrderSide orderSide,
                       BigDecimal size,
                       BigDecimal price) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.size = size;
        this.price = price;
        this.status = OrderStatus.PENDING;
        this.createDate = LocalDateTime.now();

        validateOrder();

        log.debug("Created new {} order {} for customer {} - {} {} at {}",
                orderSide, orderId, customerId, size, assetName, price);
    }

    // Business Methods

    /**
     * Cancel the order if it's in PENDING status
     */
    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    String.format("Cannot cancel order %s with status %s. Only PENDING orders can be canceled.",
                            orderId, status)
            );
        }
        this.status = OrderStatus.CANCELED;
        log.info("Order {} canceled for customer {}", orderId, customerId);
    }

    /**
     * Match the order (change status to MATCHED)
     */
    public void match() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    String.format("Cannot match order %s with status %s. Only PENDING orders can be matched.",
                            orderId, status)
            );
        }
        this.status = OrderStatus.MATCHED;
        log.info("Order {} matched for customer {} - {} {} at {}",
                orderId, customerId, size, assetName, price);
    }

    /**
     * Check if order can be canceled
     */
    public boolean canBeCanceled() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Check if order can be matched
     */
    public boolean canBeMatched() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Calculate total order value (size * price)
     */
    public BigDecimal getTotalValue() {
        return size.multiply(price);
    }

    /**
     * Check if this is a buy order
     */
    public boolean isBuyOrder() {
        return orderSide == OrderSide.BUY;
    }

    /**
     * Check if this is a sell order
     */
    public boolean isSellOrder() {
        return orderSide == OrderSide.SELL;
    }

    /**
     * Check if this order is for TRY asset
     */
    public boolean isTryOrder() {
        return "TRY".equals(assetName);
    }

    /**
     * Check if this order is pending
     */
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Check if this order is matched
     */
    public boolean isMatched() {
        return status == OrderStatus.MATCHED;
    }

    /**
     * Check if this order is canceled
     */
    public boolean isCanceled() {
        return status == OrderStatus.CANCELED;
    }

    // Validation
    private void validateOrder() {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        if (assetName == null || assetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be null or empty");
        }
        if (orderSide == null) {
            throw new IllegalArgumentException("Order side cannot be null");
        }
        if (size == null || size.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
