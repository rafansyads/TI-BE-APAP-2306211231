package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;

class GlobalExceptionHandlerAdviceTests {

    @Test
    void handleIllegalArgument_returns400() {
        var handler = new GlobalExceptionHandler();
        var resp = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertEquals(400, resp.getStatusCode().value());
        BaseResponseDto<?> body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertTrue(body.getMessage().contains("bad arg"));
    }

    @Test
    void handleInvalidBody_returns400WithFirstFieldError() throws Exception {
        var binding = new BeanPropertyBindingResult(new Object(), "req");
        binding.addError(new FieldError("req", "customerPhone", "must not be blank"));
        MethodParameter mp = Mockito.mock(MethodParameter.class);
        var ex = new MethodArgumentNotValidException(mp, binding);
        var handler = new GlobalExceptionHandler();
    var resp = handler.handleInvalidBody(ex);
    assertEquals(400, resp.getStatusCode().value());
    var body = resp.getBody();
    assertNotNull(body);
    assertTrue(body.getMessage().contains("customerPhone"));
    }
}
