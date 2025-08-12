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

    // Find all assets for a customer
    List<AssetEntity> findByCustomerId(String customerId);

    // Find specific asset for a customer
    Optional<AssetEntity> findByCustomerIdAndAssetName(String customerId, String assetName);

    // Check if customer has TRY asset with sufficient amount
    @Query("SELECT CASE WHEN a.usableSize >= :requiredAmount THEN true ELSE false END " +
            "FROM AssetEntity a WHERE a.customerId = :customerId AND a.assetName = 'TRY'")
    Optional<Boolean> hasSufficientTryAmount(@Param("customerId") String customerId,
                                             @Param("requiredAmount") BigDecimal requiredAmount);

    // Check if customer has specific asset with sufficient amount
    @Query("SELECT CASE WHEN a.usableSize >= :requiredAmount THEN true ELSE false END " +
            "FROM AssetEntity a WHERE a.customerId = :customerId AND a.assetName = :assetName")
    Optional<Boolean> hasSufficientAssetAmount(@Param("customerId") String customerId,
                                               @Param("assetName") String assetName,
                                               @Param("requiredAmount") BigDecimal requiredAmount);
}