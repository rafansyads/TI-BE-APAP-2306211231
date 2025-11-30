package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.mockito.Mockito;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returnsBadRequestWithMessage() {
        ResponseEntity<BaseResponseDto<Object>> resp = handler.handleIllegalArgument(new IllegalArgumentException("bad stuff"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().contains("bad stuff"));
    }

    @Test
    void handleInvalidBody_usesFirstFieldError() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "fieldOne", "must not be blank"));
        binding.addError(new FieldError("obj", "fieldTwo", "should be positive"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);
        ResponseEntity<BaseResponseDto<Object>> resp = handler.handleInvalidBody(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().startsWith("fieldOne:"));
        assertTrue(resp.getBody().getMessage().contains("must not be blank"));
    }

    @Test
    void handleConstraintViolation_usesFirstViolation() {
        jakarta.validation.Path path = new jakarta.validation.Path() {
            @Override public java.util.Iterator<jakarta.validation.Path.Node> iterator() { return java.util.Collections.emptyIterator(); }
            @Override public String toString() { return "size"; }
        };
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(mockViolation.getMessage()).thenReturn("must be >= 1");
        Mockito.when(mockViolation.getPropertyPath()).thenReturn(path);
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(mockViolation));
        ResponseEntity<BaseResponseDto<Object>> resp = handler.handleConstraintViolation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("size"));
        assertTrue(resp.getBody().getMessage().contains("must be >= 1"));
    }
}
