package apap.ti._5.accommodation_2306211231_be.models.profile;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends EndUser {

    @Column(name = "saldo", nullable = false)
    private Long saldo = 0L;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("customer-reviews")
    private List<AccommodationReview> reviews = new ArrayList<>();
}
