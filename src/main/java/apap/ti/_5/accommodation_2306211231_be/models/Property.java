package apap.ti._5.accommodation_2306211231_be.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "property")
public class Property {

    @Id
    @Column(name = "property_id", nullable = false, length = 64)
    @NotBlank
    private String propertyId;

    @Column(name = "property_name", nullable = false, length = 255)
    @NotBlank
    private String propertyName;

    // 1=hotel
    // 2=villa
    // 3=apartment
    @Column(name = "type", nullable = false)
    @NotNull
    @Min(1)
    @Max(3)
    private Integer type;

    @Column(name = "address", nullable = false, length = 500)
    @NotBlank
    private String address;

    // Example province code, refer to your enum/list
    @Column(name = "province", nullable = false)
    @NotNull
    private Integer province;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "total_room", nullable = false)
    @Min(0)
    private Integer totalRoom;

    // 0=inactive,1=active
    @Column(name = "active_status", nullable = false)
    @NotNull
    @Min(0)
    @Max(1)
    private Integer activeStatus;

    // profit always starts from 0
    @Column(name = "profit", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer profit = 0;

    @Column(name = "owner_name", nullable = false, length = 255)
    @NotBlank
    private String ownerName;

    @Column(name = "owner_id", nullable = false)
    @NotNull
    private UUID ownerId;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Soft delete marker; when not null, property is considered deleted
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("property-roomtypes")
    @Builder.Default
    private List<RoomType> listRoomType = new ArrayList<>();

    public void addRoomType(RoomType roomType) {
        listRoomType.add(roomType);
        roomType.setProperty(this);
    }

    public void removeRoomType(RoomType roomType) {
        listRoomType.remove(roomType);
        roomType.setProperty(null);
    }
}
