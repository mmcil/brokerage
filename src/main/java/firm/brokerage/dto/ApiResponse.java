package firm.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(T data, String message) {
        this.success = true;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(String message) {
        this.success = false;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Static factory methods
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "Success");
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message);
    }
}
