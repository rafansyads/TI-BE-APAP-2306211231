package apap.ti._5.accommodation_2306211231_be.restservice.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restmapper.EndUserMapper;
import apap.ti._5.accommodation_2306211231_be.restmapper.CustomerMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndUserRestService {

	private final SuperadminRepository superadminRepository;
	private final RentalVendorRepository rentalVendorRepository;
	private final FlightAirlineRepository flightAirlineRepository;
	private final InsuranceProviderRepository insuranceProviderRepository;
	private final TourPackageVendorRepository tourPackageVendorRepository;
	private final CustomerRepository customerRepository;
	private final AuthRestService authRestService;

	/**
	 * Return all end users, optionally filtered by role. Only callers with ROLE_SUPERADMIN are allowed.
	 * Filtering logic is handled here in the service layer. Results are mapped to lightweight DTOs.
	 * @param roleFilter optional role name (e.g., SUPERADMIN, RENTAL_VENDOR, CUSTOMER)
	 */
	public List<EndUserResponseDTO> getAllEndUsersByRole(String roleFilter) {
		// Ensure caller has SUPERADMIN authority
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getAuthorities().stream().noneMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()))) {
			throw new AccessDeniedException("Only SUPERADMIN can access this resource");
		}

		List<EndUser> result = new ArrayList<>();
		if (roleFilter == null || roleFilter.isBlank()) {
			// return all users across all role tables
			result.addAll(superadminRepository.findAll());
			result.addAll(rentalVendorRepository.findAll());
			result.addAll(flightAirlineRepository.findAll());
			result.addAll(insuranceProviderRepository.findAll());
			result.addAll(tourPackageVendorRepository.findAll());
			result.addAll(customerRepository.findAll());
		} else {
			String role = roleFilter.trim().toUpperCase();
			switch (role) {
				case "SUPERADMIN":
					result.addAll(superadminRepository.findAll());
					break;
				case "RENTAL_VENDOR":
					result.addAll(rentalVendorRepository.findAll());
					break;
				case "FLIGHT_AIRLINE":
					result.addAll(flightAirlineRepository.findAll());
					break;
				case "INSURANCE_PROVIDER":
					result.addAll(insuranceProviderRepository.findAll());
					break;
				case "TOUR_PACKAGE_VENDOR":
					result.addAll(tourPackageVendorRepository.findAll());
					break;
				case "CUSTOMER":
				case "ENDCUSTOMER":
				default:
					result.addAll(customerRepository.findAll());
					break;
			}
		}

		// Map entities to DTOs including resolved role using mapper
		List<EndUserResponseDTO> dtos = new ArrayList<>();
		for (EndUser u : result) {
			List<String> roles = authRestService.resolveRoles(u);
			String role = roles.isEmpty() ? null : roles.get(0);
			EndUserResponseDTO dto = EndUserMapper.toDTO(u, role);
			dtos.add(dto);
		}
		return dtos;
	}

	/**
	 * Return all Customer entities, optionally filtered by name or email.
	 * Accessible to SUPERADMIN and all vendor roles; customers themselves are not allowed.
	 * @param nameFilter optional name substring filter
	 * @param emailFilter optional email substring filter
	 */
	public List<CustomerResponseDTO> getAllCustomersFiltered(String nameFilter, String emailFilter) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AccessDeniedException("You are not authorized to access this resource.");
		}

		boolean allCustomer = auth.getAuthorities().stream()
				.allMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));
		if (allCustomer) {
			throw new AccessDeniedException("You are not authorized to access this resource.");
		}

		List<Customer> customers;
		boolean hasName = nameFilter != null && !nameFilter.isBlank();
		boolean hasEmail = emailFilter != null && !emailFilter.isBlank();

		if (hasName && hasEmail) {
			customers = customerRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(nameFilter.trim(), emailFilter.trim());
		} else if (hasName) {
			customers = customerRepository.findByNameContainingIgnoreCase(nameFilter.trim());
		} else if (hasEmail) {
			customers = customerRepository.findByEmailContainingIgnoreCase(emailFilter.trim());
		} else {
			customers = customerRepository.findAll();
		}

		// Map to DTOs using CustomerMapper
		List<CustomerResponseDTO> dtos = new ArrayList<>();
		for (Customer c : customers) {
			CustomerResponseDTO dto = CustomerMapper.toCustomerResponseDto(c);
			dtos.add(dto);
		}
		return dtos;
	}

}
