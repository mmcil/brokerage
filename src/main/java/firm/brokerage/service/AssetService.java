package firm.brokerage.service;

import firm.brokerage.entity.AssetEntity;
import firm.brokerage.entity.AssetId;
import firm.brokerage.entity.OrderSide;
import firm.brokerage.exception.AssetNotFoundException;
import firm.brokerage.exception.InsufficientFundsException;
import firm.brokerage.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private static final String TRY_ASSET = "TRY";

    /**
     * Get all assets for a customer
     */
    @Transactional(readOnly = true)
    public List<AssetEntity> getCustomerAssets(String customerId) {
        log.debug("Getting assets for customer: {}", customerId);
        return assetRepository.findByCustomerId(customerId);
    }

    /**
     * Get specific asset for a customer
     */
    @Transactional(readOnly = true)
    public AssetEntity getCustomerAsset(String customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(customerId, assetName));
    }

    /**
     * Reserve assets for order creation
     */
    public void reserveAssetsForOrder(String customerId, String assetName, OrderSide orderSide,
                                      BigDecimal size, BigDecimal price) {
        if (orderSide == OrderSide.BUY) {
            // For BUY orders, reserve TRY (money)
            BigDecimal requiredTry = size.multiply(price);
            reserveTryAsset(customerId, requiredTry);
        } else {
            // For SELL orders, reserve the asset being sold
            reserveAsset(customerId, assetName, size);
        }
    }

    /**
     * Release assets when order is canceled
     */
    public void releaseAssetsForOrder(String customerId, String assetName, OrderSide orderSide,
                                      BigDecimal size, BigDecimal price) {
        if (orderSide == OrderSide.BUY) {
            // Release TRY
            BigDecimal tryAmount = size.multiply(price);
            releaseAsset(customerId, TRY_ASSET, tryAmount);
        } else {
            // Release the asset
            releaseAsset(customerId, assetName, size);
        }
    }

    /**
     * Process matched order
     */
    public void processMatchedOrder(String customerId, String assetName, OrderSide orderSide,
                                    BigDecimal size, BigDecimal price) {
        if (orderSide == OrderSide.BUY) {
            // Customer bought asset: decrease TRY, increase asset
            BigDecimal tryAmount = size.multiply(price);
            decreaseAsset(customerId, TRY_ASSET, tryAmount);
            increaseAsset(customerId, assetName, size);
        } else {
            // Customer sold asset: decrease asset, increase TRY
            decreaseAsset(customerId, assetName, size);
            BigDecimal tryAmount = size.multiply(price);
            increaseAsset(customerId, TRY_ASSET, tryAmount);
        }
    }

    /**
     * Create or update asset
     */
    public AssetEntity createOrUpdateAsset(String customerId, String assetName, BigDecimal size) {
        AssetId assetId = new AssetId(customerId, assetName);

        return assetRepository.findById(assetId)
                .map(existing -> {
                    existing.increase(size);
                    return assetRepository.save(existing);
                })
                .orElseGet(() -> {
                    AssetEntity newAsset = new AssetEntity(customerId, assetName, size);
                    return assetRepository.save(newAsset);
                });
    }

    // Private helper methods

    private void reserveTryAsset(String customerId, BigDecimal amount) {
        AssetEntity tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY_ASSET)
                .orElseThrow(() -> new AssetNotFoundException(customerId, TRY_ASSET));

        if (!tryAsset.hasSufficientUsableAmount(amount)) {
            throw new InsufficientFundsException(customerId, TRY_ASSET,
                    amount.toString(), tryAsset.getUsableSize().toString());
        }

        tryAsset.reserve(amount);
        assetRepository.save(tryAsset);
        log.debug("Reserved {} TRY for customer {}", amount, customerId);
    }

    private void reserveAsset(String customerId, String assetName, BigDecimal amount) {
        AssetEntity asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(customerId, assetName));

        if (!asset.hasSufficientUsableAmount(amount)) {
            throw new InsufficientFundsException(customerId, assetName,
                    amount.toString(), asset.getUsableSize().toString());
        }

        asset.reserve(amount);
        assetRepository.save(asset);
        log.debug("Reserved {} {} for customer {}", amount, assetName, customerId);
    }

    private void releaseAsset(String customerId, String assetName, BigDecimal amount) {
        AssetEntity asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(customerId, assetName));

        asset.release(amount);
        assetRepository.save(asset);
        log.debug("Released {} {} for customer {}", amount, assetName, customerId);
    }

    private void decreaseAsset(String customerId, String assetName, BigDecimal amount) {
        AssetEntity asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(customerId, assetName));

        asset.decrease(amount);
        assetRepository.save(asset);
        log.debug("Decreased {} {} for customer {}", amount, assetName, customerId);
    }

    private void increaseAsset(String customerId, String assetName, BigDecimal amount) {
        AssetEntity asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElse(new AssetEntity(customerId, assetName, BigDecimal.ZERO));

        asset.increase(amount);
        assetRepository.save(asset);
        log.debug("Increased {} {} for customer {}", amount, assetName, customerId);
    }
}
