package firm.brokerage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Asset entity representing customer's holdings
 * Contains both total size and usable size for trading
 */
@Entity
@Table(name = "assets")
@IdClass(AssetId.class)
@Data
@NoArgsConstructor
@Slf4j
public class AssetEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "Customer ID cannot be null")
    private String customerId;

    @Id
    @Column(name = "asset_name", nullable = false)
    @NotNull(message = "Asset name cannot be null")
    private String assetName;

    @Column(name = "size", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Size cannot be null")
    @PositiveOrZero(message = "Size must be positive or zero")
    private BigDecimal size;

    @Column(name = "usable_size", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Usable size cannot be null")
    @PositiveOrZero(message = "Usable size must be positive or zero")
    private BigDecimal usableSize;

    /**
     * Constructor for creating new asset
     */
    public AssetEntity(String customerId,
                       String assetName,
                       BigDecimal size) {
        this.customerId = customerId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = size; // Initially all is usable
        validateAsset();
    }

    /**
     * Full constructor
     */
    public AssetEntity(String customerId,
                       String assetName,
                       BigDecimal size,
                       BigDecimal usableSize) {
        this.customerId = customerId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
        validateAsset();
    }

    // Business Methods

    /**
     * Reserve amount for trading (reduces usable size)
     */
    public void reserve(BigDecimal amount) {
        validatePositiveAmount(amount, "Reserve amount must be positive");

        if (usableSize.compareTo(amount) < 0) {
            log.warn("Insufficient usable amount for customer: {} asset: {} required: {} available: {}",
                    customerId, assetName, amount, usableSize);
            throw new IllegalArgumentException(
                    String.format("Insufficient usable amount. Required: %s, Available: %s", amount, usableSize)
            );
        }

        this.usableSize = usableSize.subtract(amount);
        log.debug("Reserved {} {} for customer {}, usable size now: {}",
                amount, assetName, customerId, usableSize);
    }

    /**
     * Release reserved amount back to usable (increases usable size)
     */
    public void release(BigDecimal amount) {
        validatePositiveAmount(amount, "Release amount must be positive");

        BigDecimal newUsableSize = usableSize.add(amount);
        if (newUsableSize.compareTo(size) > 0) {
            log.warn("Attempting to release more than total size for customer: {} asset: {}",
                    customerId, assetName);
            this.usableSize = size; // Set to maximum possible
        } else {
            this.usableSize = newUsableSize;
        }

        log.debug("Released {} {} for customer {}, usable size now: {}",
                amount, assetName, customerId, usableSize);
    }

    /**
     * Add to total asset size and usable size
     */
    public void increase(BigDecimal amount) {
        validatePositiveAmount(amount, "Increase amount must be positive");

        this.size = size.add(amount);
        this.usableSize = usableSize.add(amount);

        log.debug("Increased {} {} for customer {}, total size now: {}",
                amount, assetName, customerId, size);
    }

    /**
     * Reduce total asset size and usable size
     */
    public void decrease(BigDecimal amount) {
        validatePositiveAmount(amount, "Decrease amount must be positive");

        if (size.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Cannot decrease more than current size");
        }

        this.size = size.subtract(amount);
        this.usableSize = usableSize.subtract(amount);

        // Ensure usable size doesn't go negative
        if (usableSize.compareTo(BigDecimal.ZERO) < 0) {
            this.usableSize = BigDecimal.ZERO;
        }

        log.debug("Decreased {} {} for customer {}, total size now: {}",
                amount, assetName, customerId, size);
    }

    /**
     * Check if sufficient usable amount exists
     */
    public boolean hasSufficientUsableAmount(BigDecimal requiredAmount) {
        return usableSize.compareTo(requiredAmount) >= 0;
    }

    /**
     * Get reserved amount (total - usable)
     */
    public BigDecimal getReservedAmount() {
        return size.subtract(usableSize);
    }

    /**
     * Check if this is TRY asset
     */
    public boolean isTryAsset() {
        return "TRY".equals(assetName);
    }

    // Validation methods
    private void validateAsset() {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        if (assetName == null || assetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be null or empty");
        }
        if (size == null || size.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Size must be positive or zero");
        }
        if (usableSize == null || usableSize.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Usable size must be positive or zero");
        }
        if (usableSize.compareTo(size) > 0) {
            throw new IllegalArgumentException("Usable size cannot exceed total size");
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
