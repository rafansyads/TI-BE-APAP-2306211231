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
@Table(name = "room_type")
public class RoomType {

    @Id
    @Column(name = "room_type_id", nullable = false, length = 64)
    @NotBlank
    private String roomTypeId;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    private String name;

    @Column(name = "price", nullable = false)
    @Min(0)
    private Integer price;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "capacity", nullable = false)
    @Min(1)
    private Integer capacity;

    @Column(name = "facility", length = 1000)
    private String facility;

    @Column(name = "floor", nullable = false)
    @Min(0)
    private Integer floor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonBackReference("property-roomtypes")
    @NotNull
    private Property property;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("roomtype-rooms")
    @Builder.Default
    private List<Room> listRoom = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public void addRoom(Room room) {
        listRoom.add(room);
        room.setRoomType(this);
    }

    public void removeRoom(Room room) {
        listRoom.remove(room);
        room.setRoomType(null);
    }
}
