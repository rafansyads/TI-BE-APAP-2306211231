package apap.ti._5.accommodation_2306211231_be.restservice.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.models.profile.FlightAirline;
import apap.ti._5.accommodation_2306211231_be.models.profile.InsuranceProvider;
import apap.ti._5.accommodation_2306211231_be.models.profile.RentalVendor;
import apap.ti._5.accommodation_2306211231_be.models.profile.Superadmin;
import apap.ti._5.accommodation_2306211231_be.models.profile.TourPackageVendor;
import apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.FlightAirlineRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.InsuranceProviderRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.RentalVendorRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.SuperadminRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.TourPackageVendorRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerGetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerSetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.EndUserUpdateRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restmapper.CustomerMapper;
import apap.ti._5.accommodation_2306211231_be.restmapper.EndUserMapper;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EndUserRestService {

	private final SuperadminRepository superadminRepository;
	private final AccommodationOwnerRepository accommodationOwnerRepository;
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
			result.addAll(accommodationOwnerRepository.findAll());
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
				case "ACCOMMODATION_OWNER":
					result.addAll(accommodationOwnerRepository.findAll());
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

		if (nameFilter != null && emailFilter != null && !nameFilter.isBlank() && !emailFilter.isBlank()) {
			customers = customerRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(nameFilter.trim(), emailFilter.trim());
		} else if (nameFilter != null && !nameFilter.isBlank()) {
			customers = customerRepository.findByNameContainingIgnoreCase(nameFilter.trim());
		} else if (emailFilter != null && !emailFilter.isBlank()) {
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

	/**
	 * Find EndUser by identifier which can be UUID, username, or email.
	 * Returns the EndUser entity or null when not found.
	 */
	public EndUser findEndUserByIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) return null;

		// try UUID
		try {
			UUID id = UUID.fromString(identifier.trim());
			Optional<? extends EndUser> res;
			res = superadminRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = accommodationOwnerRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = rentalVendorRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = flightAirlineRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = insuranceProviderRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = tourPackageVendorRepository.findById(id);
			if (res.isPresent()) return res.get();
			res = customerRepository.findById(id);
			if (res.isPresent()) return res.get();
		} catch (IllegalArgumentException ex) {
			// not a UUID, continue
		}

        
		String maybeUsername = identifier.trim();
		// try username lookups
		Optional<? extends EndUser> resOpt;
		resOpt = superadminRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = accommodationOwnerRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = rentalVendorRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = flightAirlineRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = insuranceProviderRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = tourPackageVendorRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();
		resOpt = customerRepository.findByUsername(maybeUsername);
		if (resOpt.isPresent()) return resOpt.get();

		// try email lookups (case-insensitive)
		String maybeEmail = identifier.trim();
		Optional<? extends EndUser> byEmail;
		byEmail = superadminRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = accommodationOwnerRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = rentalVendorRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = flightAirlineRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = insuranceProviderRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = tourPackageVendorRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();
		byEmail = customerRepository.findByEmailIgnoreCase(maybeEmail);
		if (byEmail.isPresent()) return byEmail.get();

		return null;
	}

		/**
		 * Convenience method to get detail DTO for an identifier.
		 */
		public EndUserResponseDTO getEndUserDtoByIdentifier(String identifier) {
			EndUser u = findEndUserByIdentifier(identifier);
			if (u == null) return null;
			List<String> roles = authRestService.resolveRoles(u);
			String role = roles.isEmpty() ? null : roles.get(0);
			return EndUserMapper.toDTO(u, role);
		}

	/**
	 * Update EndUser identified by identifier according to role rules.
	 */
	@Transactional
	public EndUserResponseDTO updateEndUser(String identifier, EndUserUpdateRequestDTO dto) {
		EndUser target = findEndUserByIdentifier(identifier);
		if (target == null) throw new IllegalArgumentException("Target user not found");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String caller = auth == null ? null : auth.getName();
		boolean isSuperadmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));

		// If not superadmin, only allow user to update themself
		if (!isSuperadmin) {
			// caller must match target username or email
			if (caller == null || (!caller.equals(target.getUsername()) && (target.getEmail()==null || !caller.equalsIgnoreCase(target.getEmail())))) {
				throw new AccessDeniedException("You are not authorized to update this user");
			}
		}

		// Apply updates: superadmin may update saldo; endusers may not.
		boolean updated = false;
		if (dto.getUsername() != null && !dto.getUsername().isBlank()) { target.setUsername(dto.getUsername().trim()); updated = true; }
		if (dto.getName() != null && !dto.getName().isBlank()) { target.setName(dto.getName().trim()); updated = true; }
		if (dto.getPassword() != null && !dto.getPassword().isBlank()) { target.setPassword(dto.getPassword()); updated = true; }
		if (dto.getEmail() != null && !dto.getEmail().isBlank()) { target.setEmail(dto.getEmail().trim()); updated = true; }
		if (dto.getGender() != null) { target.setGender(dto.getGender()); updated = true; }

		if (dto.getSaldo() != null) {
			if (!isSuperadmin) {
				throw new IllegalArgumentException("Only SUPERADMIN can update saldo");
			}
			if (target instanceof Customer customer) {
				customer.setSaldo(dto.getSaldo());
				updated = true;
			}
		}

		// if any mutable field changed, set updatedAt timestamp
		if (updated) {
			try {
				target.setUpdatedAt(LocalDateTime.now());
			} catch (NoSuchMethodError | Exception ignored) {
				// If entity doesn't have updatedAt setter, ignore silently
			}
		}

		// persist to correct repository based on runtime type
		if (target instanceof Superadmin) {
			superadminRepository.save((Superadmin) target);
		} else if (target instanceof AccommodationOwner) {
				accommodationOwnerRepository.save((AccommodationOwner) target);
		} else if (target instanceof RentalVendor) {
			rentalVendorRepository.save((RentalVendor) target);
		} else if (target instanceof FlightAirline) {
			flightAirlineRepository.save((FlightAirline) target);
		} else if (target instanceof InsuranceProvider) {
			insuranceProviderRepository.save((InsuranceProvider) target);
		} else if (target instanceof TourPackageVendor) {
			tourPackageVendorRepository.save((TourPackageVendor) target);
		} else if (target instanceof Customer) {
			customerRepository.save((Customer) target);
		}

		// return mapped DTO
		List<String> roles = authRestService.resolveRoles(target);
		String role = roles.isEmpty() ? null : roles.get(0);
		return EndUserMapper.toDTO(target, role);
	}

	public CustomerSaldoResponseDTO getCustomerSaldo(String id, CustomerGetSaldoRequestDTO dto) {
		Customer customer = customerRepository.findByUsername(dto.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Customer not found"));

		// Check whether the authenticated user is allowed to access this customer's saldo
		// Only superadmin & the belonging customer themselves are allowed
		// But if the id: String provided mismatch the request DTO id, deny access
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isSuperadmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
		String caller = auth == null ? null : auth.getName();
		if (!isSuperadmin) {
			// caller must match target username or email
			if (caller == null || (!caller.equals(customer.getUsername()) && (customer.getEmail()==null || !caller.equalsIgnoreCase(customer.getEmail())))) {
				throw new AccessDeniedException("You are not authorized to access this customer's saldo");
			}
		}
		if (!id.equals(customer.getId().toString())) {
			throw new AccessDeniedException("ID mismatch: you are not authorized to access this customer's saldo");
		}
		
		CustomerSaldoResponseDTO responseDTO = new CustomerSaldoResponseDTO();
		responseDTO.setUsername(customer.getUsername());
		responseDTO.setSaldo(customer.getSaldo());
		responseDTO.setUserId(customer.getId().toString());
		return responseDTO;
	}

	@Transactional
	public CustomerSaldoResponseDTO setCustomerSaldo(String id, CustomerSetSaldoRequestDTO dto) {
		Customer customer = customerRepository.findByUsername(dto.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Customer not found"));

		// Check whether the authenticated user is allowed to access this customer's saldo
		// Only superadmin & the belonging customer themselves are allowed
		// But if the id: String provided mismatch the request DTO id, deny access
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isSuperadmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
		String caller = auth == null ? null : auth.getName();
		if (!isSuperadmin) {
			// caller must match target username or email
			if (caller == null || (!caller.equals(customer.getUsername()) && (customer.getEmail()==null || !caller.equalsIgnoreCase(customer.getEmail())))) {
				throw new AccessDeniedException("You are not authorized to access this customer's saldo");
			}
		}
		if (!id.equals(customer.getId().toString())) {
			throw new AccessDeniedException("ID mismatch: you are not authorized to access this customer's saldo");
		}
		
		customer.setSaldo(dto.getSaldo());
		customerRepository.save(customer);

		CustomerSaldoResponseDTO responseDTO = new CustomerSaldoResponseDTO();
		responseDTO.setUsername(customer.getUsername());
		responseDTO.setSaldo(customer.getSaldo());
		responseDTO.setUserId(customer.getId().toString());
		return responseDTO;
	}
}
