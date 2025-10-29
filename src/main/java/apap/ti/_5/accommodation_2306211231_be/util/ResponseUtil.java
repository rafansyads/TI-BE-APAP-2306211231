package apap.ti._5.accommodation_2306211231_be.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;

import java.util.Date;

/**
 * Utility to build standardized REST responses using {@link io.hafizmuh.apaplib.dto.BaseResponseDTO}.
 * Wrap all REST controller responses with this to keep the format consistent.
 */
@Component
public class ResponseUtil {
    /**
     * Build a success response with payload.
     * @param data domain/DTO payload to return
     * @param message human friendly success message
     * @param status HTTP status to send (e.g., 200, 201)
     */
    public static <T> ResponseEntity<BaseResponseDto<T>> success(T data, String message, HttpStatus status) {
        BaseResponseDto<T> response = new BaseResponseDto<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(new Date());
        return new ResponseEntity<>(response, status);
    }

    /**
     * Build an error response without payload.
     * @param message error details suitable for clients
     * @param status HTTP error status (e.g., 400, 404, 409, 500)
     */
    public static <T> ResponseEntity<BaseResponseDto<T>> error(String message, HttpStatus status) {
        BaseResponseDto<T> response = new BaseResponseDto<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(new Date());
        return new ResponseEntity<>(response, status);
    }
}