package firm.brokerage.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers
 * Converts exceptions to proper HTTP responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Business Logic Exceptions

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException ex) {
        log.warn("Invalid order status: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATUS", ex.getMessage());
    }

    @ExceptionHandler(AssetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssetNotFound(AssetNotFoundException ex) {
        log.warn("Asset not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "ASSET_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Customer not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCustomer(DuplicateCustomerException ex) {
        log.warn("Duplicate customer: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_CUSTOMER", ex.getMessage());
    }

    // Authentication & Authorization Exceptions

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeactivated(AccountDeactivatedException ex) {
        log.warn("Account deactivated: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_DEACTIVATED", ex.getMessage());
    }

    // Validation Exceptions

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {}", errors);
        String message = "Validation failed: " + errors.toString();
        return createErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_STATE", ex.getMessage());
    }

    // Generic Exception

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred");
    }

    // Helper method to create error responses
    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse error = new ErrorResponse(code, message, LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Standard error response format
     */
    @Getter
    @Setter
    public static class ErrorResponse {
        // Getters and Setters (or use @Data if you prefer Lombok)
        private String code;
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(String code, String message, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
