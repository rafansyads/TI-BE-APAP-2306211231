package apap.ti._5.accommodation_2306211231_be.restservice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSummaryResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restmapper.AccommodationBookingMapper;
import apap.ti._5.accommodation_2306211231_be.util.DateUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AccommodationBookingRestService {

    private final AccommodationBookingRepository bookingRepository;

    private final RoomRepository roomRepository;

    private final PropertyRepository propertyRepository;

    private final CustomerRestService customerService;

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

    // public AccommodationBooking updateBooking(String bookingId,
    // AccommodationBooking booking) {
    // throw new UnsupportedOperationException("Not implemented yet");
    // }

    // public void deleteBooking(String bookingId) {
    // throw new UnsupportedOperationException("Not implemented yet");
    // }

    // DTO-based method signatures for controllers
    public List<AccommodationBookingDto> getAllBookingsDto() {
        return getAllBookings().stream()
                .map(AccommodationBookingMapper::toDto)
                .toList();
    }

    public AccommodationBookingDto getBookingDtoById(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking b = findBookingOrThrow(bookingId);
        return AccommodationBookingMapper.toDto(b);
    }

    public AccommodationBookingDto createBooking(AccommodationBookingCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Find customer, if not found then do not proceed
        UUID custID = customerService.findById(UUID.fromString(request.getCustomerId().trim()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId()))
                .getId();

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
            if (DateUtil.isOverlapping(effectiveIn, effectiveOut, room.getMaintenanceStart(),
                    room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }

        // Existing bookings overlap check (exclude canceled)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                Integer st = b.getStatus();
                if (st != null && st == 2)
                    continue; // ignore canceled
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn, effectiveOut, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException(
                                "Booking window overlaps with an existing booking for this room");
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

        // Add booking to room
        addBookingToRoom(booking, room);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto createBookingWithRoom(String idRoom, AccommodationBookingCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Find customer, if not found then do not proceed
        UUID custID = customerService.findById(UUID.fromString(request.getCustomerId().trim()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId()))
                .getId();

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
            if (DateUtil.isOverlapping(effectiveIn2, effectiveOut2, room.getMaintenanceStart(),
                    room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }
        // Existing bookings overlap check (exclude canceled)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                Integer st = b.getStatus();
                if (st != null && st == 2)
                    continue;
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn2, effectiveOut2, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException(
                                "Booking window overlaps with an existing booking for this room");
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

        // Add booking to Room
        addBookingToRoom(booking, room);
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

        // Enforce rule: Updates are only allowed for status 0 or 1 when there is no
        // pending extraPay/refund
        int currStatus = existing.getStatus() == null ? 0 : existing.getStatus();
        int extra = existing.getExtraPay() == null ? 0 : existing.getExtraPay();
        int refund = existing.getRefund() == null ? 0 : existing.getRefund();
        if ((currStatus == 0 || currStatus == 1) && (extra != 0 || refund != 0)) {
            throw new IllegalStateException("Booking cannot be updated while there is pending extra payment or refund");
        }
        if (currStatus == 2 || currStatus == 4) {
            throw new IllegalStateException("Booking with status 2 or 4 cannot be updated");
        }
        if (currStatus == 3) {
            throw new IllegalStateException("Booking in refund state (3) cannot be updated");
        }

        // Customer identity must not change
        if (existing.getCustomerId() != null && request.getCustomerId() != null) {
            UUID reqCustId = UUID.fromString(request.getCustomerId().trim());
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
                || request.getRoomName().isBlank() || request.getRoomTypeName().isBlank()
                || request.getPropertyName().isBlank()) {
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

        // Snapshot previous values for comparison before applying updates
        LocalDateTime prevCheckIn = existing.getCheckInDate();
        LocalDateTime prevCheckOut = existing.getCheckOutDate();
        Boolean prevBreakfast = existing.getIsBreakfast();
        Integer previousTotal = existing.getTotalPrice() == null ? 0 : existing.getTotalPrice();

        // Apply core field updates from request
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
            if (DateUtil.isOverlapping(effectiveIn, effectiveOut, room.getMaintenanceStart(),
                    room.getMaintenanceEnd())) {
                throw new IllegalArgumentException("Booking window overlaps with room maintenance schedule");
            }
        }

        // Existing bookings overlap check (exclude canceled, ignore self)
        if (room.getBookings() != null) {
            for (AccommodationBooking b : room.getBookings()) {
                if (bookingId.equals(b.getBookingId()))
                    continue; // skip self
                Integer st = b.getStatus();
                if (st != null && st == 2)
                    continue;
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    if (DateUtil.isOverlapping(effectiveIn, effectiveOut, b.getCheckInDate(), b.getCheckOutDate())) {
                        throw new IllegalArgumentException(
                                "Booking window overlaps with an existing booking for this room");
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

        Integer prevStatus = currStatus;

        // Determine if the change is only due to RoomType.price (dates/breakfast
        // unchanged)
        boolean datesUnchanged = (prevCheckIn != null && prevCheckOut != null
                && prevCheckIn.equals(existing.getCheckInDate())
                && prevCheckOut.equals(existing.getCheckOutDate()));
        boolean breakfastUnchanged = Objects.equals(prevBreakfast, existing.getIsBreakfast());

        // Apply per-status rules
        // New rule: do not change totalPrice on update; keep as-is until next
        // payment/refund action.
        existing.setTotalPrice(previousTotal);

        int delta = expectedTotal - previousTotal;
        if (prevStatus == 1) {
            if (!(datesUnchanged && breakfastUnchanged)) {
                if (delta > 0) {
                    // Longer stay (more expensive): require extraPay, revert to waiting, and remove
                    // previously recognized income
                    existing.setExtraPay(delta);
                    existing.setRefund(0);
                    existing.setStatus(0);
                    // Reset property's profit by removing previous recognition
                    if (room.getRoomType() != null && room.getRoomType().getProperty() != null) {
                        Property prop = room.getRoomType().getProperty();
                        int profit = prop.getProfit() == null ? 0 : prop.getProfit();
                        prop.setProfit(Math.max(0, profit - previousTotal));
                        propertyRepository.save(prop);
                    }
                } else if (delta < 0) {
                    // Shorter stay (cheaper): set refund and go to refund-requested; do not change
                    // income yet
                    existing.setRefund(-delta);
                    existing.setExtraPay(0);
                    existing.setStatus(3);
                } else {
                    // No change in baseline
                    existing.setExtraPay(0);
                    existing.setRefund(0);
                    existing.setStatus(1);
                }
            }
        } else if (prevStatus == 0) {
            // Waiting: set deltas relative to prior baseline
            if (delta > 0) {
                existing.setExtraPay(delta);
                existing.setRefund(0);
            } else if (delta < 0) {
                existing.setRefund(-delta);
                existing.setExtraPay(0);
            } else {
                existing.setExtraPay(0);
                existing.setRefund(0);
            }
            existing.setStatus(0);
        }

        // Assign room
        existing.setRoom(room);

        // Persist
        existing = bookingRepository.save(existing);

        // The existing booking in the Room repository should only change
        // the attribute inside of it, so the Room does not need to remove and re-add
        // the accommodation booking.
        return AccommodationBookingMapper.toDto(existing);
    }

    public AccommodationBookingDto markBookingAsPaid(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = findBookingOrThrow(bookingId);

        if (booking.getStatus() != null && booking.getStatus() != 0) {
            throw new IllegalStateException("Only bookings with status 0 (waiting for payment) can be paid");
        }

        // Apply stored adjustments (extraPay/refund) at payment time to the original
        // total
        Room room = booking.getRoom();
        if (room == null || room.getRoomType() == null || room.getRoomType().getProperty() == null) {
            throw new IllegalStateException("Booking is not associated with a property");
        }
        int oldTotal = booking.getTotalPrice() == null ? 0 : booking.getTotalPrice();
        int extraPay = booking.getExtraPay() == null ? 0 : booking.getExtraPay();
        int refund = booking.getRefund() == null ? 0 : booking.getRefund();
        // At payment, refund reduces the amount to be paid
        int payTotal = oldTotal + extraPay - refund;
        if (payTotal < 0)
            payTotal = 0;
        booking.setTotalPrice(payTotal);

        // Add profit equal to the new total price
        Property property = room.getRoomType().getProperty();
        int profit = property.getProfit() == null ? 0 : property.getProfit();
        property.setProfit(profit + payTotal);
        propertyRepository.save(property);

        // Debit customer saldo when payment is applied (if linked to real Customer)
        if (booking.getCustomerId() != null) {
            try {
                var maybeCust = customerService.findById(booking.getCustomerId());
                if (maybeCust.isPresent()) {
                    var cust = maybeCust.get();
                    long currSaldo = cust.getSaldo() == null ? 0L : cust.getSaldo();
                    // Prevent negative saldo: require sufficient saldo to cover payment
                    if (currSaldo < (long) payTotal) {
                        throw new ResponseStatusException(
                                HttpStatus.PAYMENT_REQUIRED,
                                "Insufficient customer saldo to complete payment");
                    }
                    long newSaldo = currSaldo - (long) payTotal;
                    // Clamp to zero as a safety (shouldn't be negative due to check)
                    if (newSaldo < 0)
                        newSaldo = 0L;
                    cust.setSaldo(newSaldo);
                    customerService.update(cust);
                }
            } catch (ResponseStatusException rse) {
                // propagate balance-related response status up
                throw rse;
            } catch (Exception ex) {
                // Do not fail the payment if customer update (non-balance error) fails; log if
                // logging available
            }
        }

        // Reset adjustments after applying
        booking.setExtraPay(0);
        booking.setRefund(0);
        booking.setStatus(1); // payment confirmed
        booking = bookingRepository.save(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = findBookingOrThrow(bookingId);

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

        int refundToCustomer = 0;
        if (status == 0) {
            // No income recognized yet; just cancel
        } else if (status == 1) {
            // Paid -> remove recognized income and refund full paid amount to customer
            profit = profit - totalPrice;
            refundToCustomer = totalPrice;
        } else if (status == 3) {
            // Refund-requested: remove recognized income and refund either refund amount
            // (if set) or full total
            profit = profit - totalPrice;
            refundToCustomer = booking.getRefund() != null && booking.getRefund() > 0 ? booking.getRefund()
                    : totalPrice;
        }

        property.setProfit(Math.max(0, profit));
        booking.setStatus(2); // canceled
        booking.setExtraPay(0);
        booking.setRefund(0);

        propertyRepository.save(property);

        // Credit customer saldo for refunds when applicable
        if (refundToCustomer > 0 && booking.getCustomerId() != null) {
            try {
                var maybeCust = customerService.findById(booking.getCustomerId());
                if (maybeCust.isPresent()) {
                    var cust = maybeCust.get();
                    long currSaldo = cust.getSaldo() == null ? 0L : cust.getSaldo();
                    cust.setSaldo(currSaldo + (long) refundToCustomer);
                    customerService.update(cust);
                }
            } catch (Exception ex) {
                // swallow to avoid failing cancel flow; log if infrastructure available
            }
        }

        booking = bookingRepository.save(booking);
        // Do NOT remove canceled bookings from the Room collection here.
        // Removing a booking from the Room.bookings list with
        // `orphanRemoval = true` causes JPA to delete the booking entity.
        // Keep canceled bookings in the DB (status=2) so records/audit remain.
        return AccommodationBookingMapper.toDto(booking);
    }

    public AccommodationBookingDto refundBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        AccommodationBooking booking = findBookingOrThrow(bookingId);

        // Allow refund action when a refund amount exists (>0). Typically status 3, but
        // tolerate 1 as well.
        Integer st = booking.getStatus();
        if (st == null)
            st = 0;
        if (!(st == 1 || st == 3)) {
            throw new IllegalStateException("Refund can only be processed for bookings with status 1 or 3");
        }
        int refund = booking.getRefund() != null ? booking.getRefund() : 0;
        if (refund <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than 0");
        }
        // Reduce property's profit by refund amount and reduce booking's total; status
        // returns to 1
        Room room = booking.getRoom();
        if (room == null || room.getRoomType() == null || room.getRoomType().getProperty() == null) {
            throw new IllegalStateException("Booking is not associated with a property");
        }
        Property property = room.getRoomType().getProperty();
        int profit = property.getProfit() == null ? 0 : property.getProfit();
        property.setProfit(Math.max(0, profit - refund));
        propertyRepository.save(property);

        // Credit customer with refund amount
        if (refund > 0 && booking.getCustomerId() != null) {
            try {
                var maybeCust = customerService.findById(booking.getCustomerId());
                if (maybeCust.isPresent()) {
                    var cust = maybeCust.get();
                    long currSaldo = cust.getSaldo() == null ? 0L : cust.getSaldo();
                    cust.setSaldo(currSaldo + (long) refund);
                    customerService.update(cust);
                }
            } catch (Exception ex) {
                // Do not fail refund if customer update fails; log if possible
            }
        }

        int newTotal = Math.max(0, (booking.getTotalPrice() == null ? 0 : booking.getTotalPrice()) - refund);
        booking.setTotalPrice(newTotal);
        booking.setRefund(0);
        booking.setStatus(1); // after processing refund, back to paid
        booking = bookingRepository.save(booking);
        return AccommodationBookingMapper.toDto(booking);
    }

    public Map<String, Object> getBookingChart(Integer month, Integer year, UUID ownerId) {
        if (month == null || year == null) {
            throw new IllegalArgumentException("month and year are required");
        }

        // Build per-property monthly income from bookings where check-in month/year
        // match
        var properties = propertyRepository.findByDeletedAtIsNull();
        // If ownerId present, restrict to properties owned by that owner
        if (ownerId != null) {
            var filtered = new ArrayList<Property>();
            for (var p : properties) {
                if (p.getOwnerId() != null && p.getOwnerId().equals(ownerId)) {
                    filtered.add(p);
                }
            }
            properties = filtered;
        }
        Map<String, Integer> incomeByProperty = new LinkedHashMap<>();
        Map<String, Property> propById = new LinkedHashMap<>();
        for (var p : properties) {
            incomeByProperty.put(p.getPropertyId(), 0);
            propById.put(p.getPropertyId(), p);
        }

        var bookings = bookingRepository.findAll();
        for (AccommodationBooking b : bookings) {
            if (b.getRoom() == null || b.getRoom().getRoomType() == null
                    || b.getRoom().getRoomType().getProperty() == null)
                continue;
            var prop = b.getRoom().getRoomType().getProperty();
            // If ownerId filter is present, skip bookings not belonging to the owner's properties
            if (ownerId != null) {
                if (prop.getOwnerId() == null || !prop.getOwnerId().equals(ownerId)) continue;
            }
            var checkIn = b.getCheckInDate();
            if (checkIn == null)
                continue;
            if (checkIn.getYear() != year)
                continue;
            if (checkIn.getMonthValue() != month)
                continue;

            int status = b.getStatus() == null ? 0 : b.getStatus();
            // Recognized income for the month: include paid/done bookings
            // Exclude 0 (waiting), 2 (canceled). For 3 (refund requested), keep previously
            // recognized total for now.
            if (status == 1 || status == 4 || status == 3) {
                int amt = b.getTotalPrice() == null ? 0 : b.getTotalPrice();
                // If status 3 and refund already set, reflect net if desired; otherwise, keep
                // recognized value
                // We'll subtract refund only if it has been applied to totalPrice elsewhere
                // (our service does upon refund)
                incomeByProperty.computeIfPresent(prop.getPropertyId(), (k, v) -> v + amt);
            }
        }

        // Prepare labels/data/items aligned
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Map<String, Object>> items = new ArrayList<>();
        for (var p : properties) {
            String pid = p.getPropertyId();
            int income = incomeByProperty.getOrDefault(pid, 0);
            labels.add(p.getPropertyName());
            data.add(income);
            Map<String, Object> m = new HashMap<>();
            m.put("propertyId", pid);
            m.put("propertyName", p.getPropertyName());
            m.put("type", p.getType());
            m.put("profit", income);
            items.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("year", year);
        result.put("labels", labels);
        result.put("data", data);
        result.put("items", items);
        // Also provide 'values' alias to support FE expecting {labels, values}
        result.put("values", data);
        return result;
    }

    /**
     * Process bookings whose check-in is today (Asia/Jakarta) and update statuses
     * per spec:
     * - status 1 (paid) -> 4 (done), profit unchanged
     * - status 0 with extraPay>0 -> auto-cancel, profit reduced by amount already
     * paid (handled by cancelBooking)
     * - status 3 (refund requested) -> mark done and reduce profit by refund
     * Returns number of affected bookings.
     */
    public int processCheckInToday() {
        LocalDateTime todayStart = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDate().atStartOfDay();
        LocalDateTime todayCheckInAnchor = DateUtil.normalizeCheckIn(todayStart); // typically 14:00 on today
        var all = bookingRepository.findAll();
        int changed = 0;
        for (AccommodationBooking b : all) {
            if (b.getCheckInDate() == null)
                continue;
            LocalDateTime normIn = DateUtil.normalizeCheckIn(b.getCheckInDate());
            if (normIn.toLocalDate().isEqual(todayCheckInAnchor.toLocalDate())) {
                int st = b.getStatus() == null ? 0 : b.getStatus();
                if (st == 1) {
                    b.setStatus(4); // done
                    bookingRepository.save(b);
                    changed++;
                } else if (st == 3) {
                    // auto-refund then mark done
                    Room r = b.getRoom();
                    if (r != null && r.getRoomType() != null && r.getRoomType().getProperty() != null) {
                        Property prop = r.getRoomType().getProperty();
                        int profit = prop.getProfit() == null ? 0 : prop.getProfit();
                        int ref = b.getRefund() == null ? 0 : b.getRefund();
                        prop.setProfit(Math.max(0, profit - ref));
                        propertyRepository.save(prop);
                        int newTotal = Math.max(0, (b.getTotalPrice() == null ? 0 : b.getTotalPrice()) - ref);
                        b.setTotalPrice(newTotal);
                        b.setRefund(0);
                    }
                    b.setStatus(4);
                    bookingRepository.save(b);
                    changed++;
                } else if (st == 0 || st == 2) {
                    // cancel unpaid or already canceled
                    if (st != 2) {
                        b.setStatus(2);
                        bookingRepository.save(b);
                        changed++;
                        // also remove bookings from Room
                        removeBookingFromRoom(b, b.getRoom());
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Process Checkout past due bookings (Asia/Jakarta) and update statuses
     * per spec:
     * Only checkout date before now:
     * - status 4 (done) -> no change
     * - we could checkout before check-in date if the booking is canceled early
     * but as per this moment, the refund is not processed
     * - if after checkout, we still remove the booking from Room,
     * but as per this moment, the extraPay is not processed
     * - there should not be any other status than 4, but if there is any
     * if status (1 / 3) then change to done, else (0 / 2) do nothing
     * Returns number of affected bookings.
     */
    public int processCheckoutPastDue() {
        LocalDateTime nowJakarta = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toLocalDateTime();
        var all = bookingRepository.findAll();
        int changed = 0;
        for (AccommodationBooking b : all) {
            if (b.getCheckOutDate() == null)
                continue;
            if (b.getCheckOutDate().isBefore(nowJakarta)) {
                int st = b.getStatus() == null ? 0 : b.getStatus();
                if (st == 1) {
                    b.setStatus(4); // done
                    bookingRepository.save(b);
                    changed++;
                } else if (st == 3) {
                    // auto-refund then mark done
                    Room r = b.getRoom();
                    if (r != null && r.getRoomType() != null && r.getRoomType().getProperty() != null) {
                        Property prop = r.getRoomType().getProperty();
                        int profit = prop.getProfit() == null ? 0 : prop.getProfit();
                        int ref = b.getRefund() == null ? 0 : b.getRefund();
                        prop.setProfit(Math.max(0, profit - ref));
                        propertyRepository.save(prop);
                        int newTotal = Math.max(0, (b.getTotalPrice() == null ? 0 : b.getTotalPrice()) - ref);
                        b.setTotalPrice(newTotal);
                        b.setRefund(0);
                    }
                    b.setStatus(4);
                    bookingRepository.save(b);
                    changed++;
                }
                // Remove booking from Room
                removeBookingFromRoom(b, b.getRoom());
            } else {
                // Not yet past due, we can process checkout as per spec
                // but check if the check in date is already past now
                // if yes then continue, else do the operations
                if (b.getCheckInDate() == null || b.getCheckInDate().isAfter(nowJakarta)) {
                    continue;
                }
                int st = b.getStatus() == null ? 0 : b.getStatus();
                if (st == 1) {
                    b.setStatus(4); // done
                    bookingRepository.save(b);
                    changed++;
                } else if (st == 3) {
                    // auto-refund then mark done
                    Room r = b.getRoom();
                    if (r != null && r.getRoomType() != null && r.getRoomType().getProperty() != null) {
                        Property prop = r.getRoomType().getProperty();
                        int profit = prop.getProfit() == null ? 0 : prop.getProfit();
                        int ref = b.getRefund() == null ? 0 : b.getRefund();
                        prop.setProfit(Math.max(0, profit - ref));
                        propertyRepository.save(prop);
                        int newTotal = Math.max(0, (b.getTotalPrice() == null ? 0 : b.getTotalPrice()) - ref);
                        b.setTotalPrice(newTotal);
                        b.setRefund(0);
                    }
                    b.setStatus(4);
                    bookingRepository.save(b);
                    changed++;
                }
                // Remove booking from Room
                removeBookingFromRoom(b, b.getRoom());
            }
        }
        return changed;
    }

    /**
     * Add bookings to Room
     */
    public void addBookingToRoom(AccommodationBooking book, Room room) {
        room.addBooking(book);
    }

    /**
     * Add several bookings to Room
     */
    public void addBookingsToRoom(List<AccommodationBooking> bookings, Room room) {
        for (AccommodationBooking b : bookings) {
            room.addBooking(b);
        }
    }

    /**
     * Remove bookings from Room
     * Only invoked when the status is 2 or 4 after checkout Date
     */
    public void removeBookingFromRoom(AccommodationBooking book, Room room) {
        // Only remove bookings that are completed (status == 4) AND have a
        // check-out date strictly before now (Asia/Jakarta). Do NOT remove
        // canceled bookings (status == 2) here because removing them from the
        // Room.bookings collection with `orphanRemoval = true` will delete the
        // booking entity from the database.
        if (book != null && book.getStatus() != null
                && book.getStatus() == 4
                && book.getCheckOutDate() != null
                && book.getCheckOutDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Jakarta")))) {
            room.removeBooking(book);
        }
    }

    /**
     * Remove several bookings from Room
     * Only invoked when the status is 2 or 4 after checkout Date
     */
    public void removeBookingsFromRoom(List<AccommodationBooking> bookings, Room room) {
        for (AccommodationBooking b : bookings) {
            if (b != null && b.getStatus() != null
                    && b.getStatus() == 4
                    && b.getCheckOutDate() != null
                    && b.getCheckOutDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Jakarta")))) {
                room.removeBooking(b);
            }
        }
    }

    /**
     * Robust lookup for booking by id. Tries several normalization strategies
     * (trim, URL-decode, strip quotes) and falls back to a contains()-based
     * scan as a last resort. Throws NoSuchElementException if nothing matches.
     */
    private AccommodationBooking findBookingOrThrow(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        String id = bookingId.trim();

        // 1) direct lookup
        var opt = bookingRepository.findById(id);
        if (opt.isPresent()) return opt.get();

        // 2) try URL decode (in case callers sent an encoded value)
        try {
            String dec = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            if (!dec.equals(id)) {
                opt = bookingRepository.findById(dec);
                if (opt.isPresent()) return opt.get();
            }
        } catch (Exception ignore) {
        }

        // 3) strip surrounding quotes if present
        String stripped = id.replaceAll("^\"|\"$", "");
        if (!stripped.equals(id)) {
            opt = bookingRepository.findById(stripped);
            if (opt.isPresent()) return opt.get();
        }

        // 4) last resort: scan all bookings and match contains (helps with small
        // formatting differences when the unique suffix is present)
        var all = bookingRepository.findAll();
        for (AccommodationBooking b : all) {
            if (b.getBookingId() != null && b.getBookingId().contains(id)) {
                return b;
            }
        }

        throw new NoSuchElementException("Booking not found with ID: " + bookingId);
    }

    /**
     * Legacy System, 
     * get unique customers from bookings
     * We do not use this again, but we will not remove it yet
     */
    public List<CustomerSummaryResponseDTO> getCustomers() {
        var bookings = bookingRepository.findAll();
        Map<String, CustomerSummaryResponseDTO> map = new LinkedHashMap<>();
        for (AccommodationBooking b : bookings) {
            if (b.getCustomerId() != null) {
                String id = b.getCustomerId().toString();
                if (!map.containsKey(id)) {
                    map.put(id, new CustomerSummaryResponseDTO(id, b.getCustomerName(), b.getCustomerEmail(),
                            b.getCustomerPhone()));
                }
            }
        }
        return new ArrayList<>(map.values());
    }
}
