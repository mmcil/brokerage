package firm.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {
    private String customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
    private BigDecimal reservedAmount;

    // Static factory method for easy conversion
    public static AssetResponse fromEntity(firm.brokerage.entity.AssetEntity asset) {
        return new AssetResponse(
                asset.getCustomerId(),
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize(),
                asset.getReservedAmount()
        );
    }
}
