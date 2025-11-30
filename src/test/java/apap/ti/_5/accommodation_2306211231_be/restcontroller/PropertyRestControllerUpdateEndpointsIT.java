package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManagerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;

@WebMvcTest(controllers = PropertyRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class PropertyRestControllerUpdateEndpointsIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PropertyRestService propertyService;
    @MockBean AuthRestService authRestService;
    @MockBean AccommodationReviewRestService reviewService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsService userDetailsService;
    @MockBean EntityManagerFactory entityManagerFactory;

    private PropertyDetailDto sampleDetail(String id) {
        PropertyDetailDto dto = new PropertyDetailDto();
        dto.setPropertyId(id);
        dto.setPropertyName("Hotel Alpha");
        return dto;
    }

    @Test
    void updateProperty_success() throws Exception {
        var update = new PropertyUpdateRequest();
        update.setPropertyId("HOT-ALPHA-1");
        update.setPropertyName("Hotel Alpha Updated");
        update.setAddress("Jl. Kebon Jeruk");
        update.setOwnerName("Owner A");
        update.setOwnerId("123e4567-e89b-12d3-a456-426614174000");
        update.setActiveStatus(1);
        RoomTypeUpdateRequest rtUpd = new RoomTypeUpdateRequest();
        rtUpd.setRoomTypeId("HOT-ALPHA-1-STD-1");
        rtUpd.setName("Standard");
        rtUpd.setPrice(500000);
        rtUpd.setFloor(1);
        rtUpd.setCapacity(2);
        update.setRoomTypes(List.of(rtUpd));
        var body = new BaseRequestDto<PropertyUpdateRequest>();
        body.setData(update);

        when(propertyService.updateProperty(eq("HOT-ALPHA-1"), any(PropertyUpdateRequest.class)))
            .thenReturn(sampleDetail("HOT-ALPHA-1"));

        mockMvc.perform(put("/property/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.propertyId").value("HOT-ALPHA-1"));
    }

    @Test
    void updateRooms_success() throws Exception {
        var reqDto = RoomTypeCreateRequest.builder()
            .propertyId("HOT-ALPHA-1")
            .name("New Deluxe")
            .floor(2)
            .price(750000)
            .capacity(3)
            .build();
        var body = new BaseRequestDto<RoomTypeCreateRequest>();
        body.setData(reqDto);
        when(propertyService.updatePropertyRooms(eq("HOT-ALPHA-1"), any(RoomTypeCreateRequest.class)))
            .thenReturn(sampleDetail("HOT-ALPHA-1"));

        mockMvc.perform(post("/property/updateroom")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.propertyId").value("HOT-ALPHA-1"));
    }

    @Test
    void recomputeTotalRooms_success() throws Exception {
        when(propertyService.recomputeTotalRooms("HOT-ALPHA-1"))
            .thenReturn(sampleDetail("HOT-ALPHA-1"));

        mockMvc.perform(post("/property/recompute-totalrooms/HOT-ALPHA-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.propertyId").value("HOT-ALPHA-1"));
    }

    @Test
    void updateProperty_validationError_returns400() throws Exception {
        var update = new PropertyUpdateRequest();
        update.setPropertyId(""); // invalid blank triggers @NotBlank
        var body = new BaseRequestDto<PropertyUpdateRequest>();
        body.setData(update);
        // Simulate service throwing IllegalArgumentException which controller maps to 400
        when(propertyService.updateProperty(any(String.class), any(PropertyUpdateRequest.class)))
            .thenThrow(new IllegalArgumentException("bad request"));

        mockMvc.perform(put("/property/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }
}
