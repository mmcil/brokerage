package firm.brokerage.repository;

import firm.brokerage.entity.AssetEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AssetRepositoryTest {

    @Autowired
    private AssetRepository assetRepository;

    @Test
    @DisplayName("Should save and find asset by customer ID and asset name")
    void shouldSaveAndFindAssetByCustomerIdAndAssetName() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "AAPL", new BigDecimal("100.00"));
        assetRepository.save(asset);

        // When
        Optional<AssetEntity> result = assetRepository.findByCustomerIdAndAssetName("CUST001", "AAPL");

        // Then
        assertTrue(result.isPresent());
        assertEquals("CUST001", result.get().getCustomerId());
        assertEquals("AAPL", result.get().getAssetName());
        assertEquals(new BigDecimal("100.00"), result.get().getSize());
        assertEquals(new BigDecimal("100.00"), result.get().getUsableSize());
    }

    @Test
    @DisplayName("Should find all assets by customer ID")
    void shouldFindAllAssetsByCustomerId() {
        // Given
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        AssetEntity stockAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));
        AssetEntity otherCustomerAsset = new AssetEntity("CUST002", "GOOGL", new BigDecimal("20.00"));

        assetRepository.save(tryAsset);
        assetRepository.save(stockAsset);
        assetRepository.save(otherCustomerAsset);

        // When
        List<AssetEntity> result = assetRepository.findByCustomerId("CUST001");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> "TRY".equals(a.getAssetName())));
        assertTrue(result.stream().anyMatch(a -> "AAPL".equals(a.getAssetName())));
        assertFalse(result.stream().anyMatch(a -> "GOOGL".equals(a.getAssetName())));
    }

    @Test
    @DisplayName("Should return empty list when customer has no assets")
    void shouldReturnEmptyListWhenCustomerHasNoAssets() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "AAPL", new BigDecimal("100.00"));
        assetRepository.save(asset);

        // When
        List<AssetEntity> result = assetRepository.findByCustomerId("CUST999");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty optional when asset not found")
    void shouldReturnEmptyOptionalWhenAssetNotFound() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "AAPL", new BigDecimal("100.00"));
        assetRepository.save(asset);

        // When
        Optional<AssetEntity> result = assetRepository.findByCustomerIdAndAssetName("CUST001", "GOOGL");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle asset with reserved amount")
    void shouldHandleAssetWithReservedAmount() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        asset.reserve(new BigDecimal("300.00")); // Reserve 300, leaving 700 usable
        assetRepository.save(asset);

        // When
        Optional<AssetEntity> result = assetRepository.findByCustomerIdAndAssetName("CUST001", "TRY");

        // Then
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("1000.00"), result.get().getSize());
        assertEquals(new BigDecimal("700.00"), result.get().getUsableSize());
        assertEquals(new BigDecimal("300.00"), result.get().getReservedAmount());
        assertTrue(result.get().hasSufficientUsableAmount(new BigDecimal("500.00")));
        assertFalse(result.get().hasSufficientUsableAmount(new BigDecimal("800.00")));
    }

    @Test
    @DisplayName("Should save and retrieve multiple assets for same customer")
    void shouldSaveAndRetrieveMultipleAssetsForSameCustomer() {
        // Given
        AssetEntity tryAsset = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        AssetEntity appleAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));
        AssetEntity googleAsset = new AssetEntity("CUST001", "GOOGL", new BigDecimal("25.00"));

        assetRepository.save(tryAsset);
        assetRepository.save(appleAsset);
        assetRepository.save(googleAsset);

        // When
        List<AssetEntity> assets = assetRepository.findByCustomerId("CUST001");

        // Then
        assertEquals(3, assets.size());

        // Verify each asset exists
        assertTrue(assets.stream().anyMatch(a ->
                "TRY".equals(a.getAssetName()) && new BigDecimal("10000.00").equals(a.getSize())));
        assertTrue(assets.stream().anyMatch(a ->
                "AAPL".equals(a.getAssetName()) && new BigDecimal("50.00").equals(a.getSize())));
        assertTrue(assets.stream().anyMatch(a ->
                "GOOGL".equals(a.getAssetName()) && new BigDecimal("25.00").equals(a.getSize())));
    }

    @Test
    @DisplayName("Should handle composite key correctly")
    void shouldHandleCompositeKeyCorrectly() {
        // Given - Same asset name for different customers
        AssetEntity asset1 = new AssetEntity("CUST001", "AAPL", new BigDecimal("100.00"));
        AssetEntity asset2 = new AssetEntity("CUST002", "AAPL", new BigDecimal("200.00"));

        assetRepository.save(asset1);
        assetRepository.save(asset2);

        // When
        Optional<AssetEntity> result1 = assetRepository.findByCustomerIdAndAssetName("CUST001", "AAPL");
        Optional<AssetEntity> result2 = assetRepository.findByCustomerIdAndAssetName("CUST002", "AAPL");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(new BigDecimal("100.00"), result1.get().getSize());
        assertEquals(new BigDecimal("200.00"), result2.get().getSize());
        assertNotEquals(result1.get().getCustomerId(), result2.get().getCustomerId());
    }

    @Test
    @DisplayName("Should persist asset modifications correctly")
    void shouldPersistAssetModificationsCorrectly() {
        // Given
        AssetEntity asset = new AssetEntity("CUST001", "TRY", new BigDecimal("1000.00"));
        AssetEntity savedAsset = assetRepository.save(asset);

        // When - Modify the asset
        savedAsset.reserve(new BigDecimal("200.00"));
        savedAsset.increase(new BigDecimal("500.00"));
        assetRepository.save(savedAsset);

        // Then - Retrieve and verify
        Optional<AssetEntity> result = assetRepository.findByCustomerIdAndAssetName("CUST001", "TRY");
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("1500.00"), result.get().getSize()); // 1000 + 500
        assertEquals(new BigDecimal("1300.00"), result.get().getUsableSize()); // 1500 - 200
        assertEquals(new BigDecimal("200.00"), result.get().getReservedAmount());
    }
}