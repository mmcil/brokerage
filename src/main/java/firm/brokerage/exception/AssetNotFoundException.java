package firm.brokerage.exception;

/**
 * Thrown when an asset cannot be found for a customer
 */
public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String customerId, String assetName) {
        super(String.format("Asset %s not found for customer %s", assetName, customerId));
    }

    public AssetNotFoundException(String message) {
        super(message);
    }
}
