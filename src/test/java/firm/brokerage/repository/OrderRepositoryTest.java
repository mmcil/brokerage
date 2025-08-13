package firm.brokerage.repository;

import firm.brokerage.entity.OrderEntity;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Should save and find order by customer ID")
    void shouldSaveAndFindOrderByCustomerId() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));
        orderRepository.save(order);

        // When
        List<OrderEntity> result = orderRepository.findByCustomerIdOrderByCreateDateDesc("CUST001");

        // Then
        assertEquals(1, result.size());
        assertEquals("CUST001", result.get(0).getCustomerId());
        assertEquals("AAPL", result.get(0).getAssetName());
        assertEquals(OrderSide.BUY, result.get(0).getOrderSide());
    }

    @Test
    @DisplayName("Should find order by order ID and customer ID")
    void shouldFindOrderByOrderIdAndCustomerId() {
        // Given
        OrderEntity order = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));
        OrderEntity savedOrder = orderRepository.save(order);

        // When
        Optional<OrderEntity> result = orderRepository.findByOrderIdAndCustomerId(
                savedOrder.getOrderId(), "CUST001");

        // Then
        assertTrue(result.isPresent());
        assertEquals(savedOrder.getOrderId(), result.get().getOrderId());
        assertEquals("CUST001", result.get().getCustomerId());
    }

    @Test
    @DisplayName("Should find orders by date range")
    void shouldFindOrdersByDateRange() {
        // Given
        OrderEntity order1 = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));
        OrderEntity order2 = new OrderEntity("CUST001", "GOOGL", OrderSide.SELL,
                new BigDecimal("5.00"), new BigDecimal("2800.00"));
        orderRepository.save(order1);
        orderRepository.save(order2);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<OrderEntity> result = orderRepository.findByCustomerIdAndDateRange(
                "CUST001", startDate, endDate);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should find pending orders by status")
    void shouldFindPendingOrdersByStatus() {
        // Given
        OrderEntity pendingOrder = new OrderEntity("CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00"));
        OrderEntity matchedOrder = new OrderEntity("CUST002", "GOOGL", OrderSide.SELL,
                new BigDecimal("5.00"), new BigDecimal("2800.00"));
        matchedOrder.match();

        orderRepository.save(pendingOrder);
        orderRepository.save(matchedOrder);

        // When
        List<OrderEntity> result = orderRepository.findByStatusOrderByCreateDateAsc(OrderStatus.PENDING);

        // Then
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        assertEquals("CUST001", result.get(0).getCustomerId());
    }
}