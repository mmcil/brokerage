package firm.brokerage.service;

import firm.brokerage.dto.CreateOrderRequest;
import firm.brokerage.entity.OrderEntity;
import firm.brokerage.entity.OrderStatus;
import firm.brokerage.exception.InvalidOrderStatusException;
import firm.brokerage.exception.OrderNotFoundException;
import firm.brokerage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;

    /**
     * Create a new order
     */
    public OrderEntity createOrder(CreateOrderRequest request) {
        log.info("Creating {} order for customer {} - {} {} at {}",
                request.getOrderSide(), request.getCustomerId(),
                request.getSize(), request.getAssetName(), request.getPrice());

        // Reserve assets before creating order
        assetService.reserveAssetsForOrder(
                request.getCustomerId(),
                request.getAssetName(),
                request.getOrderSide(),
                request.getSize(),
                request.getPrice()
        );

        // Create and save order
        OrderEntity order = new OrderEntity(
                request.getCustomerId(),
                request.getAssetName(),
                request.getOrderSide(),
                request.getSize(),
                request.getPrice()
        );

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderId());
        return savedOrder;
    }

    /**
     * List orders for a customer
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> listOrders(String customerId) {
        log.debug("Listing orders for customer: {}", customerId);
        return orderRepository.findByCustomerIdOrderByCreateDateDesc(customerId);
    }

    /**
     * List orders for a customer within date range
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> listOrders(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Listing orders for customer {} from {} to {}", customerId, startDate, endDate);
        return orderRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    /**
     * Cancel an order (delete pending order)
     */
    public void cancelOrder(String orderId, String customerId) {
        log.info("Canceling order {} for customer {}", orderId, customerId);

        OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId, customerId));

        if (!order.canBeCanceled()) {
            throw new InvalidOrderStatusException(orderId, order.getStatus(), "cancel");
        }

        // Release reserved assets
        assetService.releaseAssetsForOrder(
                order.getCustomerId(),
                order.getAssetName(),
                order.getOrderSide(),
                order.getSize(),
                order.getPrice()
        );

        // Mark order as canceled
        order.cancel();
        orderRepository.save(order);

        log.info("Order {} canceled successfully", orderId);
    }

    /**
     * Get order by ID (for admin or customer access)
     */
    @Transactional(readOnly = true)
    public OrderEntity getOrder(String orderId, String customerId) {
        return orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId, customerId));
    }

    /**
     * Get all pending orders (for admin matching)
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getAllPendingOrders() {
        return orderRepository.findByStatusOrderByCreateDateAsc(OrderStatus.PENDING);
    }
}
