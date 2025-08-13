package firm.brokerage.controller;

import firm.brokerage.config.SecurityConfig;
import firm.brokerage.entity.AssetEntity;
import firm.brokerage.exception.AssetNotFoundException;
import firm.brokerage.service.AssetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@Import(SecurityConfig.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetService assetService;

    @Test
    @DisplayName("Should list assets successfully with admin authentication")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldListAssetsSuccessfullyWithAdminAuthentication() throws Exception {
        // Given
        String customerId = "CUST001";
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        AssetEntity stockAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));
        List<AssetEntity> assets = Arrays.asList(tryAsset, stockAsset);

        when(assetService.getCustomerAssets(customerId)).thenReturn(assets);

        // When & Then
        mockMvc.perform(get("/api/assets")
                        .param("customerId", customerId))
                .andExpect(status().isOk())  // Fixed: was andExpected
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("CUST001"))
                .andExpect(jsonPath("$[0].assetName").value("TRY"))
                .andExpect(jsonPath("$[0].size").value(10000.00))
                .andExpect(jsonPath("$[0].usableSize").value(10000.00))
                .andExpect(jsonPath("$[1].customerId").value("CUST001"))
                .andExpect(jsonPath("$[1].assetName").value("AAPL"))
                .andExpect(jsonPath("$[1].size").value(50.00));

        verify(assetService, times(1)).getCustomerAssets(customerId);
    }

    @Test
    @DisplayName("Should get specific asset successfully")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetSpecificAssetSuccessfully() throws Exception {
        // Given
        String customerId = "CUST001";
        String assetName = "TRY";
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));

        when(assetService.getCustomerAsset(customerId, assetName)).thenReturn(tryAsset);

        // When & Then
        mockMvc.perform(get("/api/assets/{assetName}", assetName)
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.assetName").value("TRY"))
                .andExpect(jsonPath("$.size").value(10000.00))
                .andExpect(jsonPath("$.usableSize").value(10000.00))
                .andExpect(jsonPath("$.reservedAmount").value(0.00));

        verify(assetService, times(1)).getCustomerAsset(customerId, assetName);
    }

    @Test
    @DisplayName("Should require authentication for asset listing")
    void shouldRequireAuthenticationForAssetListing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/assets")
                        .param("customerId", "CUST001"))
                .andExpect(status().isUnauthorized());

        verify(assetService, never()).getCustomerAssets(any(String.class));
    }

    @Test
    @DisplayName("Should require authentication for specific asset retrieval")
    void shouldRequireAuthenticationForSpecificAssetRetrieval() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/assets/TRY")
                        .param("customerId", "CUST001"))
                .andExpect(status().isUnauthorized());

        verify(assetService, never()).getCustomerAsset(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Should handle missing customer ID parameter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleMissingCustomerIdParameter() throws Exception {
        // When & Then - Missing customerId parameter
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isBadRequest());

        verify(assetService, never()).getCustomerAssets(any(String.class));
    }

    @Test
    @DisplayName("Should handle asset not found exception")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleAssetNotFoundException() throws Exception {
        // Given
        String customerId = "CUST001";
        String assetName = "NONEXISTENT";

        when(assetService.getCustomerAsset(customerId, assetName))
                .thenThrow(new AssetNotFoundException(customerId, assetName));

        // When & Then
        mockMvc.perform(get("/api/assets/{assetName}", assetName)
                        .param("customerId", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ASSET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Asset NONEXISTENT not found for customer CUST001"));

        verify(assetService, times(1)).getCustomerAsset(customerId, assetName);
    }

    @Test
    @DisplayName("Should return empty list when customer has no assets")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnEmptyListWhenCustomerHasNoAssets() throws Exception {
        // Given
        String customerId = "CUST999";
        List<AssetEntity> emptyAssets = Arrays.asList();

        when(assetService.getCustomerAssets(customerId)).thenReturn(emptyAssets);

        // When & Then
        mockMvc.perform(get("/api/assets")
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(assetService, times(1)).getCustomerAssets(customerId);
    }

    @Test
    @DisplayName("Should handle assets with reserved amounts")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldHandleAssetsWithReservedAmounts() throws Exception {
        // Given
        String customerId = "CUST001";
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        tryAsset.reserve(new BigDecimal("1500.00")); // Reserve some amount

        List<AssetEntity> assets = Arrays.asList(tryAsset);

        when(assetService.getCustomerAssets(customerId)).thenReturn(assets);

        // When & Then
        mockMvc.perform(get("/api/assets")
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value("CUST001"))
                .andExpect(jsonPath("$[0].assetName").value("TRY"))
                .andExpect(jsonPath("$[0].size").value(10000.00))
                .andExpect(jsonPath("$[0].usableSize").value(8500.00))
                .andExpect(jsonPath("$[0].reservedAmount").value(1500.00));

        verify(assetService, times(1)).getCustomerAssets(customerId);
    }
}