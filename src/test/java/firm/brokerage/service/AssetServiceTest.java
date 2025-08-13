package firm.brokerage.service;

import firm.brokerage.entity.AssetEntity;
import firm.brokerage.entity.AssetId;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.exception.AssetNotFoundException;
import firm.brokerage.exception.InsufficientFundsException;
import firm.brokerage.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private AssetEntity tryAsset;
    private AssetEntity stockAsset;

    // Helper method for BigDecimal assertions
    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected: " + expected + " but was: " + actual);
    }

    @BeforeEach
    void setUp() {
        tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        stockAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should get customer assets successfully")
    void shouldGetCustomerAssetsSuccessfully() {
        // Given
        String customerId = "CUST001";
        List<AssetEntity> expectedAssets = Arrays.asList(tryAsset, stockAsset);
        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        // When
        List<AssetEntity> result = assetService.getCustomerAssets(customerId);

        // Then
        assertEquals(expectedAssets, result);
        verify(assetRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should get specific customer asset successfully")
    void shouldGetSpecificCustomerAssetSuccessfully() {
        // Given
        String customerId = "CUST001";
        String assetName = "TRY";
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(Optional.of(tryAsset));

        // When
        AssetEntity result = assetService.getCustomerAsset(customerId, assetName);

        // Then
        assertEquals(tryAsset, result);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
    }

    @Test
    @DisplayName("Should throw exception when asset not found")
    void shouldThrowExceptionWhenAssetNotFound() {
        // Given
        String customerId = "CUST001";
        String assetName = "NONEXISTENT";
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(Optional.empty());

        // When & Then
        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.getCustomerAsset(customerId, assetName)
        );

        assertTrue(exception.getMessage().contains(assetName));
        assertTrue(exception.getMessage().contains(customerId));
    }

    @Test
    @DisplayName("Should reserve assets for BUY order successfully")
    void shouldReserveAssetsForBuyOrderSuccessfully() {
        // Given
        String customerId = "CUST001";
        String assetName = "AAPL";
        OrderSide orderSide = OrderSide.BUY;
        BigDecimal size = new BigDecimal("10.00");
        BigDecimal price = new BigDecimal("150.00");
        BigDecimal requiredTry = new BigDecimal("1500.00");

        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(AssetEntity.class))).thenReturn(tryAsset);

        // When
        assetService.reserveAssetsForOrder(customerId, assetName, orderSide, size, price);

        // Then
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");
        verify(assetRepository).save(tryAsset);
        // Fixed: Use BigDecimal comparison instead of assertEquals
        assertBigDecimalEquals(new BigDecimal("8500.00"), tryAsset.getUsableSize());
    }

    @Test
    @DisplayName("Should reserve assets for SELL order successfully")
    void shouldReserveAssetsForSellOrderSuccessfully() {
        // Given
        String customerId = "CUST001";
        String assetName = "AAPL";
        OrderSide orderSide = OrderSide.SELL;
        BigDecimal size = new BigDecimal("10.00");
        BigDecimal price = new BigDecimal("150.00");

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(Optional.of(stockAsset));
        when(assetRepository.save(any(AssetEntity.class))).thenReturn(stockAsset);

        // When
        assetService.reserveAssetsForOrder(customerId, assetName, orderSide, size, price);

        // Then
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
        verify(assetRepository).save(stockAsset);
        // Fixed: Use BigDecimal comparison instead of assertEquals
        assertBigDecimalEquals(new BigDecimal("40.00"), stockAsset.getUsableSize());
    }

    @Test
    @DisplayName("Should throw exception when insufficient TRY for BUY order")
    void shouldThrowExceptionWhenInsufficientTryForBuyOrder() {
        // Given
        String customerId = "CUST001";
        String assetName = "AAPL";
        OrderSide orderSide = OrderSide.BUY;
        BigDecimal size = new BigDecimal("100.00");
        BigDecimal price = new BigDecimal("200.00"); // Requires 20,000 TRY but only have 10,000

        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        // When & Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> assetService.reserveAssetsForOrder(customerId, assetName, orderSide, size, price)
        );

        assertTrue(exception.getMessage().contains("TRY"));
        assertTrue(exception.getMessage().contains(customerId));
    }
}