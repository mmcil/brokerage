package firm.brokerage.controller;

import firm.brokerage.dto.MatchOrderRequest;
import firm.brokerage.dto.OrderResponse;
import firm.brokerage.entity.OrderEntity;
import firm.brokerage.service.MatchingService;
import firm.brokerage.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin operations
 * Handles order matching and administrative functions
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MatchingService matchingService;
    private final OrderService orderService;

    /**
     * Match a pending order
     * POST /api/admin/match-order
     */
    @PostMapping("/match-order")
    public ResponseEntity<OrderResponse> matchOrder(@Valid @RequestBody MatchOrderRequest request) {
        log.info("Admin matching order: {}", request.getOrderId());

        OrderEntity matchedOrder = matchingService.matchOrder(request.getOrderId());
        OrderResponse response = OrderResponse.fromEntity(matchedOrder);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all pending orders (for admin review)
     * GET /api/admin/pending-orders
     */
    @GetMapping("/pending-orders")
    public ResponseEntity<List<OrderResponse>> getAllPendingOrders() {
        log.info("Admin requesting all pending orders");

        List<OrderEntity> pendingOrders = orderService.getAllPendingOrders();
        List<OrderResponse> responses = pendingOrders.stream()
                .map(OrderResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Alternative endpoint to match order by path parameter
     * POST /api/admin/orders/{orderId}/match
     */
    @PostMapping("/orders/{orderId}/match")
    public ResponseEntity<OrderResponse> matchOrderById(@PathVariable String orderId) {
        log.info("Admin matching order by ID: {}", orderId);

        OrderEntity matchedOrder = matchingService.matchOrder(orderId);
        OrderResponse response = OrderResponse.fromEntity(matchedOrder);

        return ResponseEntity.ok(response);
    }
}
