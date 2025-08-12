package firm.brokerage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key for AssetEntity (customerId + assetName)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetId implements Serializable {
    private String customerId;
    private String assetName;
}