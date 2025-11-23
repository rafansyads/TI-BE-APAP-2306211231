package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthRestService {

	private final SuperadminRepository superadminRepository;

	private final RentalVendorRepository rentalVendorRepository;

	private final FlightAirlineRepository flightAirlineRepository;

	private final InsuranceProviderRepository insuranceProviderRepository;

	private final TourPackageVendorRepository tourPackageVendorRepository;

	private final CustomerRepository customerRepository;
  
	private final BCryptPasswordEncoder passwordEncoder;

    private final AccommodationOwnerRepository accommodationOwnerRepository;


	// Aggregate user lookup across all role tables
	public EndUser findAggregateByUsername(String username) {
		return superadminRepository.findByUsername(username).map(u -> (EndUser) u)
				.or(() -> rentalVendorRepository.findByUsername(username).map(u -> (EndUser) u))
				.or(() -> flightAirlineRepository.findByUsername(username).map(u -> (EndUser) u))
				.or(() -> insuranceProviderRepository.findByUsername(username).map(u -> (EndUser) u))
				.or(() -> tourPackageVendorRepository.findByUsername(username).map(u -> (EndUser) u))
				.or(() -> accommodationOwnerRepository.findByUsername(username).map(u -> (EndUser) u))
				.or(() -> customerRepository.findByUsername(username).map(u -> (EndUser) u))
				.orElse(null);
	}

	public List<String> resolveRoles(EndUser user) {
		if (user == null) return List.of();
		if (user instanceof Superadmin) return List.of("ROLE_SUPERADMIN");
		if (user instanceof RentalVendor) return List.of("ROLE_RENTAL_VENDOR");
		if (user instanceof FlightAirline) return List.of("ROLE_FLIGHT_AIRLINE");
		if (user instanceof InsuranceProvider) return List.of("ROLE_INSURANCE_PROVIDER");
		if (user instanceof TourPackageVendor) return List.of("ROLE_TOUR_PACKAGE_VENDOR");
		if (user instanceof AccommodationOwner) return List.of("ROLE_ACCOMMODATION_OWNER");
		return List.of("ROLE_CUSTOMER");
	}

	public boolean existsUsername(String username) {
		return superadminRepository.findByUsername(username).isPresent() ||
				rentalVendorRepository.findByUsername(username).isPresent() ||
				flightAirlineRepository.findByUsername(username).isPresent() ||
				insuranceProviderRepository.findByUsername(username).isPresent() ||
				tourPackageVendorRepository.findByUsername(username).isPresent() ||
				accommodationOwnerRepository.findByUsername(username).isPresent() ||
				customerRepository.findByUsername(username).isPresent();
	}

	public boolean existsEmail(String email) {
		// Use repository-level case-insensitive lookups to avoid full-table scans
		if (email == null) return false;
		String maybe = email.trim();
		if (maybe.isEmpty()) return false;
		if (superadminRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		if (rentalVendorRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		if (flightAirlineRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		if (insuranceProviderRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		if (tourPackageVendorRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		if (accommodationOwnerRepository.findByEmailIgnoreCase(maybe).isPresent()) return true;
		return customerRepository.findByEmailIgnoreCase(maybe).isPresent();
	}

	public EndUser register(RegisterRequestDTO payload) {
		String roleUpper = payload.getRole() == null ? "CUSTOMER" : payload.getRole().toUpperCase();
		switch (roleUpper) {
			case "ACCOMMODATION_OWNER":
				apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner ao = new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
				ao.setUsername(payload.getUsername());
				ao.setPassword(passwordEncoder.encode(payload.getPassword()));
				ao.setName(payload.getName());
				ao.setEmail(payload.getEmail());
				ao.setGender(payload.getGender());
				return accommodationOwnerRepository.save(ao);

			case "RENTAL_VENDOR":
				RentalVendor rv = new RentalVendor();
				rv.setUsername(payload.getUsername());
				rv.setPassword(passwordEncoder.encode(payload.getPassword()));
				rv.setName(payload.getName());
				rv.setEmail(payload.getEmail());
				rv.setGender(payload.getGender());
				rv.setPhone(payload.getPhone());
				if (payload.getLocations() != null) rv.setListOfLocations(payload.getLocations());
				return rentalVendorRepository.save(rv);
			case "FLIGHT_AIRLINE":
				FlightAirline fa = new FlightAirline();
				fa.setUsername(payload.getUsername());
				fa.setPassword(passwordEncoder.encode(payload.getPassword()));
				fa.setName(payload.getName());
				fa.setEmail(payload.getEmail());
				fa.setGender(payload.getGender());
				return flightAirlineRepository.save(fa);
			case "INSURANCE_PROVIDER":
				InsuranceProvider ip = new InsuranceProvider();
				ip.setUsername(payload.getUsername());
				ip.setPassword(passwordEncoder.encode(payload.getPassword()));
				ip.setName(payload.getName());
				ip.setEmail(payload.getEmail());
				ip.setGender(payload.getGender());
				return insuranceProviderRepository.save(ip);
			case "TOUR_PACKAGE_VENDOR":
				TourPackageVendor tpv = new TourPackageVendor();
				tpv.setUsername(payload.getUsername());
				tpv.setPassword(passwordEncoder.encode(payload.getPassword()));
				tpv.setName(payload.getName());
				tpv.setEmail(payload.getEmail());
				tpv.setGender(payload.getGender());
				return tourPackageVendorRepository.save(tpv);
			default:
				Customer c = new Customer();
				c.setUsername(payload.getUsername());
				c.setPassword(passwordEncoder.encode(payload.getPassword()));
				c.setName(payload.getName());
				c.setEmail(payload.getEmail());
				c.setGender(payload.getGender());
				return customerRepository.save(c);
		}
	}
}
