package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthRestServiceBranchTests {

    @Mock private SuperadminRepository superadminRepository;
    @Mock private RentalVendorRepository rentalVendorRepository;
    @Mock private FlightAirlineRepository flightAirlineRepository;
    @Mock private InsuranceProviderRepository insuranceProviderRepository;
    @Mock private TourPackageVendorRepository tourPackageVendorRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private AccommodationOwnerRepository accommodationOwnerRepository;

    private AuthRestService authRestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authRestService = new AuthRestService(
            superadminRepository, rentalVendorRepository, flightAirlineRepository,
            insuranceProviderRepository, tourPackageVendorRepository, customerRepository,
            passwordEncoder, accommodationOwnerRepository
        );
    }

    // ====== findAggregateByUsername - all branches ======

    @Test
    void findAggregateByUsername_superadmin() {
        Superadmin s = new Superadmin();
        s.setUsername("admin");
        when(superadminRepository.findByUsername("admin")).thenReturn(Optional.of(s));
        
        EndUser result = authRestService.findAggregateByUsername("admin");
        
        assertNotNull(result);
        assertTrue(result instanceof Superadmin);
    }

    @Test
    void findAggregateByUsername_rentalVendor() {
        when(superadminRepository.findByUsername("rv")).thenReturn(Optional.empty());
        RentalVendor rv = new RentalVendor();
        rv.setUsername("rv");
        when(rentalVendorRepository.findByUsername("rv")).thenReturn(Optional.of(rv));
        
        EndUser result = authRestService.findAggregateByUsername("rv");
        
        assertNotNull(result);
        assertTrue(result instanceof RentalVendor);
    }

    @Test
    void findAggregateByUsername_flightAirline() {
        when(superadminRepository.findByUsername("fa")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("fa")).thenReturn(Optional.empty());
        FlightAirline fa = new FlightAirline();
        fa.setUsername("fa");
        when(flightAirlineRepository.findByUsername("fa")).thenReturn(Optional.of(fa));
        
        EndUser result = authRestService.findAggregateByUsername("fa");
        
        assertNotNull(result);
        assertTrue(result instanceof FlightAirline);
    }

    @Test
    void findAggregateByUsername_insuranceProvider() {
        when(superadminRepository.findByUsername("ip")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("ip")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("ip")).thenReturn(Optional.empty());
        InsuranceProvider ip = new InsuranceProvider();
        ip.setUsername("ip");
        when(insuranceProviderRepository.findByUsername("ip")).thenReturn(Optional.of(ip));
        
        EndUser result = authRestService.findAggregateByUsername("ip");
        
        assertNotNull(result);
        assertTrue(result instanceof InsuranceProvider);
    }

    @Test
    void findAggregateByUsername_tourPackageVendor() {
        when(superadminRepository.findByUsername("tpv")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("tpv")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("tpv")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("tpv")).thenReturn(Optional.empty());
        TourPackageVendor tpv = new TourPackageVendor();
        tpv.setUsername("tpv");
        when(tourPackageVendorRepository.findByUsername("tpv")).thenReturn(Optional.of(tpv));
        
        EndUser result = authRestService.findAggregateByUsername("tpv");
        
        assertNotNull(result);
        assertTrue(result instanceof TourPackageVendor);
    }

    @Test
    void findAggregateByUsername_accommodationOwner() {
        when(superadminRepository.findByUsername("ao")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("ao")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("ao")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("ao")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("ao")).thenReturn(Optional.empty());
        AccommodationOwner ao = new AccommodationOwner();
        ao.setUsername("ao");
        when(accommodationOwnerRepository.findByUsername("ao")).thenReturn(Optional.of(ao));
        
        EndUser result = authRestService.findAggregateByUsername("ao");
        
        assertNotNull(result);
        assertTrue(result instanceof AccommodationOwner);
    }

    @Test
    void findAggregateByUsername_customer() {
        when(superadminRepository.findByUsername("cust")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("cust")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("cust")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("cust")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("cust")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("cust")).thenReturn(Optional.empty());
        Customer c = new Customer();
        c.setUsername("cust");
        when(customerRepository.findByUsername("cust")).thenReturn(Optional.of(c));
        
        EndUser result = authRestService.findAggregateByUsername("cust");
        
        assertNotNull(result);
        assertTrue(result instanceof Customer);
    }

    @Test
    void findAggregateByUsername_notFound_returnsNull() {
        when(superadminRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        
        EndUser result = authRestService.findAggregateByUsername("unknown");
        
        assertNull(result);
    }

    // ====== resolveRoles - all branches ======

    @Test
    void resolveRoles_null_returnsEmpty() {
        List<String> roles = authRestService.resolveRoles(null);
        assertTrue(roles.isEmpty());
    }

    @Test
    void resolveRoles_superadmin() {
        assertEquals(List.of("ROLE_SUPERADMIN"), authRestService.resolveRoles(new Superadmin()));
    }

    @Test
    void resolveRoles_rentalVendor() {
        assertEquals(List.of("ROLE_RENTAL_VENDOR"), authRestService.resolveRoles(new RentalVendor()));
    }

    @Test
    void resolveRoles_flightAirline() {
        assertEquals(List.of("ROLE_FLIGHT_AIRLINE"), authRestService.resolveRoles(new FlightAirline()));
    }

    @Test
    void resolveRoles_insuranceProvider() {
        assertEquals(List.of("ROLE_INSURANCE_PROVIDER"), authRestService.resolveRoles(new InsuranceProvider()));
    }

    @Test
    void resolveRoles_tourPackageVendor() {
        assertEquals(List.of("ROLE_TOUR_PACKAGE_VENDOR"), authRestService.resolveRoles(new TourPackageVendor()));
    }

    @Test
    void resolveRoles_accommodationOwner() {
        assertEquals(List.of("ROLE_ACCOMMODATION_OWNER"), authRestService.resolveRoles(new AccommodationOwner()));
    }

    @Test
    void resolveRoles_customer() {
        assertEquals(List.of("ROLE_CUSTOMER"), authRestService.resolveRoles(new Customer()));
    }

    // ====== existsUsername - all branches ======

    @Test
    void existsUsername_foundInSuperadmin() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.of(new Superadmin()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInRentalVendor() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.of(new RentalVendor()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInFlightAirline() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.of(new FlightAirline()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInInsuranceProvider() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("u")).thenReturn(Optional.of(new InsuranceProvider()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInTourPackageVendor() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("u")).thenReturn(Optional.of(new TourPackageVendor()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInAccommodationOwner() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("u")).thenReturn(Optional.of(new AccommodationOwner()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_foundInCustomer() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("u")).thenReturn(Optional.of(new Customer()));
        assertTrue(authRestService.existsUsername("u"));
    }

    @Test
    void existsUsername_notFound_returnsFalse() {
        when(superadminRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("u")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertFalse(authRestService.existsUsername("u"));
    }

    // ====== existsEmail - all branches ======

    @Test
    void existsEmail_nullEmail_returnsFalse() {
        assertFalse(authRestService.existsEmail(null));
    }

    @Test
    void existsEmail_emptyEmail_returnsFalse() {
        assertFalse(authRestService.existsEmail(""));
        assertFalse(authRestService.existsEmail("   "));
    }

    @Test
    void existsEmail_foundInSuperadmin() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new Superadmin()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInRentalVendor() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new RentalVendor()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInFlightAirline() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new FlightAirline()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInInsuranceProvider() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new InsuranceProvider()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInTourPackageVendor() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new TourPackageVendor()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInAccommodationOwner() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new AccommodationOwner()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_foundInCustomer() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(customerRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new Customer()));
        assertTrue(authRestService.existsEmail("e@x.com"));
    }

    @Test
    void existsEmail_notFound_returnsFalse() {
        when(superadminRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        when(customerRepository.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.empty());
        assertFalse(authRestService.existsEmail("e@x.com"));
    }

    // ====== register - all branches ======

    private RegisterRequestDTO createPayload(String role) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("user");
        dto.setPassword("pass");
        dto.setName("Name");
        dto.setEmail("e@x.com");
        dto.setGender(true);
        dto.setRole(role);
        return dto;
    }

    @Test
    void register_accommodationOwner() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(accommodationOwnerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("ACCOMMODATION_OWNER"));
        
        assertTrue(result instanceof AccommodationOwner);
        verify(accommodationOwnerRepository).save(any(AccommodationOwner.class));
    }

    @Test
    void register_rentalVendor() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(rentalVendorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RegisterRequestDTO payload = createPayload("RENTAL_VENDOR");
        payload.setPhone("123456");
        payload.setLocations(List.of("Jakarta", "Surabaya"));

        EndUser result = authRestService.register(payload);
        
        assertTrue(result instanceof RentalVendor);
        verify(rentalVendorRepository).save(any(RentalVendor.class));
    }

    @Test
    void register_rentalVendor_noLocations() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(rentalVendorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RegisterRequestDTO payload = createPayload("RENTAL_VENDOR");
        payload.setPhone("123456");
        payload.setLocations(null);

        EndUser result = authRestService.register(payload);
        
        assertTrue(result instanceof RentalVendor);
    }

    @Test
    void register_flightAirline() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(flightAirlineRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("FLIGHT_AIRLINE"));
        
        assertTrue(result instanceof FlightAirline);
        verify(flightAirlineRepository).save(any(FlightAirline.class));
    }

    @Test
    void register_insuranceProvider() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(insuranceProviderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("INSURANCE_PROVIDER"));
        
        assertTrue(result instanceof InsuranceProvider);
        verify(insuranceProviderRepository).save(any(InsuranceProvider.class));
    }

    @Test
    void register_tourPackageVendor() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(tourPackageVendorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("TOUR_PACKAGE_VENDOR"));
        
        assertTrue(result instanceof TourPackageVendor);
        verify(tourPackageVendorRepository).save(any(TourPackageVendor.class));
    }

    @Test
    void register_customer_defaultRole() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RegisterRequestDTO payload = createPayload(null); // null role = default customer
        EndUser result = authRestService.register(payload);
        
        assertTrue(result instanceof Customer);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_customer_explicitRole() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("CUSTOMER"));
        
        assertTrue(result instanceof Customer);
    }

    @Test
    void register_customer_unknownRole_fallsBackToCustomer() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("UNKNOWN_ROLE"));
        
        assertTrue(result instanceof Customer);
    }

    @Test
    void register_roleCase_insensitive() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(flightAirlineRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EndUser result = authRestService.register(createPayload("flight_airline"));
        
        assertTrue(result instanceof FlightAirline);
    }
}
