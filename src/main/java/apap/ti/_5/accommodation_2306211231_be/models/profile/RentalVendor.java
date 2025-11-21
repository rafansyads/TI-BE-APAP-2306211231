package apap.ti._5.accommodation_2306211231_be.models.profile;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_vendor")
@Getter
@Setter
@NoArgsConstructor
public class RentalVendor extends EndUser {

    @Column(name = "phone", length = 50)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "rental_vendor_locations", joinColumns = @jakarta.persistence.JoinColumn(name = "rental_vendor_id"))
    @Column(name = "location", length = 255)
    private List<String> listOfLocations = new ArrayList<>();
}
