package firm.brokerage.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AssetEntityTest {

    @Test
    @DisplayName("Should create asset with correct initial values")
    void shouldCreateAssetWithCorrectInitialValues() {
        // Given
        String customerId = "CUST001";
        String assetName = "AAPL";
        BigDecimal size = new BigDecimal("100.00");

        // When
        AssetEntity asset = new AssetEntity(customerId, assetName, size);

        // Then
        assertEquals(customerId, asset.getCustomerId());
        assertEquals(assetName, asset.getAssetName());
        assertEquals(0, size.compareTo(asset.getSize()));
        assertEquals(0, size.compareTo(asset.getUsableSize())); // Initially all is usable
        assertEquals(0, BigDecimal.ZERO.compareTo(asset.getReservedAmount()));
    }

    @Test
    @DisplayName("Should reserve amount correctly")
    void shouldReserveAmountCorrectly() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        BigDecimal reserveAmount = new BigDecimal("300.00");

        // When
        asset.reserve(reserveAmount);

        // Then
        assertEquals(0, new BigDecimal("1000.00").compareTo(asset.getSize()));
        assertEquals(0, new BigDecimal("700.00").compareTo(asset.getUsableSize()));
        assertEquals(0, reserveAmount.compareTo(asset.getReservedAmount()));
    }

    @Test
    @DisplayName("Should throw exception when reserving more than usable amount")
    void shouldThrowExceptionWhenReservingMoreThanUsableAmount() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("100.00"));
        BigDecimal reserveAmount = new BigDecimal("150.00");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> asset.reserve(reserveAmount)
        );

        assertTrue(exception.getMessage().contains("Insufficient usable amount"));
    }

    @Test
    @DisplayName("Should release reserved amount correctly")
    void shouldReleaseReservedAmountCorrectly() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        asset.reserve(new BigDecimal("300.00"));

        // When
        asset.release(new BigDecimal("100.00"));

        // Then
        assertEquals(0, new BigDecimal("1000.00").compareTo(asset.getSize()));
        assertEquals(0, new BigDecimal("800.00").compareTo(asset.getUsableSize()));
        assertEquals(0, new BigDecimal("200.00").compareTo(asset.getReservedAmount()));
    }

    @Test
    @DisplayName("Should check sufficient usable amount correctly")
    void shouldCheckSufficientUsableAmountCorrectly() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        asset.reserve(new BigDecimal("300.00")); // 700 usable

        // When & Then
        assertTrue(asset.hasSufficientUsableAmount(new BigDecimal("500.00")));
        assertTrue(asset.hasSufficientUsableAmount(new BigDecimal("700.00")));
        assertFalse(asset.hasSufficientUsableAmount(new BigDecimal("800.00")));
    }

    @Test
    @DisplayName("Should identify TRY asset correctly")
    void shouldIdentifyTryAssetCorrectly() {
        // Given
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        AssetEntity stockAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));

        // When & Then
        assertTrue(tryAsset.isTryAsset());
        assertFalse(stockAsset.isTryAsset());
    }
}