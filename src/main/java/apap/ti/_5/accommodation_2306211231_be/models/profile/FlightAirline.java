package apap.ti._5.accommodation_2306211231_be.models.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flight_airline")
@Getter
@Setter
@NoArgsConstructor
public class FlightAirline extends EndUser {
    // Airline-specific future fields
}
