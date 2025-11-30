package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomRestService {

    private final RoomRepository roomRepository;

    // Entity helpers (to be implemented later)
    // public Optional<Room> getById(String roomId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public List<Room> getByRoomTypeId(String roomTypeId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public List<Room> getByPropertyId(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // // DTO helpers (to be implemented later)
    // public RoomDetailDto getDetailDto(String roomId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public List<RoomSummaryDto> getSummariesByRoomType(String roomTypeId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }
}
