package firm.brokerage.service;

import firm.brokerage.entity.OrderEntity;
import firm.brokerage.exception.InvalidOrderStatusException;
import firm.brokerage.exception.OrderNotFoundException;
import firm.brokerage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchingService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;

    /**
     * Match a pending order (Admin function)
     */
    public OrderEntity matchOrder(String orderId) {
        log.info("Matching order: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.canBeMatched()) {
            throw new InvalidOrderStatusException(orderId, order.getStatus(), "match");
        }

        // Process the asset transfers
        assetService.processMatchedOrder(
                order.getCustomerId(),
                order.getAssetName(),
                order.getOrderSide(),
                order.getSize(),
                order.getPrice()
        );

        // Mark order as matched
        order.match();
        OrderEntity matchedOrder = orderRepository.save(order);

        log.info("Order {} matched successfully", orderId);
        return matchedOrder;
    }
}
