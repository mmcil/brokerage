package firm.brokerage.repository;

import firm.brokerage.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    // Find customer by username for authentication
    Optional<CustomerEntity> findByUsername(String username);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find active customers only
    Optional<CustomerEntity> findByUsernameAndActiveTrue(String username);
}