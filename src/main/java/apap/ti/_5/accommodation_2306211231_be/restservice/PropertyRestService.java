package apap.ti._5.accommodation_2306211231_be.restservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.OwnerSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;
import apap.ti._5.accommodation_2306211231_be.restmapper.PropertyMapper;
import apap.ti._5.accommodation_2306211231_be.restmapper.RoomMapper;
import apap.ti._5.accommodation_2306211231_be.restmapper.RoomTypeMapper;
import apap.ti._5.accommodation_2306211231_be.util.DateUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import apap.ti._5.accommodation_2306211231_be.util.ProvinceUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyRestService {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final AccommodationOwnerRepository accommodationOwnerRepository;

    public long count() {
        return propertyRepository.countByDeletedAtIsNull();
    }

    // Placeholders to be implemented later
    public List<Property> getAllProperties() {
        return propertyRepository.findByDeletedAtIsNull();
    }

    public Optional<Property> getPropertyById(String propertyId) {
        return propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId);
    }

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    // public Property updateProperty(String propertyId, Property property) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public void deleteProperty(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // // Room & RoomType placeholders to demonstrate repository availability
    // public List<Room> getRoomsByProperty(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public List<RoomType> getRoomTypesByProperty(String propertyId) {
    //     throw new UnsupportedOperationException("Not implemented yet");
    // }

    // DTO-based methods for controllers
    public List<PropertySummaryDto> getAllPropertiesDto() {
        return propertyRepository.findByDeletedAtIsNull().stream()
                .map(PropertyMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public PropertyDetailDto getPropertyDetailDto(String propertyId) {
        Property p = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        return PropertyMapper.toDetailDto(p);
    }

    /**
     * Return property details and, when date range is provided, filter the rooms list
     * to only include rooms available (no maintenance and no overlapping bookings) in that range.
     */
    public PropertyDetailDto getPropertyDetailDto(String propertyId, String checkIn, String checkOut) {
        Property p = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        // If no filters, regular mapping
        if (checkIn == null || checkIn.isBlank() || checkOut == null || checkOut.isBlank()) {
            return PropertyMapper.toDetailDto(p);
        }

        // Compose normalized anchors (14:00 in, 12:00 out)
        LocalDateTime in = DateUtil.normalizeCheckIn(LocalDateTime.parse(checkIn + "T00:00:00"));
        LocalDateTime out = DateUtil.normalizeCheckOut(LocalDateTime.parse(checkOut + "T00:00:00"));

        // Start with base dto
        PropertyDetailDto dto = PropertyMapper.toDetailDto(p);
        if (dto == null) return null;

        // Evaluate rooms availability but keep all rooms; mark availability via computed availabilityStatus (1=available,0=unavailable)
        List<Room> filteredRooms = new ArrayList<>();
        if (p.getListRoomType() != null) {
            for (RoomType rt : p.getListRoomType()) {
                if (rt.getListRoom() == null) continue;
                for (Room r : rt.getListRoom()) {
                    // Compute dynamic availability for requested window.
                    boolean available = true;
                    // Active flag
                    if (r.getActiveRoom() != null && r.getActiveRoom() == 0) available = false;
                    // Maintenance overlap
                    if (available && r.getMaintenanceStart() != null && r.getMaintenanceEnd() != null
                        && DateUtil.isOverlapping(in, out, r.getMaintenanceStart(), r.getMaintenanceEnd())) {
                        available = false;
                    }
                    // Booking overlap (exclude canceled)
                    if (available && r.getBookings() != null) {
                        for (AccommodationBooking b : r.getBookings()) {
                            Integer st = b.getStatus();
                            if (st != null && st == 2) continue;
                            if (b.getCheckInDate() != null && b.getCheckOutDate() != null
                                && DateUtil.isOverlapping(in, out, b.getCheckInDate(), b.getCheckOutDate())) {
                                available = false; break;
                            }
                        }
                    }
                    // Mutate transient availabilityStatus for mapping (1=available,0=unavailable)
                    r.setAvailabilityStatus(available ? 1 : 0);
                    filteredRooms.add(r);
                }
            }
        }

        // Remap rooms list to DTO (include all rooms with updated availabilityStatus)
        List<RoomDetailDto> roomDtos = filteredRooms.stream()
                .map(RoomMapper::toDetailDto)
                .collect(Collectors.toList());
        dto.setRooms(roomDtos);
        // roomTypes remain summaries; FE groups rooms by roomTypeId from rooms list
        return dto;
    }

    public PropertyDetailDto recomputeTotalRooms(String propertyId) {
        Property p = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        int total = 0;
        if (p.getListRoomType() != null) {
            for (RoomType rt : p.getListRoomType()) {
                total += (rt.getListRoom() == null ? 0 : rt.getListRoom().size());
            }
        }
        p.setTotalRoom(total);
        Property saved = propertyRepository.save(p);
        return PropertyMapper.toDetailDto(saved);
    }

    public List<OwnerSummaryDto> getOwners() {
        // Distinct owners from current (non-deleted) properties
        var props = propertyRepository.findByDeletedAtIsNull();
        Map<String, String> map = new LinkedHashMap<>();
        for (Property pr : props) {
            if (pr.getOwnerId() != null && pr.getOwnerName() != null) {
                map.putIfAbsent(pr.getOwnerId().toString(), pr.getOwnerName());
            }
        }
        List<OwnerSummaryDto> list = new ArrayList<>();
        for (var e : map.entrySet()) {
            list.add(new OwnerSummaryDto(e.getKey(), e.getValue()));
        }
        return list;
    }

    @Transactional
    public PropertyDetailDto createProperty(PropertyCreateRequest request) {

        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");
        if (!ProvinceUtil.isValidCode(request.getProvince())) {
            throw new IllegalArgumentException("Invalid province code: " + request.getProvince());
        }
        // Validate owner UUID ↔ name consistency: owner must exist and name should match
        UUID ownerUuid = UUID.fromString(request.getOwnerId());
        Optional<AccommodationOwner> ownerOpt = accommodationOwnerRepository.findById(ownerUuid);
        if (ownerOpt.isEmpty()) {
            throw new IllegalArgumentException("Owner not found: " + ownerUuid);
        } else {
            AccommodationOwner owner = ownerOpt.get();
            if (owner.getName() != null && request.getOwnerName() != null
                    && !owner.getName().equals(request.getOwnerName())) {
                throw new IllegalArgumentException("Owner UUID/name mismatch for UUID: " + ownerUuid);
            }
        }
        // Validate presence of at least one room type
        List<RoomTypeCreateRequest> rtReqs = request.getRoomTypes();
        if (rtReqs == null || rtReqs.isEmpty()) {
            throw new IllegalArgumentException("At least one room type is required when creating a property");
        }
        // Rule: do NOT allow duplicate room type names on the same floor within a
        // property
        Map<Integer, Set<String>> typeNamesPerFloor = new HashMap<>();
        for (RoomTypeCreateRequest rtr : rtReqs) {
            int floor = rtr.getFloor() != null ? rtr.getFloor() : 0;
            String nameKey = (rtr.getName() == null ? "" : rtr.getName().trim().toLowerCase());
            Set<String> names = typeNamesPerFloor.computeIfAbsent(floor, k -> new HashSet<>());
            if (names.contains(nameKey)) {
                throw new IllegalArgumentException(
                        "Duplicate room type '" + rtr.getName() + "' on floor " + floor + " is not allowed");
            }
            names.add(nameKey);
        }
        // If multiple room types exist, rooms without roomTypeId cannot be
        // auto-assigned
        // note: we'll enforce the multiple room types condition when assigning rooms

        // Map and assemble the aggregate
        Property entity = PropertyMapper.fromCreateRequest(request);

        // Generate Property ID with retry-safe allocator to reduce race conditions
        String propertyId = allocatePropertyId(request.getType(), ownerUuid);
        entity.setPropertyId(propertyId);

        // We'll keep per-floor unit index to avoid duplicate room numbers across
        // different room types on same floor
        Map<Integer, Integer> nextUnitByFloor = new HashMap<>();

        int totalRooms = 0;

        for (RoomTypeCreateRequest rtr : rtReqs) {
            int floor = rtr.getFloor() != null ? rtr.getFloor() : 0;
            if (floor > 9) {
                throw new IllegalArgumentException("Floor number cannot exceed 9");
            }
            String generatedRtId = IdUtil.generateRoomTypeId(propertyId, rtr.getName(), floor);

            // If client provided roomTypeId, it must match the generated one; otherwise
            // reject
            if (rtr.getRoomTypeId() != null && !rtr.getRoomTypeId().isBlank()
                    && !rtr.getRoomTypeId().equals(generatedRtId)) {
                throw new IllegalArgumentException(
                        "roomTypeId mismatch for type '" + rtr.getName() + "' on floor " + floor +
                                ": expected '" + generatedRtId + "' but got '" + rtr.getRoomTypeId() + "'");
            }

            RoomType rt = new RoomType();
            rt.setRoomTypeId(generatedRtId);
            rt.setName(rtr.getName());
            rt.setPrice(rtr.getPrice());
            rt.setDescription(rtr.getDescription());
            rt.setCapacity(rtr.getCapacity());
            rt.setFacility(rtr.getFacility());
            rt.setFloor(floor);
            rt.setProperty(entity);

            // Allocate rooms under this type (nested request)
            List<RoomCreateRequest> roomsReq = rtr.getRooms();
            if (roomsReq == null || roomsReq.isEmpty()) {
                throw new IllegalArgumentException("RoomType '" + rtr.getName() + "' must include at least one room");
            }

            for (RoomCreateRequest rr : roomsReq) {
                int fl = floor;
                int nextUnitIndex = nextUnitByFloor.getOrDefault(fl, 0) + 1; // start from 1 per floor
                if (nextUnitIndex > 99) {
                    throw new IllegalArgumentException("Room number per floor cannot exceed 99");
                }
                String expectedRoomId = IdUtil.generateRoomId(propertyId, fl, nextUnitIndex);

                if (rr.getRoomId() != null && !rr.getRoomId().isBlank()
                        && !rr.getRoomId().equals(expectedRoomId)) {
                    throw new IllegalArgumentException(
                            "roomId mismatch for room on floor " + fl +
                                    ": expected '" + expectedRoomId + "' but got '" + rr.getRoomId() + "'");
                }

                int unitIndex = nextUnitByFloor.merge(fl, 1, Integer::sum);
                String roomId = IdUtil.generateRoomId(propertyId, fl, unitIndex);

                Room room = new Room();
                room.setRoomId(roomId);
                String roomNumberStr = roomId.substring(roomId.lastIndexOf('-') + 1);
                room.setName(roomNumberStr);
                room.setAvailabilityStatus(rr.getAvailabilityStatus());
                room.setActiveRoom(rr.getActiveRoom());
                room.setMaintenanceStart(parseDate(rr.getMaintenanceStart()));
                room.setMaintenanceEnd(parseDate(rr.getMaintenanceEnd()));
                room.setRoomType(rt);

                rt.addRoom(room);
                totalRooms++;
            }

            entity.addRoomType(rt);
        }

        // Final validations
        if (entity.getListRoomType() == null || entity.getListRoomType().isEmpty()) {
            throw new IllegalArgumentException("Property must have at least one room type");
        }
        if (totalRooms == 0) {
            throw new IllegalArgumentException("Property must have at least one room");
        }
        // Ensure each room type has at least one room (already enforced per type above)

        // Ensure totalRoom matches actual allocated rooms
        entity.setTotalRoom(totalRooms);

        // Let Hibernate timestamps do their job, but also set explicitly for clarity
        // in case the provider is configured without auditing.
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        if (entity.getUpdatedDate() == null) {
            entity.setUpdatedDate(entity.getCreatedDate());
        }

        Property saved = propertyRepository.save(entity);
        return PropertyMapper.toDetailDto(saved);
    }

    private static LocalDateTime parseDate(String s) {
        if (s == null || s.isBlank())
            return null;
        return LocalDateTime.parse(s);
    }

    // private static int parseRoomNumber(String roomId) {
    //     if (roomId == null)
    //         return -1;
    //     try {
    //         String numStr = roomId.substring(roomId.lastIndexOf('-') + 1);
    //         return Integer.parseInt(numStr);
    //     } catch (NumberFormatException e) {
    //         return -1;
    //     } catch (Exception ignore) {
    //         throw new UnknownError("Failed to parse room number from roomId: " + roomId);
    //     }
    // }

    public PropertyDetailDto updateProperty(String propertyId, PropertyUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Property existing = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        // Validate province code
        if (request.getProvince() != null && !ProvinceUtil.isValidCode(request.getProvince())) {
            throw new IllegalArgumentException("Invalid province code: " + request.getProvince());
        }

        // Province provided: only allowed if matches existing; if existing is null,
        // ignore the provided value
        if (request.getProvince() != null) {
            if (existing.getProvince() != null && !request.getProvince().equals(existing.getProvince())) {
                throw new IllegalArgumentException("Cannot change province code: " + request.getProvince());
            }
        }

        // Validate owner UUID ↔ name consistency on update as well (but do not mutate
        // owner fields)
        if (request.getOwnerId() != null) {
            UUID ownerUuid = UUID.fromString(request.getOwnerId());
            Optional<AccommodationOwner> ownerOpt = accommodationOwnerRepository.findById(ownerUuid);
            if (ownerOpt.isEmpty()) {
                throw new IllegalArgumentException("Owner not found: " + ownerUuid);
            } else {
                AccommodationOwner owner = ownerOpt.get();
                if (owner.getName() != null && request.getOwnerName() != null
                        && !owner.getName().equals(request.getOwnerName())) {
                    throw new IllegalArgumentException("Owner UUID/name mismatch for UUID: " + ownerUuid);
                }
                // We do not want to change the owner of an existing property
                if (!existing.getOwnerId().equals(ownerUuid)) {
                    throw new IllegalArgumentException("Cannot change property owner");
                }
            }
        }

        // Update allowed Property fields only
        PropertyMapper.updateEntity(existing, request);

        // If there are room type updates, apply them
        if (request.getRoomTypes() != null && !request.getRoomTypes().isEmpty()) {
            // Build a map of existing RoomTypes by id for quick lookup and also compute
            // per-floor next unit index
            Map<String, RoomType> byId = new HashMap<>();
            Map<Integer, Integer> nextUnitByFloor = new HashMap<>();
            for (RoomType rt : existing.getListRoomType()) {
                byId.put(rt.getRoomTypeId(), rt);
                if (rt.getListRoom() != null) {
                    for (Room r : rt.getListRoom()) {
                        String id = r.getRoomId();
                        if (id == null)
                            continue;
                        try {
                            String numStr = id.substring(id.lastIndexOf('-') + 1);
                            int num = Integer.parseInt(numStr);
                            int f = num / 100;
                            int idx = num - f * 100;
                            nextUnitByFloor.merge(f, idx, Math::max);
                        } catch (Exception ignore) {
                        }
                    }
                }
            }

            // Apply updates per RoomType
            for (var rtReq : request.getRoomTypes()) {
                if (rtReq.getRoomTypeId() == null || rtReq.getRoomTypeId().isBlank()) {
                    throw new IllegalArgumentException("roomTypeId is required for update");
                }
                RoomType rt = byId.get(rtReq.getRoomTypeId());
                if (rt == null) {
                    throw new IllegalArgumentException("RoomType not found on this property: " + rtReq.getRoomTypeId());
                }
                // Enforce ID immutability: propertyId doesn't change, roomTypeId doesn't change
                // Update allowed fields: price, description, facility
                Integer previousPrice = rt.getPrice();
                if (rtReq.getPrice() != null)
                    rt.setPrice(rtReq.getPrice());
                if (rtReq.getDescription() != null)
                    rt.setDescription(rtReq.getDescription());
                if (rtReq.getFacility() != null)
                    rt.setFacility(rtReq.getFacility());

                // If price changed, reflect adjustments on related bookings without changing totalPrice for status 0.
                Integer newPrice = rt.getPrice();
                if (previousPrice != null && newPrice != null && !previousPrice.equals(newPrice)) {
                    int priceDeltaPerNight = newPrice - previousPrice; // integer math
                    if (rt.getListRoom() != null) {
                        for (Room r : rt.getListRoom()) {
                            if (r.getBookings() == null) continue;
                            for (AccommodationBooking b : r.getBookings()) {
                                int st = b.getStatus() == null ? 0 : b.getStatus();
                                if (!(st == 0 || st == 1)) continue; // consider only waiting/paid
                                int days = b.getTotalDays() == null
                                        ? DateUtil.computeDays(b.getCheckInDate(), b.getCheckOutDate())
                                        : b.getTotalDays();
                                int delta = days * priceDeltaPerNight; // positive -> extraPay, negative -> refund

                                if (st == 0) {
                                    if (delta > 0) { b.setExtraPay(delta); b.setRefund(0); }
                                    else if (delta < 0) { b.setRefund(-delta); b.setExtraPay(0); }
                                    else { b.setExtraPay(0); b.setRefund(0); }
                                    // keep status 0 and totalPrice unchanged
                                } else if (st == 1) {
                                    if (delta > 0) {
                                        // need extra payment; revert to waiting
                                        b.setExtraPay(delta); b.setRefund(0); b.setStatus(0);
                                    } else if (delta < 0) {
                                        // refund scenario; go to request-refund
                                        b.setRefund(-delta); b.setExtraPay(0); b.setStatus(3);
                                    } else {
                                        b.setExtraPay(0); b.setRefund(0);
                                    }
                                    // do not mutate totalPrice here; apply at payment/refund processing time
                                }
                                bookingRepository.save(b);
                            }
                        }
                    }
                }

                // Capacity here means 'per-room capacity', not the number of rooms; do not change units
                if (rtReq.getCapacity() != null) {
                    int cap = rtReq.getCapacity();
                    if (cap < 1) throw new IllegalArgumentException("capacity must be >= 1");
                    rt.setCapacity(cap);
                }
            }

            // Recompute total rooms across the property
            int total = 0;
            for (RoomType rt : existing.getListRoomType()) {
                total += (rt.getListRoom() == null ? 0 : rt.getListRoom().size());
            }
            existing.setTotalRoom(total);
        }

        // Touch the updated timestamp explicitly (Hibernate @UpdateTimestamp will also
        // set it)
        existing.setUpdatedDate(LocalDateTime.now());
        Property saved = propertyRepository.save(existing);
        return PropertyMapper.toDetailDto(saved);
    }

    public PropertyDetailDto updatePropertyRooms(String propertyId, RoomTypeCreateRequest req) {
        Property existing = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        int floor = req.getFloor() != null ? req.getFloor() : 0;
        if (floor > 9) {
            throw new IllegalArgumentException("Floor number cannot exceed 9");
        }
        List<RoomCreateRequest> roomsReq = req.getRooms();
        if (roomsReq == null || roomsReq.isEmpty()) {
            throw new IllegalArgumentException("Rooms list cannot be empty for update");
        }

        // Find existing room type by name and floor; if not found, create it (ensuring
        // no duplicate name on same floor)
        RoomType targetRt = null;
        for (RoomType rt : existing.getListRoomType()) {
            int f = rt.getFloor() != null ? rt.getFloor() : 0;
            if (f == floor && rt.getName().equals(req.getName())) {
                targetRt = rt;
                break;
            }
        }
        if (targetRt == null) {
            boolean duplicate = existing.getListRoomType().stream()
                    .anyMatch(rt -> (rt.getFloor() != null ? rt.getFloor() : 0) == floor
                            && rt.getName().equalsIgnoreCase(req.getName()));
            if (duplicate) {
                throw new IllegalArgumentException(
                        "Duplicate room type '" + req.getName() + "' on floor " + floor + " is not allowed");
            }
            String generatedRtId = IdUtil.generateRoomTypeId(propertyId, req.getName(), floor);
            if (req.getRoomTypeId() != null && !req.getRoomTypeId().isBlank()
                    && !req.getRoomTypeId().equals(generatedRtId)) {
                throw new IllegalArgumentException(
                        "roomTypeId mismatch: expected '" + generatedRtId + "' but got '" + req.getRoomTypeId() + "'");
            }
            targetRt = new RoomType();
            targetRt.setRoomTypeId(generatedRtId);
            targetRt.setName(req.getName());
            targetRt.setPrice(req.getPrice());
            targetRt.setDescription(req.getDescription());
            targetRt.setCapacity(req.getCapacity());
            targetRt.setFacility(req.getFacility());
            targetRt.setFloor(floor);
            targetRt.setProperty(existing);
            existing.addRoomType(targetRt);
        }

        // Build per-floor next index from existing rooms across the property
        Map<Integer, Integer> nextUnitByFloor = new HashMap<>();
        for (RoomType rt : existing.getListRoomType()) {
            if (rt.getListRoom() == null)
                continue;
            for (Room r : rt.getListRoom()) {
                String id = r.getRoomId();
                if (id == null)
                    continue;
                try {
                    String numStr = id.substring(id.lastIndexOf('-') + 1);
                    int num = Integer.parseInt(numStr);
                    int f = num / 100;
                    int idx = num - f * 100;
                    nextUnitByFloor.merge(f, idx, Math::max);
                } catch (Exception ignore) {
                }
            }
        }

        int added = 0;
        for (RoomCreateRequest rr : roomsReq) {
            int nextIdx = nextUnitByFloor.getOrDefault(floor, 0) + 1;
            if (nextIdx > 99) {
                throw new IllegalArgumentException("Room number per floor cannot exceed 99");
            }
            String expectedRoomId = IdUtil.generateRoomId(propertyId, floor, nextIdx);
            if (rr.getRoomId() != null && !rr.getRoomId().isBlank()
                    && !rr.getRoomId().equals(expectedRoomId)) {
                throw new IllegalArgumentException("roomId mismatch for update: expected '" + expectedRoomId
                        + "' but got '" + rr.getRoomId() + "'");
            }
            String roomId = expectedRoomId;
            nextUnitByFloor.put(floor, nextIdx);

            Room room = new Room();
            room.setRoomId(roomId);
            String roomNumberStr = roomId.substring(roomId.lastIndexOf('-') + 1);
            room.setName(roomNumberStr);
            room.setAvailabilityStatus(rr.getAvailabilityStatus());
            room.setActiveRoom(rr.getActiveRoom());
            room.setMaintenanceStart(parseDate(rr.getMaintenanceStart()));
            room.setMaintenanceEnd(parseDate(rr.getMaintenanceEnd()));
            room.setRoomType(targetRt);

            targetRt.addRoom(room);
            added++;
        }

        existing.setTotalRoom((existing.getTotalRoom() == null ? 0 : existing.getTotalRoom()) + added);
        Property saved = propertyRepository.save(existing);
        return PropertyMapper.toDetailDto(saved);
    }

    public RoomDetailDto addMaintenance(RoomUpdateRequest request) {
        // Validate the request is not null
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Validate and parse request data
        String roomTypeId = request.getRoomTypeId();

        if (roomTypeId == null || roomTypeId.isBlank()) {
            throw new IllegalArgumentException("roomTypeId cannot be blank");
        }

        String roomId = request.getId();

        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId cannot be blank");
        }

        String propertyId = IdUtil.fetchPropertyIdFromRoomId(roomId);

        if (propertyId == null || propertyId.isBlank()) {
            throw new IllegalArgumentException("Invalid roomId format, cannot extract propertyId");
        }

        LocalDateTime maintenanceStart = parseDate(request.getMaintenanceStart());
        LocalDateTime maintenanceEnd = parseDate(request.getMaintenanceEnd());

        if (maintenanceStart == null || maintenanceEnd == null) {
            throw new IllegalArgumentException("Maintenance start and end dates cannot be null");
        } else if (maintenanceEnd.isBefore(maintenanceStart)) {
            throw new IllegalArgumentException("Maintenance end date cannot be before start date");
        }

        // Ensure maintenance dates are not in the past (the same day is allowed but
        // after the current time)
        if (maintenanceStart.isBefore(LocalDateTime.now()) || maintenanceEnd.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Maintenance start and end date cannot be in the past");
        }

        // Find the property and room
        Property property = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        RoomType roomType = property.getListRoomType().stream()
                .filter(rt -> rt.getRoomTypeId().equals(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("RoomType not found: " + roomTypeId));
        Room room = roomType.getListRoom().stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // Guard: maintenance must not collide with existing bookings on this room (statuses other than canceled)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                Integer st = b.getStatus();
                if (st != null && st == 2) continue; // ignore canceled
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(b.getCheckInDate(), b.getCheckOutDate(), maintenanceStart, maintenanceEnd)) {
                        throw new IllegalArgumentException("Maintenance window overlaps an existing booking for this room");
                    }
                }
            }
        }

        // Update room maintenance schedule
        room.setMaintenanceStart(maintenanceStart);
        room.setMaintenanceEnd(maintenanceEnd);
        Room savedRoom = roomRepository.save(room);

        return RoomMapper.toDetailDto(savedRoom);
    }

    public RoomTypeDetailDto getRoomTypeById(String propertyId, String id) {
        // 1. Handle decoding ID terlebih dahulu
        String finalId = id; // Default gunakan nilai asli
        try {
            // Coba decode. Jika berhasil, finalId akan terupdate.
            finalId = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            // Jika gagal decode, biarkan finalId tetap menggunakan nilai 'id' asli (silent fail)
        }

        // 2. Cari Property
        Property property = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        // 3. Cari RoomType dari list yang ada di dalam Property tersebut
        // Kita perlu variable effective final untuk lambda, jadi gunakan variable baru jika perlu, 
        // tapi 'finalId' di sini sudah cukup aman.
        String targetId = finalId; 
        
        RoomType roomType = property.getListRoomType().stream()
                .filter(rt -> rt.getRoomTypeId().equals(targetId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("RoomType not found: " + targetId));

        // 4. Return (Pengecekan .contains() sebelumnya itu redundan/tidak perlu 
        // karena kita baru saja mengambil roomType dari list itu sendiri via stream)
        return RoomTypeMapper.toDetailDto(roomType);
    }

    public void softDeleteProperty(String propertyId) {
        Property existing = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        existing.setDeletedAt(LocalDateTime.now());
        propertyRepository.save(existing);
    }

    /**
     * Allocate a propertyId that minimizes race conditions by checking for existing
     * IDs and retrying.
     * Strategy:
     * - Scan existing property IDs to find the maximum numeric counter suffix (XXX)
     * - Try the next counter; if exists, increment and retry a few times
     */
    private String allocatePropertyId(int type, UUID ownerUuid) {
        // gather current max counter
        int max = propertyRepository.findAll().stream()
                .map(Property::getPropertyId)
                .filter(Objects::nonNull)
                .mapToInt(IdUtil::extractPropertyCounter)
                .max().orElse(0);

        for (int attempt = 1; attempt <= 5; attempt++) {
            int candidate = max + attempt;
            String pid = IdUtil.generatePropertyId(type, ownerUuid, candidate);
            if (!propertyRepository.existsById(pid)) {
                return pid;
            }
        }
        // Fallback: last resort pick a far future counter
        int fallback = max + 100 + new Random().nextInt(900);
        return IdUtil.generatePropertyId(type, ownerUuid, fallback);
    }

    /**
     * Predict the next property numeric sequence (max suffix + 1). For client-side
     * convenience only.
     */
    public int predictNextPropertySequence() {
        int max = propertyRepository.findAll().stream()
                .map(Property::getPropertyId)
                .filter(Objects::nonNull)
                .mapToInt(IdUtil::extractPropertyCounter)
                .max().orElse(0);
        return max + 1;
    }
}