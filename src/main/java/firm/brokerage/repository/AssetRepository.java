package firm.brokerage.repository;

import firm.brokerage.entity.AssetEntity;
import firm.brokerage.entity.AssetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, AssetId> {

    /**
     * Find all assets for a customer
     */
    List<AssetEntity> findByCustomerId(String customerId);

    /**
     * Find specific asset for a customer
     */
    Optional<AssetEntity> findByCustomerIdAndAssetName(String customerId, String assetName);

    /**
     * Find all customers who have a specific asset
     */
    List<AssetEntity> findByAssetName(String assetName);

    /**
     * Find assets with usable size greater than or equal to specified amount
     */
    @Query("SELECT a FROM AssetEntity a WHERE a.customerId = :customerId AND a.usableSize >= :minUsableSize")
    List<AssetEntity> findByCustomerIdAndUsableSizeGreaterThanEqual(
            @Param("customerId") String customerId,
            @Param("minUsableSize") BigDecimal minUsableSize);

    /**
     * Delete all assets for a customer (useful for testing)
     */
    void deleteByCustomerId(String customerId);
}