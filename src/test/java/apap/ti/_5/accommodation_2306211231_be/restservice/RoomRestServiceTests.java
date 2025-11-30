package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;

class RoomRestServiceTests {

    @Mock
    private RoomRepository roomRepository;

    private RoomRestService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RoomRestService(roomRepository);
    }

    @Test
    void construction_success() {
        // Test that service can be instantiated
        assertNotNull(service);
    }

    // The service currently has commented-out methods. 
    // This test file provides baseline coverage for the class construction.
    // If methods are uncommented in the future, additional tests should be added.
}
