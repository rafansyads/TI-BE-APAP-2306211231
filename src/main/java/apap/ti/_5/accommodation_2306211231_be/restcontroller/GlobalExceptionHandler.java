package apap.ti._5.accommodation_2306211231_be.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleIllegalArgument(IllegalArgumentException ex){
        return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleInvalidBody(MethodArgumentNotValidException ex){
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Invalid request body");
        return ResponseUtil.error(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleConstraintViolation(ConstraintViolationException ex){
        String msg = ex.getConstraintViolations().stream().findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Invalid request");
        return ResponseUtil.error(msg, HttpStatus.BAD_REQUEST);
    }
}
