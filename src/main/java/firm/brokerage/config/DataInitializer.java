package firm.brokerage.config;

import firm.brokerage.entity.AssetEntity;
import firm.brokerage.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initialize essential data after application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AssetRepository assetRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing database with essential data...");

        // Ensure we have some TRY assets for testing if database is empty
        if (assetRepository.count() == 0) {
            log.info("Database is empty, creating sample data...");
            createSampleData();
        } else {
            log.info("Database already contains data, skipping initialization");
        }

        log.info("Data initialization completed");
    }

    private void createSampleData() {
        // This method can be used if data.sql doesn't execute properly
        // Create some basic TRY assets for testing

        AssetEntity tryAsset1 = new AssetEntity("CUST001", "TRY", new BigDecimal("10000.00"));
        AssetEntity tryAsset2 = new AssetEntity("CUST002", "TRY", new BigDecimal("15000.00"));
        AssetEntity appleAsset = new AssetEntity("CUST001", "AAPL", new BigDecimal("50.00"));

        assetRepository.save(tryAsset1);
        assetRepository.save(tryAsset2);
        assetRepository.save(appleAsset);

        log.info("Created sample assets for testing");
    }
}
