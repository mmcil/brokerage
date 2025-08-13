package firm.brokerage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import firm.brokerage.dto.CreateOrderRequest;
import firm.brokerage.entity.AssetEntity;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.repository.AssetRepository;
import firm.brokerage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
class BrokerageSystemIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method for BigDecimal assertions
    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected: " + expected + " but was: " + actual);
    }

    @BeforeEach
    void setUp() {
        // Setup MockMvc with security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean database
        orderRepository.deleteAll();
        assetRepository.deleteAll();

        // Set up test data
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        AssetEntity stockAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));
        assetRepository.save(tryAsset);
        assetRepository.save(stockAsset);
    }

    @Test
    @DisplayName("Should complete full order lifecycle successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldCompleteFullOrderLifecycleSuccessfully() throws Exception {
        // 1. Create BUY order
        CreateOrderRequest buyRequest = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("10.00"), new BigDecimal("150.00")
        );

        String createResponse = mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract order ID from response
        var orderResponse = objectMapper.readTree(createResponse);
        String orderId = orderResponse.get("orderId").asText();

        // 2. Verify TRY asset was reserved
        AssetEntity tryAsset = assetRepository.findByCustomerIdAndAssetName("CUST001", "TRY").orElseThrow();
        assertBigDecimalEquals(new BigDecimal("8500.00"), tryAsset.getUsableSize()); // 10000 - (10 * 150)

        // 3. List orders
        mockMvc.perform(get("/api/orders")
                        .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value(orderId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // 4. Cancel order
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNoContent());

        // 5. Verify TRY asset was released
        AssetEntity tryAssetAfterCancel = assetRepository.findByCustomerIdAndAssetName("CUST001", "TRY").orElseThrow();
        assertBigDecimalEquals(new BigDecimal("10000.00"), tryAssetAfterCancel.getUsableSize()); // Back to original

        // 6. Verify order status
        var canceledOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals("CANCELED", canceledOrder.getStatus().name());
    }

    @Test
    @DisplayName("Should handle insufficient funds correctly")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInsufficientFundsCorrectly() throws Exception {
        // Try to create order that requires more TRY than available
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.BUY,
                new BigDecimal("100.00"), new BigDecimal("200.00") // Requires 20,000 TRY but only have 10,000
        );

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    @DisplayName("Should handle SELL order correctly")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleSellOrderCorrectly() throws Exception {
        // Create SELL order
        CreateOrderRequest sellRequest = new CreateOrderRequest(
                "CUST001", "AAPL", OrderSide.SELL,
                new BigDecimal("20.00"), new BigDecimal("155.00")
        );

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellRequest)))
                .andExpect(status().isCreated());

        // Verify AAPL asset was reserved
        AssetEntity stockAsset = assetRepository.findByCustomerIdAndAssetName("CUST001", "AAPL").orElseThrow();
        assertBigDecimalEquals(new BigDecimal("30.00"), stockAsset.getUsableSize()); // 50 - 20
    }

    @Test
    @DisplayName("Should list customer assets correctly")
    @WithMockUser(roles = "ADMIN")
    void shouldListCustomerAssetsCorrectly() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.assetName == 'TRY')].size").value(10000.00))
                .andExpect(jsonPath("$[?(@.assetName == 'AAPL')].size").value(50.00));
    }
}