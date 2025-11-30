package apap.ti._5.accommodation_2306211231_be.models.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "superadmin")
@Getter
@Setter
@NoArgsConstructor
public class Superadmin extends EndUser {
    // No extra fields for now
}
