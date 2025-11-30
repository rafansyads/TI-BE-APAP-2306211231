package apap.ti._5.accommodation_2306211231_be.security.service;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class ProfileUserDetailsService implements UserDetailsService {


    private final SuperadminRepository superadminRepository;

    private final RentalVendorRepository rentalVendorRepository;

    private final FlightAirlineRepository flightAirlineRepository;

    private final InsuranceProviderRepository insuranceProviderRepository;

    private final TourPackageVendorRepository tourPackageVendorRepository;    

    private final CustomerRepository customerRepository;
    
    private final AccommodationOwnerRepository accommodationOwnerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EndUser found = null;
        String role = null;

        Optional<Superadmin> superadmin = superadminRepository.findByUsername(username);
        if (superadmin.isPresent()) { found = superadmin.get(); role = "ROLE_SUPERADMIN"; }
        if (found == null) {
            Optional<RentalVendor> rv = rentalVendorRepository.findByUsername(username);
            if (rv.isPresent()) { found = rv.get(); role = "ROLE_RENTAL_VENDOR"; }
        }
        if (found == null) {
            Optional<FlightAirline> fa = flightAirlineRepository.findByUsername(username);
            if (fa.isPresent()) { found = fa.get(); role = "ROLE_FLIGHT_AIRLINE"; }
        }
        if (found == null) {
            Optional<InsuranceProvider> ip = insuranceProviderRepository.findByUsername(username);
            if (ip.isPresent()) { found = ip.get(); role = "ROLE_INSURANCE_PROVIDER"; }
        }
        if (found == null) {
            Optional<TourPackageVendor> tpv = tourPackageVendorRepository.findByUsername(username);
            if (tpv.isPresent()) { found = tpv.get(); role = "ROLE_TOUR_PACKAGE_VENDOR"; }
        }
        if (found == null) {
            Optional<AccommodationOwner> owner = accommodationOwnerRepository.findByUsername(username);
            if (owner.isPresent()) { found = owner.get(); role = "ROLE_ACCOMMODATION_OWNER"; }
        }
        if (found == null) {
            Optional<Customer> cust = customerRepository.findByUsername(username);
            if (cust.isPresent()) { found = cust.get(); role = "ROLE_CUSTOMER"; }
        }

        if (found == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        return User.withUsername(found.getUsername())
                .password(found.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .disabled(Boolean.TRUE.equals(found.getIsDeleted()))
                .build();
    }
}
