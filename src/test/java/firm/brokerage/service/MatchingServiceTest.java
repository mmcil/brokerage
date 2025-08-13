package firm.brokerage.service;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private MatchingService matchingService;

    private OrderEntity pendingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
    }

    @Test
    @DisplayName("Should match pending order successfully")
    void shouldMatchPendingOrderSuccessfully() {
        // Given
        String orderId = "ORDER001";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(pendingOrder);
        doNothing().when(assetService).processMatchedOrder(
                any(String.class), any(String.class), any(OrderSide.class),
                any(BigDecimal.class), any(BigDecimal.class)
        );

        // When
        OrderEntity result = matchingService.matchOrder(orderId);

        // Then
        assertEquals(OrderStatus.MATCHED, result.getStatus());
        verify(assetService).processMatchedOrder(
                pendingOrder.getCustomerId(),
                pendingOrder.getAssetName(),
                pendingOrder.getOrderSide(),
                pendingOrder.getSize(),
                pendingOrder.getPrice()
        );
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    @DisplayName("Should throw exception when matching non-existent order")
    void shouldThrowExceptionWhenMatchingNonExistentOrder() {
        // Given
        String orderId = "NONEXISTENT";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> matchingService.matchOrder(orderId)
        );

        assertTrue(exception.getMessage().contains(orderId));
    }

    @Test
    @DisplayName("Should throw exception when matching canceled order")
    void shouldThrowExceptionWhenMatchingCanceledOrder() {
        // Given
        String orderId = "ORDER001";
        pendingOrder.cancel(); // Set status to CANCELED

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));

        // When & Then
        InvalidOrderStatusException exception = assertThrows(
                InvalidOrderStatusException.class,
                () -> matchingService.matchOrder(orderId)
        );

        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains("match"));
    }
}