package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restmapper.AccommodationBookingMapper;
import apap.ti._5.accommodation_2306211231_be.util.DateUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.CustomerSummaryDto;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AccommodationBookingRestService {

    private final AccommodationBookingRepository bookingRepository;

    private final RoomRepository roomRepository;

    private final PropertyRepository propertyRepository;

    

    public long count() {
        return bookingRepository.count();
    }

    // Placeholders to be implemented later (entity-based)
    public List<AccommodationBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<AccommodationBooking> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public AccommodationBooking createBooking(AccommodationBooking booking) {
        return bookingRepository.save(booking);
    }

    public AccommodationBooking updateBooking(String bookingId, AccommodationBooking booking) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteBooking(String bookingId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // DTO-based method signatures for controllers (stubs)
    public List<AccommodationBookingDto> getAllBookingsDto() {
        return getAllBookings().stream()
                .map(AccommodationBookingMapper::toDto)
                .toList();
    }

    public AccommodationBookingDto getBookingDtoById(String bookingId) {
        return getBookingById(bookingId)
                .map(AccommodationBookingMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
    }

    public AccommodationBookingDto createBooking(AccommodationBookingCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Create aggregate from request
        AccommodationBooking booking = AccommodationBookingMapper.fromCreateRequest(request);

        // Resolve Room by ID from the request
        String roomId = request.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is required");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        // Optional validation using names when provided
        if (request.getRoomName() != null && !request.getRoomName().isBlank()) {
            if (!request.getRoomName().equals(room.getName())) {
                throw new IllegalArgumentException("roomName does not match the target Room");
            }
        }
        if (request.getRoomTypeName() != null && !request.getRoomTypeName().isBlank()) {
            if (room.getRoomType() == null || !request.getRoomTypeName().equals(room.getRoomType().getName())) {
                throw new IllegalArgumentException("roomTypeName does not match the target RoomType");
            }
        }
        if (request.getPropertyName() != null && !request.getPropertyName().isBlank()) {
            if (room.getRoomType() == null || room.getRoomType().getProperty() == null
                    || !request.getPropertyName().equals(room.getRoomType().getProperty().getPropertyName())) {
                throw new IllegalArgumentException("propertyName does not match the target Property");
            }
        }

        // Time rules: normalize and validate window, compute days
        LocalDateTime requestedIn = booking.getCheckInDate();
        LocalDateTime requestedOut = booking.getCheckOutDate();
        if (requestedIn == null || requestedOut == null) {
            throw new IllegalArgumentException("checkInDate and checkOutDate are required");
        }
        LocalDateTime effectiveIn = DateUtil.normalizeCheckIn(requestedIn);
        LocalDateTime effectiveOut = DateUtil.normalizeCheckOut(requestedOut);
        if (!effectiveOut.isAfter(effectiveIn)) {
            throw new IllegalArgumentException("Invalid stay window: check-out must be after check-in");
        }
        // Check-in not earlier than now (Asia/Jakarta)
        LocalDateTime nowJakartaLocal = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDateTime();
        if (effectiveIn.isBefore(nowJakartaLocal)) {
            throw new IllegalArgumentException("Check-in cannot be earlier than now");
        }
        // Maintenance overlap check
        if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
            if (DateUtil.isOverlapping(effectiveIn, effectiveOut, room.getMaintenanceStart(), room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }

        // Existing bookings overlap check (exclude canceled)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                Integer st = b.getStatus();
                if (st != null && st == 2) continue; // ignore canceled
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn, effectiveOut, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException("Booking window overlaps with an existing booking for this room");
                    }
                }
            }
        }
        // Capacity check against RoomType capacity (party size <= room type capacity)
        if (room.getRoomType() != null && room.getRoomType().getCapacity() != null) {
            if (booking.getCapacity() != null && booking.getCapacity() > room.getRoomType().getCapacity()) {
                throw new IllegalArgumentException("Requested capacity exceeds room type capacity");
            }
        }
        int days = DateUtil.computeDays(requestedIn, requestedOut);
        booking.setTotalDays(days);
        // Pricing: totalPrice = days * (basePrice + breakfastPerDay)
        int basePrice = 0;
        if (room.getRoomType() != null && room.getRoomType().getPrice() != null) {
            basePrice = room.getRoomType().getPrice();
        }
        int breakfastPerDay = Boolean.TRUE.equals(booking.getIsBreakfast()) ? 50_000 : 0;
        int expectedTotal = days * (basePrice + breakfastPerDay);
        // Server-authoritative: override client-sent totalPrice if present
        booking.setTotalPrice(expectedTotal);
        // extraPay reserved for other add-ons; breakfast is included in totalPrice
        booking.setExtraPay(0);

        // Generate/validate bookingId using Asia/Jakarta time and last 7 chars of
        // roomId
        ZonedDateTime nowJakarta = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
        String expectedBookingId = IdUtil.generateBookingId(room.getRoomId(), nowJakarta);
        if (request.getBookingId() != null && !request.getBookingId().isBlank()
                && !request.getBookingId().equals(expectedBookingId)) {
            throw new IllegalArgumentException("bookingId mismatch: expected '" + expectedBookingId + "'");
        }

        booking.setBookingId(expectedBookingId);

        // Set relation and persist
        booking.setRoom(room);
        booking = createBooking(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto createBookingWithRoom(String idRoom, AccommodationBookingCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Firstly, check the idRoom exists & same with in request
        if (!idRoom.equals(request.getRoomId())) {
            throw new IllegalArgumentException("Room ID in path and request body do not match");
        }

        // Create the booking
        AccommodationBooking booking = AccommodationBookingMapper.fromCreateRequest(request);

        // Set relation to Room entity
        Room room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + idRoom));

        // Time rules: normalize and validate window, compute days
        LocalDateTime requestedIn2 = booking.getCheckInDate();
        LocalDateTime requestedOut2 = booking.getCheckOutDate();
        if (requestedIn2 == null || requestedOut2 == null) {
            throw new IllegalArgumentException("checkInDate and checkOutDate are required");
        }
        LocalDateTime effectiveIn2 = DateUtil.normalizeCheckIn(requestedIn2);
        LocalDateTime effectiveOut2 = DateUtil.normalizeCheckOut(requestedOut2);
        if (!effectiveOut2.isAfter(effectiveIn2)) {
            throw new IllegalArgumentException("Invalid stay window: check-out must be after check-in");
        }
        LocalDateTime nowJakartaLocal2 = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDateTime();
        if (effectiveIn2.isBefore(nowJakartaLocal2)) {
            throw new IllegalArgumentException("Check-in cannot be earlier than now");
        }
        if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
            if (DateUtil.isOverlapping(effectiveIn2, effectiveOut2, room.getMaintenanceStart(), room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }
        // Existing bookings overlap check (exclude canceled)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                Integer st = b.getStatus();
                if (st != null && st == 2) continue;
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn2, effectiveOut2, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException("Booking window overlaps with an existing booking for this room");
                    }
                }
            }
        }
        if (room.getRoomType() != null && room.getRoomType().getCapacity() != null) {
            if (booking.getCapacity() != null && booking.getCapacity() > room.getRoomType().getCapacity()) {
                throw new IllegalArgumentException("Requested capacity exceeds room type capacity");
            }
        }
        int days2 = DateUtil.computeDays(requestedIn2, requestedOut2);
        booking.setTotalDays(days2);
        int basePrice2 = 0;
        if (room.getRoomType() != null && room.getRoomType().getPrice() != null) {
            basePrice2 = room.getRoomType().getPrice();
        }
        int breakfastPerDay2 = Boolean.TRUE.equals(booking.getIsBreakfast()) ? 50_000 : 0;
        int expectedTotal2 = days2 * (basePrice2 + breakfastPerDay2);
        // Server-authoritative: override client-sent totalPrice if present
        booking.setTotalPrice(expectedTotal2);
        booking.setExtraPay(0);

        // Generate/validate bookingId using Asia/Jakarta time and last 7 chars of
        // roomId
        ZonedDateTime nowJakarta2 = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
        String expectedBookingId2 = IdUtil.generateBookingId(room.getRoomId(), nowJakarta2);
        if (request.getBookingId() != null && !request.getBookingId().isBlank()
                && !request.getBookingId().equals(expectedBookingId2)) {
            throw new IllegalArgumentException("bookingId mismatch: expected '" + expectedBookingId2 + "'");
        }
        booking.setBookingId(expectedBookingId2);

        booking.setRoom(room);

        // Save booking
        booking = createBooking(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto updateBooking(String bookingId, AccommodationBookingUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId path param is required");
        }
        if (!bookingId.equals(request.getBookingId())) {
            throw new IllegalArgumentException("bookingId in path and body must match");
        }

        AccommodationBooking existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        // Customer identity must not change
        if (existing.getCustomerId() != null && request.getCustomerId() != null) {
            java.util.UUID reqCustId = java.util.UUID.fromString(request.getCustomerId().trim());
            if (!existing.getCustomerId().equals(reqCustId)) {
                throw new IllegalArgumentException("customerId cannot be changed for an existing booking");
            }
        }

        // Resolve room target and validate provided names match
        String roomId = request.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is required");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        if (request.getRoomName() == null || request.getRoomTypeName() == null || request.getPropertyName() == null
                || request.getRoomName().isBlank() || request.getRoomTypeName().isBlank() || request.getPropertyName().isBlank()) {
            throw new IllegalArgumentException("propertyName, roomTypeName, and roomName are required for update");
        }

        if (!request.getRoomName().equals(room.getName())) {
            throw new IllegalArgumentException("roomName does not match the target Room");
        }
        if (room.getRoomType() == null || !request.getRoomTypeName().equals(room.getRoomType().getName())) {
            throw new IllegalArgumentException("roomTypeName does not match the target RoomType");
        }
        if (room.getRoomType().getProperty() == null ||
                !request.getPropertyName().equals(room.getRoomType().getProperty().getPropertyName())) {
            throw new IllegalArgumentException("propertyName does not match the target Property");
        }

        // Apply core field updates
        AccommodationBookingMapper.updateEntity(existing, request);

        // Normalize and validate the booking window
        LocalDateTime requestedIn = existing.getCheckInDate();
        LocalDateTime requestedOut = existing.getCheckOutDate();
        if (requestedIn == null || requestedOut == null) {
            throw new IllegalArgumentException("checkInDate and checkOutDate are required");
        }
        LocalDateTime effectiveIn = DateUtil.normalizeCheckIn(requestedIn);
        LocalDateTime effectiveOut = DateUtil.normalizeCheckOut(requestedOut);
        if (!effectiveOut.isAfter(effectiveIn)) {
            throw new IllegalArgumentException("Invalid stay window: check-out must be after check-in");
        }
        LocalDateTime nowJakartaLocal = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDateTime();
        if (effectiveIn.isBefore(nowJakartaLocal)) {
            throw new IllegalArgumentException("Check-in cannot be earlier than now");
        }

        // Maintenance overlap check
        if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
            if (DateUtil.isOverlapping(effectiveIn, effectiveOut, room.getMaintenanceStart(), room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }

        // Existing bookings overlap check (exclude canceled, ignore self)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                if (bookingId.equals(b.getBookingId())) continue; // skip self
                Integer st = b.getStatus();
                if (st != null && st == 2) continue;
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn, effectiveOut, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException("Booking window overlaps with an existing booking for this room");
                    }
                }
            }
        }

        // Capacity check
        if (room.getRoomType() != null && room.getRoomType().getCapacity() != null) {
            if (existing.getCapacity() != null && existing.getCapacity() > room.getRoomType().getCapacity()) {
                throw new IllegalArgumentException("Requested capacity exceeds room type capacity");
            }
        }

        // Recompute totals and adjust financial side-effects according to status rules
        int days = DateUtil.computeDays(requestedIn, requestedOut);
        existing.setTotalDays(days);
        int basePrice = 0;
        if (room.getRoomType() != null && room.getRoomType().getPrice() != null) {
            basePrice = room.getRoomType().getPrice();
        }
        int breakfastPerDay = Boolean.TRUE.equals(existing.getIsBreakfast()) ? 50_000 : 0;
        int expectedTotal = days * (basePrice + breakfastPerDay);

        Integer prevStatus = existing.getStatus() == null ? 0 : existing.getStatus();
        int previousTotal = existing.getTotalPrice() == null ? 0 : existing.getTotalPrice();

        // Apply new total price
        existing.setTotalPrice(expectedTotal);

        // Status transitions per spec when booking was already paid
        if (prevStatus == 1) {
            if (expectedTotal > previousTotal) {
                // price increased -> extraPay and revert to waiting (0)
                existing.setExtraPay(expectedTotal - previousTotal);
                existing.setStatus(0);
                // profit unchanged until extraPay settled
            } else if (expectedTotal < previousTotal) {
                // price decreased -> refund and switch to request refund (3)
                existing.setRefund(previousTotal - expectedTotal);
                existing.setStatus(3);
                // profit unchanged until refund processed/done
            } else {
                // unchanged price -> keep status 1
                existing.setStatus(1);
                existing.setExtraPay(0);
                // keep existing refund as-is (should be 0)
            }
        } else if (prevStatus == 0) {
            // still waiting; just reset extraPay unless explicitly set
            // If client toggled breakfast or dates making total change, profit still unaffected
            if (existing.getExtraPay() == null) existing.setExtraPay(0);
            existing.setStatus(0);
        } else if (prevStatus == 3) {
            // In refund state; recompute refund relative to previous paid amount
            if (expectedTotal < previousTotal) {
                existing.setRefund(previousTotal - expectedTotal);
            }
            existing.setStatus(3);
            existing.setExtraPay(0);
        }

        // Assign room
        existing.setRoom(room);

        // Persist
        existing = bookingRepository.save(existing);
        return AccommodationBookingMapper.toDto(existing);
    }

    public AccommodationBookingDto markBookingAsPaid(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() != null && booking.getStatus() != 0) {
            throw new IllegalStateException("Only bookings with status 0 (waiting for payment) can be paid");
        }

        // Update property profit: add totalPrice and any extraPay, then reset extraPay to 0
        Room room = booking.getRoom();
        if (room == null || room.getRoomType() == null || room.getRoomType().getProperty() == null) {
            throw new IllegalStateException("Booking is not associated with a property");
        }
        Property property = room.getRoomType().getProperty();
        int delta = (booking.getTotalPrice() != null ? booking.getTotalPrice() : 0)
                + (booking.getExtraPay() != null ? booking.getExtraPay() : 0);
        property.setProfit((property.getProfit() != null ? property.getProfit() : 0) + delta);
        booking.setExtraPay(0);
        booking.setStatus(1); // payment confirmed

        // Persist both entities
        propertyRepository.save(property);
        booking = bookingRepository.save(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        int status = booking.getStatus() != null ? booking.getStatus() : 0;
        if (status != 0 && status != 1 && status != 3) {
            throw new IllegalStateException("Only bookings with status 0, 1, or 3 can be canceled");
        }

        Room room = booking.getRoom();
        if (room == null || room.getRoomType() == null || room.getRoomType().getProperty() == null) {
            throw new IllegalStateException("Booking is not associated with a property");
        }
        var property = room.getRoomType().getProperty();
        int profit = property.getProfit() != null ? property.getProfit() : 0;
        int totalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0;
        int extraPay = booking.getExtraPay() != null ? booking.getExtraPay() : 0;
        int refund = booking.getRefund() != null ? booking.getRefund() : 0;

        if (status == 0) {
            // default: only change status to canceled
            // special case from spec: if extraPay exists, profit = profit - totalPrice + extraPay
            if (extraPay > 0) {
                profit = profit - totalPrice + extraPay;
            }
        } else if (status == 1) {
            // paid booking canceled -> subtract current total price
            profit = profit - totalPrice;
        } else if (status == 3) {
            // refund state canceled -> subtract total price and refund
            profit = profit - totalPrice - refund;
        }

        property.setProfit(Math.max(0, profit));
        booking.setStatus(2); // canceled

        propertyRepository.save(property);
        booking = bookingRepository.save(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto refundBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == null || booking.getStatus() != 1) {
            throw new IllegalStateException("Refund can only be processed for bookings with status 1 (paid)");
        }
        int refund = booking.getRefund() != null ? booking.getRefund() : 0;
        if (refund <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than 0");
        }

        Room room = booking.getRoom();
        if (room == null || room.getRoomType() == null || room.getRoomType().getProperty() == null) {
            throw new IllegalStateException("Booking is not associated with a property");
        }
        var property = room.getRoomType().getProperty();
        int profit = property.getProfit() != null ? property.getProfit() : 0;
        profit = profit - refund;
        property.setProfit(Math.max(0, profit));

        booking.setStatus(3); // refund requested/completed

        propertyRepository.save(property);
        booking = bookingRepository.save(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public Map<String, Object> getBookingChart(Integer month, Integer year) {
        // For now, profit is cumulative on Property; we expose current profit per property.
        // month/year are accepted for future use and echoed back to the client.
        var properties = propertyRepository.findByDeletedAtIsNull();

        List<String> labels = properties.stream()
                .map(p -> p.getPropertyName())
                .toList();
        List<Integer> data = properties.stream()
                .map(p -> p.getProfit() == null ? 0 : p.getProfit())
                .toList();

        // items array with property object essentials for FE that prefers full objects on x-axis
        List<Map<String, Object>> items = properties.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("propertyId", p.getPropertyId());
            m.put("propertyName", p.getPropertyName());
            m.put("type", p.getType());
            m.put("profit", p.getProfit() == null ? 0 : p.getProfit());
            return m;
        }).toList();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("month", month);
        result.put("year", year);
        result.put("labels", labels);
        result.put("data", data);
        result.put("items", items);
        return result;
    }

    /**
     * Process bookings whose check-in is today (Asia/Jakarta) and update statuses per spec:
     * - status 1 (paid) -> 4 (done), profit unchanged
     * - status 0 with extraPay>0 -> auto-cancel, profit reduced by amount already paid (handled by cancelBooking)
     * - status 3 (refund requested) -> mark done and reduce profit by refund
     * Returns number of affected bookings.
     */
    public int processCheckInToday() {
        LocalDateTime todayStart = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDate().atStartOfDay();
        LocalDateTime todayCheckInAnchor = DateUtil.normalizeCheckIn(todayStart);
        // We'll consider bookings whose normalized check-in date matches today's check-in anchor date
        var all = bookingRepository.findAll();
        int changed = 0;
        for (AccommodationBooking b : all) {
            if (b.getCheckInDate() == null) continue;
            LocalDateTime normIn = DateUtil.normalizeCheckIn(b.getCheckInDate());
            if (normIn.toLocalDate().isEqual(todayCheckInAnchor.toLocalDate())) {
                int st = b.getStatus() == null ? 0 : b.getStatus();
                if (st == 1) {
                    b.setStatus(4); // done
                    bookingRepository.save(b);
                    changed++;
                } else if (st == 0 && (b.getExtraPay() != null && b.getExtraPay() > 0)) {
                    // auto-cancel unpaid extra pay cases using existing cancellation logic
                    cancelBooking(b.getBookingId());
                    changed++;
                } else if (st == 3) {
                    // reduce profit by refund and mark done
                    Room room = b.getRoom();
                    if (room != null && room.getRoomType() != null && room.getRoomType().getProperty() != null) {
                        Property prop = room.getRoomType().getProperty();
                        int profit = prop.getProfit() == null ? 0 : prop.getProfit();
                        int refund = b.getRefund() == null ? 0 : b.getRefund();
                        prop.setProfit(Math.max(0, profit - refund));
                        propertyRepository.save(prop);
                    }
                    b.setStatus(4);
                    bookingRepository.save(b);
                    changed++;
                }
            }
        }
        return changed;
    }

    public java.util.List<CustomerSummaryDto> getCustomers() {
        var bookings = bookingRepository.findAll();
        java.util.Map<String, CustomerSummaryDto> map = new java.util.LinkedHashMap<>();
        for (AccommodationBooking b : bookings) {
            if (b.getCustomerId() != null) {
                String id = b.getCustomerId().toString();
                if (!map.containsKey(id)) {
                    map.put(id, new CustomerSummaryDto(id, b.getCustomerName(), b.getCustomerEmail(), b.getCustomerPhone()));
                }
            }
        }
        return new java.util.ArrayList<>(map.values());
    }
}
