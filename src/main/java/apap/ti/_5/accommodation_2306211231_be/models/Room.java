package apap.ti._5.accommodation_2306211231_be.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "room_id", nullable = false, length = 64)
    @NotBlank
    private String roomId;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    private String name;

    // 0=unavailable,1=available
    @Column(name = "availability_status", nullable = false)
    @NotNull
    @Min(0)
    @Max(1)
    private Integer availabilityStatus;

    // 0=inactive,1=active
    @Column(name = "active_room", nullable = false)
    @NotNull
    @Min(0)
    @Max(1)
    private Integer activeRoom;

    @Column(name = "maintenance_start")
    private LocalDateTime maintenanceStart;

    @Column(name = "maintenance_end")
    private LocalDateTime maintenanceEnd;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    @JsonBackReference("roomtype-rooms")
    @NotNull
    private RoomType roomType;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("room-bookings")
    @Builder.Default
    private List<AccommodationBooking> bookings = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public void addBooking(AccommodationBooking booking) {
        bookings.add(booking);
        booking.setRoom(this);
    }

    public void removeBooking(AccommodationBooking booking) {
        bookings.remove(booking);
        booking.setRoom(null);
    }
}
