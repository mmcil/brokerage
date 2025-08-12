package firm.brokerage.dto;

import firm.brokerage.entity.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Asset name is required")
    private String assetName;

    @NotNull(message = "Order side is required")
    private OrderSide orderSide;

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be positive")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}
