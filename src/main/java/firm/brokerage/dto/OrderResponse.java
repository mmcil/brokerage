package firm.brokerage.dto;

import firm.brokerage.entity.OrderSide;
import firm.brokerage.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal totalValue;
    private OrderStatus status;
    private LocalDateTime createDate;

    // Static factory method for easy conversion
    public static OrderResponse fromEntity(firm.brokerage.entity.OrderEntity order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getAssetName(),
                order.getOrderSide(),
                order.getSize(),
                order.getPrice(),
                order.getTotalValue(),
                order.getStatus(),
                order.getCreateDate()
        );
    }
}
