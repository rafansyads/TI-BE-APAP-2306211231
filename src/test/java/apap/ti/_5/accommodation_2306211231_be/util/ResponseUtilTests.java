package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;

class ResponseUtilTests {

    @Test
    void successBuildsResponse() {
        ResponseEntity<BaseResponseDto<String>> resp = ResponseUtil.success("ok", "msg", HttpStatus.CREATED).toBuilder().build();
        assertEquals(201, resp.getStatusCode().value());
        BaseResponseDto<String> body = resp.getBody();
        assertNotNull(body);
        assertEquals(201, body.getStatus());
        assertEquals("msg", body.getMessage());
        assertEquals("ok", body.getData());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void errorBuildsResponse() {
        ResponseEntity<BaseResponseDto<Void>> resp = ResponseUtil.error("bad", HttpStatus.BAD_REQUEST);
        assertEquals(400, resp.getStatusCode().value());
        BaseResponseDto<Void> body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("bad", body.getMessage());
        assertNull(body.getData());
        assertNotNull(body.getTimestamp());
    }
}
