package apap.ti._5.accommodation_2306211231_be.security.service;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileUserDetailsServiceTest {

    @Mock private SuperadminRepository superadminRepository;
    @Mock private RentalVendorRepository rentalVendorRepository;
    @Mock private FlightAirlineRepository flightAirlineRepository;
    @Mock private InsuranceProviderRepository insuranceProviderRepository;
    @Mock private TourPackageVendorRepository tourPackageVendorRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AccommodationOwnerRepository accommodationOwnerRepository;

    private ProfileUserDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProfileUserDetailsService(
            superadminRepository,
            rentalVendorRepository,
            flightAirlineRepository,
            insuranceProviderRepository,
            tourPackageVendorRepository,
            customerRepository,
            accommodationOwnerRepository
        );
    }

    @Test
    void loadUserByUsername_superadmin_returnsUserDetails() {
        Superadmin admin = new Superadmin();
        admin.setId(UUID.randomUUID());
        admin.setUsername("admin");
        admin.setPassword("password");
        admin.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        
        UserDetails details = service.loadUserByUsername("admin");
        
        assertNotNull(details);
        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
    }

    @Test
    void loadUserByUsername_rentalVendor_returnsUserDetails() {
        RentalVendor vendor = new RentalVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor1");
        vendor.setPassword("password");
        vendor.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("vendor1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        
        UserDetails details = service.loadUserByUsername("vendor1");
        
        assertNotNull(details);
        assertEquals("vendor1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_RENTAL_VENDOR")));
    }

    @Test
    void loadUserByUsername_flightAirline_returnsUserDetails() {
        FlightAirline airline = new FlightAirline();
        airline.setId(UUID.randomUUID());
        airline.setUsername("airline1");
        airline.setPassword("password");
        airline.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("airline1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("airline1")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("airline1")).thenReturn(Optional.of(airline));
        
        UserDetails details = service.loadUserByUsername("airline1");
        
        assertNotNull(details);
        assertEquals("airline1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_FLIGHT_AIRLINE")));
    }

    @Test
    void loadUserByUsername_insuranceProvider_returnsUserDetails() {
        InsuranceProvider provider = new InsuranceProvider();
        provider.setId(UUID.randomUUID());
        provider.setUsername("insurance1");
        provider.setPassword("password");
        provider.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("insurance1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("insurance1")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("insurance1")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("insurance1")).thenReturn(Optional.of(provider));
        
        UserDetails details = service.loadUserByUsername("insurance1");
        
        assertNotNull(details);
        assertEquals("insurance1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_PROVIDER")));
    }

    @Test
    void loadUserByUsername_tourPackageVendor_returnsUserDetails() {
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("tour1");
        vendor.setPassword("password");
        vendor.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("tour1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("tour1")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("tour1")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("tour1")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("tour1")).thenReturn(Optional.of(vendor));
        
        UserDetails details = service.loadUserByUsername("tour1");
        
        assertNotNull(details);
        assertEquals("tour1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_TOUR_PACKAGE_VENDOR")));
    }

    @Test
    void loadUserByUsername_accommodationOwner_returnsUserDetails() {
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(UUID.randomUUID());
        owner.setUsername("owner1");
        owner.setPassword("password");
        owner.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("owner1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("owner1")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("owner1")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("owner1")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("owner1")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        
        UserDetails details = service.loadUserByUsername("owner1");
        
        assertNotNull(details);
        assertEquals("owner1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ACCOMMODATION_OWNER")));
    }

    @Test
    void loadUserByUsername_customer_returnsUserDetails() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("customer1");
        customer.setPassword("password");
        customer.setIsDeleted(false);
        
        when(superadminRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("customer1")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));
        
        UserDetails details = service.loadUserByUsername("customer1");
        
        assertNotNull(details);
        assertEquals("customer1", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_deletedUser_returnsDisabledUser() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("deleted");
        customer.setPassword("password");
        customer.setIsDeleted(true);
        
        when(superadminRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("deleted")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("deleted")).thenReturn(Optional.of(customer));
        
        UserDetails details = service.loadUserByUsername("deleted");
        
        assertNotNull(details);
        assertFalse(details.isEnabled());
    }

    @Test
    void loadUserByUsername_notFound_throwsException() {
        when(superadminRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_isDeletedNull_enabledUser() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("nullDeleted");
        customer.setPassword("password");
        customer.setIsDeleted(null);
        
        when(superadminRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(rentalVendorRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(flightAirlineRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(insuranceProviderRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(tourPackageVendorRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByUsername("nullDeleted")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("nullDeleted")).thenReturn(Optional.of(customer));
        
        UserDetails details = service.loadUserByUsername("nullDeleted");
        
        assertNotNull(details);
        assertTrue(details.isEnabled());
    }
}
