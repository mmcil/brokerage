package firm.brokerage.service;

import firm.brokerage.dto.CreateOrderRequest;
import firm.brokerage.entity.OrderEntity;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.entity.OrderStatus;
import firm.brokerage.exception.InvalidOrderStatusException;
import firm.brokerage.exception.OrderNotFoundException;
import firm.brokerage.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;
    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        createOrderRequest = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        orderEntity = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Given
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        doNothing().when(assetService).reserveAssetsForOrder(
                any(String.class), any(String.class), any(OrderSide.class),
                any(BigDecimal.class), any(BigDecimal.class)
        );

        // When
        OrderEntity result = orderService.createOrder(createOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetService).reserveAssetsForOrder(
                createOrderRequest.getCustomerId(),
                createOrderRequest.getAssetName(),
                createOrderRequest.getOrderSide(),
                createOrderRequest.getSize(),
                createOrderRequest.getPrice()
        );
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should list orders for customer")
    void shouldListOrdersForCustomer() {
        // Given
        String customerId = "CUST001";
        List<OrderEntity> expectedOrders = Arrays.asList(orderEntity);
        when(orderRepository.findByCustomerIdOrderByCreateDateDesc(customerId))
                .thenReturn(expectedOrders);

        // When
        List<OrderEntity> result = orderService.listOrders(customerId);

        // Then
        assertEquals(expectedOrders, result);
        verify(orderRepository).findByCustomerIdOrderByCreateDateDesc(customerId);
    }

    @Test
    @DisplayName("Should list orders for customer with date range")
    void shouldListOrdersForCustomerWithDateRange() {
        // Given
        String customerId = "CUST001";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<OrderEntity> expectedOrders = Arrays.asList(orderEntity);

        when(orderRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate))
                .thenReturn(expectedOrders);

        // When
        List<OrderEntity> result = orderService.listOrders(customerId, startDate, endDate);

        // Then
        assertEquals(expectedOrders, result);
        verify(orderRepository).findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Given
        String orderId = "ORDER001";
        String customerId = "CUST001";

        when(orderRepository.findByOrderIdAndCustomerId(orderId, customerId))
                .thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        doNothing().when(assetService).releaseAssetsForOrder(
                any(String.class), any(String.class), any(OrderSide.class),
                any(BigDecimal.class), any(BigDecimal.class)
        );

        // When
        orderService.cancelOrder(orderId, customerId);

        // Then
        assertEquals(OrderStatus.CANCELED, orderEntity.getStatus());
        verify(assetService).releaseAssetsForOrder(
                orderEntity.getCustomerId(),
                orderEntity.getAssetName(),
                orderEntity.getOrderSide(),
                orderEntity.getSize(),
                orderEntity.getPrice()
        );
        verify(orderRepository).save(orderEntity);
    }

    @Test
    @DisplayName("Should throw exception when canceling non-existent order")
    void shouldThrowExceptionWhenCancelingNonExistentOrder() {
        // Given
        String orderId = "NONEXISTENT";
        String customerId = "CUST001";

        when(orderRepository.findByOrderIdAndCustomerId(orderId, customerId))
                .thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(orderId, customerId)
        );

        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains(customerId));
    }

    @Test
    @DisplayName("Should throw exception when canceling matched order")
    void shouldThrowExceptionWhenCancelingMatchedOrder() {
        // Given
        String orderId = "ORDER001";
        String customerId = "CUST001";
        orderEntity.match(); // Set status to MATCHED

        when(orderRepository.findByOrderIdAndCustomerId(orderId, customerId))
                .thenReturn(Optional.of(orderEntity));

        // When & Then
        InvalidOrderStatusException exception = assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.cancelOrder(orderId, customerId)
        );

        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains("cancel"));
    }
}