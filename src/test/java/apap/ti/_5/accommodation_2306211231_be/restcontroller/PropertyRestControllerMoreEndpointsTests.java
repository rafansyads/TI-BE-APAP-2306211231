package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.restservice.PropertyRestService;

@ExtendWith(MockitoExtension.class)
class PropertyRestControllerMoreEndpointsTests {

    @Mock private PropertyRestService propertyRestService;
    @InjectMocks private PropertyRestController controller;
    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listPropertiesEmptyReturns200() throws Exception {
        when(propertyRestService.getAllPropertiesDto()).thenReturn(new ArrayList<>());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/property")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(200)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data").isArray());
    }

    @Test
    void getPropertySuccess() throws Exception {
        PropertyDetailDto dto = new PropertyDetailDto(); dto.setPropertyId("HOT-ABCD-001");
        when(propertyRestService.getPropertyDetailDto("HOT-ABCD-001")).thenReturn(dto);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/property/HOT-ABCD-001"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.propertyId", is("HOT-ABCD-001")));
    }

    @Test
    void getPropertyNotFoundReturns404() throws Exception {
        when(propertyRestService.getPropertyDetailDto("MISSING")).thenThrow(new IllegalArgumentException("not found"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/property/MISSING"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(404)));
    }

    @Test
    void deletePropertySuccess() throws Exception {
        doNothing().when(propertyRestService).softDeleteProperty("HOT-ABCD-001");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/property/delete/HOT-ABCD-001"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(200)));

        verify(propertyRestService, times(1)).softDeleteProperty("HOT-ABCD-001");
    }

    @Test
    void deletePropertyNotFoundReturns404() throws Exception {
        doThrow(new IllegalArgumentException("not found")).when(propertyRestService).softDeleteProperty("MISSING");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/property/delete/MISSING"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void addMaintenanceSuccess() throws Exception {
        RoomDetailDto detail = new RoomDetailDto();
        detail.setRoomId("HOT-ABCD-001-201");
    when(propertyRestService.addMaintenance(org.mockito.ArgumentMatchers.any(RoomUpdateRequest.class))).thenReturn(detail);

        RoomUpdateRequest body = RoomUpdateRequest.builder().id("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(java.time.LocalDateTime.now().plusDays(1).toString())
            .maintenanceEnd(java.time.LocalDateTime.now().plusDays(2).toString())
            .roomTypeId("001-Deluxe-2").build();
        BaseRequestDto<RoomUpdateRequest> req = new BaseRequestDto<>(); req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(200)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.roomId", is("HOT-ABCD-001-201")));
    }

    @Test
    void addMaintenanceErrorReturns400() throws Exception {
    when(propertyRestService.addMaintenance(org.mockito.ArgumentMatchers.any(RoomUpdateRequest.class))).thenThrow(new IllegalArgumentException("bad"));

        RoomUpdateRequest body = RoomUpdateRequest.builder().id("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(java.time.LocalDateTime.now().plusDays(1).toString())
            .maintenanceEnd(java.time.LocalDateTime.now().plusDays(2).toString())
            .roomTypeId("001-Deluxe-2").build();
        BaseRequestDto<RoomUpdateRequest> req = new BaseRequestDto<>(); req.setData(body);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status", is(400)));
    }
}
