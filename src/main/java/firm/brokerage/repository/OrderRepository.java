package firm.brokerage.repository;

import firm.brokerage.entity.OrderEntity;
import firm.brokerage.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    // Find orders by customer ID
    List<OrderEntity> findByCustomerIdOrderByCreateDateDesc(String customerId);

    // Find orders by customer ID and date range
    @Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId " +
            "AND o.createDate >= :startDate AND o.createDate <= :endDate " +
            "ORDER BY o.createDate DESC")
    List<OrderEntity> findByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Find order by ID and customer ID (for security)
    Optional<OrderEntity> findByOrderIdAndCustomerId(String orderId, String customerId);

    // Find pending orders for a customer
    List<OrderEntity> findByCustomerIdAndStatusOrderByCreateDateDesc(String customerId, OrderStatus status);

    // Find all pending orders (for admin matching)
    List<OrderEntity> findByStatusOrderByCreateDateAsc(OrderStatus status);
}
