package firm.brokerage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import firm.brokerage.config.SecurityConfig;
import firm.brokerage.dto.CreateOrderRequest;
import firm.brokerage.entity.OrderEntity;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.entity.OrderStatus;
import firm.brokerage.exception.InsufficientFundsException;
import firm.brokerage.exception.InvalidOrderStatusException;
import firm.brokerage.exception.OrderNotFoundException;
import firm.brokerage.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create order successfully with admin authentication")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateOrderSuccessfullyWithAdminAuthentication() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        OrderEntity orderEntity = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
        orderEntity.setOrderId("ORDER123");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderEntity);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORDER123"))
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("BUY"))
                .andExpect(jsonPath("$.size").value(10.00))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalValue").value(1500.00));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should list orders successfully for customer")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldListOrdersSuccessfullyForCustomer() throws Exception {
        // Given
        String customerId = "CUST001";
        OrderEntity order1 = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
        order1.setOrderId("ORDER123");

        OrderEntity order2 = new OrderEntity(
                "CUST001", "GOOGL", OrderSide.SELL,
                new BigDecimal("5.00"), new BigDecimal("2800.00")
        );
        order2.setOrderId("ORDER124");

        List<OrderEntity> orders = Arrays.asList(order1, order2);

        when(orderService.listOrders(customerId)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("CUST001"))
                .andExpect(jsonPath("$[0].assetName").value("AAPL"))
                .andExpect(jsonPath("$[0].orderSide").value("BUY"))
                .andExpect(jsonPath("$[1].customerId").value("CUST001"))
                .andExpect(jsonPath("$[1].assetName").value("GOOGL"))
                .andExpect(jsonPath("$[1].orderSide").value("SELL"));

        verify(orderService, times(1)).listOrders(customerId);
    }

    @Test
    @DisplayName("Should list orders with date range filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldListOrdersWithDateRangeFilter() throws Exception {
        // Given
        String customerId = "CUST001";
        String startDate = "2025-01-01T00:00:00";
        String endDate = "2025-01-31T23:59:59";

        OrderEntity order = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
        order.setOrderId("ORDER123");

        List<OrderEntity> orders = Arrays.asList(order);

        when(orderService.listOrders(eq(customerId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .param("customerId", customerId)
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value("CUST001"));

        verify(orderService, times(1)).listOrders(eq(customerId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        String orderId = "ORDER123";
        String customerId = "CUST001";

        doNothing().when(orderService).cancelOrder(orderId, customerId);

        // When & Then
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .param("customerId", customerId))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).cancelOrder(orderId, customerId);
    }

    @Test
    @DisplayName("Should get specific order successfully")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetSpecificOrderSuccessfully() throws Exception {
        // Given
        String orderId = "ORDER123";
        String customerId = "CUST001";

        OrderEntity order = new OrderEntity(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );
        order.setOrderId(orderId);

        when(orderService.getOrder(orderId, customerId)).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.assetName").value("AAPL"));

        verify(orderService, times(1)).getOrder(orderId, customerId);
    }

    @Test
    @DisplayName("Should require authentication for order creation")
    void shouldRequireAuthenticationForOrderCreation() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should require authentication for listing orders")
    void shouldRequireAuthenticationForListingOrders() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders")
                        .param("customerId", "CUST001"))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).listOrders(any(String.class));
    }

    @Test
    @DisplayName("Should validate order request with empty customer ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldValidateOrderRequestWithEmptyCustomerId() throws Exception {
        // Given - Invalid request with empty customer ID
        CreateOrderRequest request = new CreateOrderRequest(
                "", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should validate order request with negative size")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldValidateOrderRequestWithNegativeSize() throws Exception {
        // Given - Invalid request with negative size
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("-10.00"), new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should validate order request with negative price")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldValidateOrderRequestWithNegativePrice() throws Exception {
        // Given - Invalid request with negative price
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("-150.00")
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle insufficient funds exception")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleInsufficientFundsException() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new InsufficientFundsException("Customer CUST001 has insufficient TRY. Required: 1500.00, Available: 500.00"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.message").value("Customer CUST001 has insufficient TRY. Required: 1500.00, Available: 500.00"));
    }

    @Test
    @DisplayName("Should handle order not found exception when canceling")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleOrderNotFoundExceptionWhenCanceling() throws Exception {
        // Given
        String orderId = "NONEXISTENT";
        String customerId = "CUST001";

        doThrow(new OrderNotFoundException(orderId, customerId))
                .when(orderService).cancelOrder(orderId, customerId);

        // When & Then
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .param("customerId", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order NONEXISTENT not found for customer CUST001"));
    }

    @Test
    @DisplayName("Should handle invalid order status exception when canceling")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleInvalidOrderStatusExceptionWhenCanceling() throws Exception {
        // Given
        String orderId = "ORDER123";
        String customerId = "CUST001";

        doThrow(new InvalidOrderStatusException(orderId, OrderStatus.MATCHED, "cancel"))
                .when(orderService).cancelOrder(orderId, customerId);

        // When & Then
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .param("customerId", customerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_STATUS"))
                .andExpect(jsonPath("$.message").value("Cannot cancel order ORDER123 with status MATCHED"));
    }

    @Test
    @DisplayName("Should handle missing customer ID parameter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleMissingCustomerIdParameter() throws Exception {
        // When & Then - Missing customerId parameter
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).listOrders(any(String.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleMalformedJsonRequest() throws Exception {
        // Given - Malformed JSON
        String malformedJson = "{ \"customerId\": \"CUST001\", \"assetName\": }";

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message").value("Invalid JSON format in request body"));

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }
}