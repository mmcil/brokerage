package firm.brokerage.controller;

import firm.brokerage.dto.CreateOrderRequest;
import firm.brokerage.dto.OrderResponse;
import firm.brokerage.entity.OrderEntity;
import firm.brokerage.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for order management operations
 * Handles order creation, listing, and cancellation
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        OrderEntity order = orderService.createOrder(request);
        OrderResponse response = OrderResponse.fromEntity(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List orders for a customer
     * GET /api/orders?customerId=CUST001
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Listing orders for customer: {}", customerId);

        List<OrderEntity> orders;
        if (startDate != null && endDate != null) {
            orders = orderService.listOrders(customerId, startDate, endDate);
        } else {
            orders = orderService.listOrders(customerId);
        }

        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Cancel (delete) a pending order
     * DELETE /api/orders/{orderId}?customerId=CUST001
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String customerId) {

        log.info("Canceling order {} for customer {}", orderId, customerId);

        orderService.cancelOrder(orderId, customerId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get specific order details
     * GET /api/orders/{orderId}?customerId=CUST001
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderId,
            @RequestParam String customerId) {

        log.debug("Getting order {} for customer {}", orderId, customerId);

        OrderEntity order = orderService.getOrder(orderId, customerId);
        OrderResponse response = OrderResponse.fromEntity(order);

        return ResponseEntity.ok(response);
    }
}