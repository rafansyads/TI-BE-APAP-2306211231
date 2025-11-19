package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restmapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomTypeRestService {

    private final RoomTypeRepository roomTypeRepository;

    // Entity helpers (to be implemented later)
    public Optional<RoomType> getById(String roomTypeId) {
        return roomTypeRepository.findById(roomTypeId);
    }

    // public List<RoomType> getByPropertyId(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // DTO helpers (to be implemented later)
    public RoomTypeDetailDto getDetailDto(String roomTypeId) {
        return RoomTypeMapper.toDetailDto(
                getById(roomTypeId)
                .orElseThrow(() -> 
                new IllegalArgumentException(
                    "RoomType not found: " + 
                    roomTypeId)
                    )
        );
    }

    // public List<RoomTypeSummaryDto> getSummariesByProperty(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public RoomTypeDetailDto create(RoomTypeCreateRequest request) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public RoomTypeDetailDto update(String roomTypeId, RoomTypeUpdateRequest request) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }
}
