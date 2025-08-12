package firm.brokerage.controller;

import firm.brokerage.dto.AssetResponse;
import firm.brokerage.entity.AssetEntity;
import firm.brokerage.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for asset management operations
 * Handles listing customer assets
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;

    /**
     * List all assets for a customer
     * GET /api/assets?customerId=CUST001
     */
    @GetMapping
    public ResponseEntity<List<AssetResponse>> listAssets(@RequestParam String customerId) {
        log.info("Listing assets for customer: {}", customerId);

        List<AssetEntity> assets = assetService.getCustomerAssets(customerId);
        List<AssetResponse> responses = assets.stream()
                .map(AssetResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get specific asset for a customer
     * GET /api/assets/{assetName}?customerId=CUST001
     */
    @GetMapping("/{assetName}")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable String assetName,
            @RequestParam String customerId) {

        log.debug("Getting asset {} for customer {}", assetName, customerId);

        AssetEntity asset = assetService.getCustomerAsset(customerId, assetName);
        AssetResponse response = AssetResponse.fromEntity(asset);

        return ResponseEntity.ok(response);
    }
}
