package firm.brokerage.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrderEntityTest {

    @Test
    @DisplayName("Should create order with PENDING status")
    void shouldCreateOrderWithPendingStatus() {
        // Given
        String customerId = "CUST001";
        String assetName = "AAPL";
        OrderSide orderSide = OrderSide.BUY;
        BigDecimal size = new BigDecimal("10.00");
        BigDecimal price = new BigDecimal("150.00");

        // When
        OrderEntity order = new OrderEntity(customerId, assetName, orderSide, size, price);

        // Then
        assertNotNull(order.getOrderId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(assetName, order.getAssetName());
        assertEquals(orderSide, order.getOrderSide());
        assertEquals(size, order.getSize());
        assertEquals(price, order.getPrice());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreateDate());
        assertTrue(order.isBuyOrder());
        assertFalse(order.isSellOrder());
    }

    @Test
    @DisplayName("Should calculate total value correctly")
    void shouldCalculateTotalValueCorrectly() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));

        // When
        BigDecimal totalValue = order.getTotalValue();

        // Then
        assertEquals(0, new BigDecimal("1500.00").compareTo(totalValue));
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void shouldCancelPendingOrderSuccessfully() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));

        // When
        order.cancel();

        // Then
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertTrue(order.isCanceled());
        assertFalse(order.canBeCanceled());
        assertFalse(order.canBeMatched());
    }

    @Test
    @DisplayName("Should throw exception when canceling non-pending order")
    void shouldThrowExceptionWhenCancelingNonPendingOrder() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));
        order.match(); // Change to MATCHED status

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.cancel()
        );

        assertTrue(exception.getMessage().contains("Only PENDING orders can be canceled"));
    }

    @Test
    @DisplayName("Should match pending order successfully")
    void shouldMatchPendingOrderSuccessfully() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));

        // When
        order.match();

        // Then
        assertEquals(OrderStatus.MATCHED, order.getStatus());
        assertTrue(order.isMatched());
        assertFalse(order.canBeCanceled());
        assertFalse(order.canBeMatched());
    }
}