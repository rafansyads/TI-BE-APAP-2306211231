package apap.ti._5.accommodation_2306211231_be.restservice.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.*;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;

class EndUserRestServiceBranchTests {

    private SuperadminRepository superadminRepository;
    private AccommodationOwnerRepository accommodationOwnerRepository;
    private RentalVendorRepository rentalVendorRepository;
    private FlightAirlineRepository flightAirlineRepository;
    private InsuranceProviderRepository insuranceProviderRepository;
    private TourPackageVendorRepository tourPackageVendorRepository;
    private CustomerRepository customerRepository;
    private AuthRestService authRestService;

    private EndUserRestService service;
    private SecurityContext original;

    @BeforeEach
    void setUp() {
        original = SecurityContextHolder.getContext();
        superadminRepository = mock(SuperadminRepository.class);
        accommodationOwnerRepository = mock(AccommodationOwnerRepository.class);
        rentalVendorRepository = mock(RentalVendorRepository.class);
        flightAirlineRepository = mock(FlightAirlineRepository.class);
        insuranceProviderRepository = mock(InsuranceProviderRepository.class);
        tourPackageVendorRepository = mock(TourPackageVendorRepository.class);
        customerRepository = mock(CustomerRepository.class);
        authRestService = mock(AuthRestService.class);

        service = new EndUserRestService(
            superadminRepository,
            accommodationOwnerRepository,
            rentalVendorRepository,
            flightAirlineRepository,
            insuranceProviderRepository,
            tourPackageVendorRepository,
            customerRepository,
            authRestService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(original);
    }

    private void setAuthentication(String name, String... roles) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(name);
        when(auth.getAuthorities()).thenAnswer(inv ->
            List.of(roles).stream().map(r -> new SimpleGrantedAuthority(r)).toList()
        );
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void getAllEndUsersByRole_noAuth_throwsAccessDenied() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        assertThrows(AccessDeniedException.class, () -> service.getAllEndUsersByRole(null));
    }

    @Test
    void getAllEndUsersByRole_notSuperadmin_throwsAccessDenied() {
        setAuthentication("user", "ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> service.getAllEndUsersByRole(null));
    }

    @Test
    void getAllEndUsersByRole_superadmin_noFilter_returnsAll() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Superadmin sa = new Superadmin();
        sa.setUsername("admin");
        Customer c = new Customer();
        c.setUsername("cust");
        
        when(superadminRepository.findAll()).thenReturn(List.of(sa));
        when(customerRepository.findAll()).thenReturn(List.of(c));
        when(accommodationOwnerRepository.findAll()).thenReturn(List.of());
        when(rentalVendorRepository.findAll()).thenReturn(List.of());
        when(flightAirlineRepository.findAll()).thenReturn(List.of());
        when(insuranceProviderRepository.findAll()).thenReturn(List.of());
        when(tourPackageVendorRepository.findAll()).thenReturn(List.of());
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_SUPERADMIN"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole(null);
        
        assertEquals(2, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByAccommodationOwner() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        AccommodationOwner owner = new AccommodationOwner();
        owner.setUsername("owner1");
        when(accommodationOwnerRepository.findAll()).thenReturn(List.of(owner));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_ACCOMMODATION_OWNER"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("ACCOMMODATION_OWNER");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByRentalVendor() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        RentalVendor vendor = new RentalVendor();
        vendor.setUsername("vendor1");
        when(rentalVendorRepository.findAll()).thenReturn(List.of(vendor));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_RENTAL_VENDOR"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("RENTAL_VENDOR");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByFlightAirline() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        FlightAirline airline = new FlightAirline();
        airline.setUsername("airline1");
        when(flightAirlineRepository.findAll()).thenReturn(List.of(airline));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_FLIGHT_AIRLINE"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("FLIGHT_AIRLINE");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByInsuranceProvider() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        InsuranceProvider provider = new InsuranceProvider();
        provider.setUsername("insurance1");
        when(insuranceProviderRepository.findAll()).thenReturn(List.of(provider));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_INSURANCE_PROVIDER"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("INSURANCE_PROVIDER");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByTourPackageVendor() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setUsername("tour1");
        when(tourPackageVendorRepository.findAll()).thenReturn(List.of(vendor));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_TOUR_PACKAGE_VENDOR"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("TOUR_PACKAGE_VENDOR");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllEndUsersByRole_filterByEndCustomer() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Customer customer = new Customer();
        customer.setUsername("cust1");
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_CUSTOMER"));

        List<EndUserResponseDTO> result = service.getAllEndUsersByRole("ENDCUSTOMER");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllCustomersFiltered_noAuth_throwsAccessDenied() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        assertThrows(AccessDeniedException.class, () -> service.getAllCustomersFiltered(null, null));
    }

    @Test
    void getAllCustomersFiltered_customerRole_throwsAccessDenied() {
        setAuthentication("cust", "ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> service.getAllCustomersFiltered(null, null));
    }

    @Test
    void getAllCustomersFiltered_withBothFilters() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Customer c = new Customer();
        c.setUsername("john");
        when(customerRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase("john", "test"))
            .thenReturn(List.of(c));

        List<CustomerResponseDTO> result = service.getAllCustomersFiltered("john", "test");
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllCustomersFiltered_withNameOnly() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Customer c = new Customer();
        c.setUsername("john");
        when(customerRepository.findByNameContainingIgnoreCase("john")).thenReturn(List.of(c));

        List<CustomerResponseDTO> result = service.getAllCustomersFiltered("john", null);
        
        assertEquals(1, result.size());
    }

    @Test
    void getAllCustomersFiltered_withEmailOnly() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Customer c = new Customer();
        c.setEmail("test@test.com");
        when(customerRepository.findByEmailContainingIgnoreCase("test")).thenReturn(List.of(c));

        List<CustomerResponseDTO> result = service.getAllCustomersFiltered(null, "test");
        
        assertEquals(1, result.size());
    }

    @Test
    void findEndUserByIdentifier_nullInput_returnsNull() {
        assertNull(service.findEndUserByIdentifier(null));
    }

    @Test
    void findEndUserByIdentifier_blankInput_returnsNull() {
        assertNull(service.findEndUserByIdentifier("   "));
    }

    @Test
    void findEndUserByIdentifier_byUUID_returnsSuperadmin() {
        UUID id = UUID.randomUUID();
        Superadmin sa = new Superadmin();
        sa.setId(id);
        when(superadminRepository.findById(id)).thenReturn(Optional.of(sa));

        EndUser result = service.findEndUserByIdentifier(id.toString());
        
        assertNotNull(result);
    }

    @Test
    void findEndUserByIdentifier_byUUID_returnsAccommodationOwner() {
        UUID id = UUID.randomUUID();
        AccommodationOwner owner = new AccommodationOwner();
        owner.setId(id);
        when(superadminRepository.findById(id)).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findById(id)).thenReturn(Optional.of(owner));

        EndUser result = service.findEndUserByIdentifier(id.toString());
        
        assertNotNull(result);
    }

    @Test
    void findEndUserByIdentifier_byUUID_returnsRentalVendor() {
        UUID id = UUID.randomUUID();
        RentalVendor vendor = new RentalVendor();
        vendor.setId(id);
        when(superadminRepository.findById(id)).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findById(id)).thenReturn(Optional.empty());
        when(rentalVendorRepository.findById(id)).thenReturn(Optional.of(vendor));

        EndUser result = service.findEndUserByIdentifier(id.toString());
        
        assertNotNull(result);
    }

    @Test
    void findEndUserByIdentifier_byEmail_returnsSuperadmin() {
        Superadmin sa = new Superadmin();
        sa.setEmail("admin@test.com");
        when(superadminRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(sa));

        EndUser result = service.findEndUserByIdentifier("admin@test.com");
        
        assertNotNull(result);
    }

    @Test
    void findEndUserByIdentifier_byEmail_returnsAccommodationOwner() {
        AccommodationOwner owner = new AccommodationOwner();
        owner.setEmail("owner@test.com");
        when(superadminRepository.findByEmailIgnoreCase("owner@test.com")).thenReturn(Optional.empty());
        when(accommodationOwnerRepository.findByEmailIgnoreCase("owner@test.com")).thenReturn(Optional.of(owner));

        EndUser result = service.findEndUserByIdentifier("owner@test.com");
        
        assertNotNull(result);
    }

    @Test
    void updateEndUser_targetNotFound_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            service.updateEndUser("unknown", new EndUserUpdateRequestDTO()));
    }

    @Test
    void updateEndUser_nonSuperadminUpdatingOther_throwsAccessDenied() {
        setAuthentication("user1", "ROLE_CUSTOMER");
        
        Customer target = new Customer();
        target.setUsername("user2");
        when(customerRepository.findByUsername("user2")).thenReturn(Optional.of(target));

        assertThrows(AccessDeniedException.class, () -> 
            service.updateEndUser("user2", new EndUserUpdateRequestDTO()));
    }

    @Test
    void updateEndUser_superadminUpdatingSaldo() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Customer target = new Customer();
        target.setId(UUID.randomUUID());
        target.setUsername("cust1");
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(target));
        when(customerRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_CUSTOMER"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setSaldo(1000L);

        EndUserResponseDTO result = service.updateEndUser("cust1", dto);
        
        assertNotNull(result);
        verify(customerRepository).save(any());
    }

    @Test
    void updateEndUser_nonSuperadminUpdatingSaldo_throwsException() {
        setAuthentication("cust1", "ROLE_CUSTOMER");
        
        Customer target = new Customer();
        target.setUsername("cust1");
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(target));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setSaldo(1000L);

        assertThrows(IllegalArgumentException.class, () -> 
            service.updateEndUser("cust1", dto));
    }

    @Test
    void updateEndUser_updatesSuperadminEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        Superadmin target = new Superadmin();
        target.setId(UUID.randomUUID());
        target.setUsername("admin");
        when(superadminRepository.findByUsername("admin")).thenReturn(Optional.of(target));
        when(superadminRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_SUPERADMIN"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setName("New Name");

        EndUserResponseDTO result = service.updateEndUser("admin", dto);
        
        assertNotNull(result);
        verify(superadminRepository).save(any());
    }

    @Test
    void updateEndUser_updatesAccommodationOwnerEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        AccommodationOwner target = new AccommodationOwner();
        target.setId(UUID.randomUUID());
        target.setUsername("owner1");
        when(accommodationOwnerRepository.findByUsername("owner1")).thenReturn(Optional.of(target));
        when(accommodationOwnerRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_ACCOMMODATION_OWNER"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setEmail("new@test.com");

        EndUserResponseDTO result = service.updateEndUser("owner1", dto);
        
        assertNotNull(result);
        verify(accommodationOwnerRepository).save(any());
    }

    @Test
    void updateEndUser_updatesRentalVendorEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        RentalVendor target = new RentalVendor();
        target.setId(UUID.randomUUID());
        target.setUsername("vendor1");
        when(rentalVendorRepository.findByUsername("vendor1")).thenReturn(Optional.of(target));
        when(rentalVendorRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_RENTAL_VENDOR"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setGender(true);

        EndUserResponseDTO result = service.updateEndUser("vendor1", dto);
        
        assertNotNull(result);
        verify(rentalVendorRepository).save(any());
    }

    @Test
    void updateEndUser_updatesFlightAirlineEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        FlightAirline target = new FlightAirline();
        target.setId(UUID.randomUUID());
        target.setUsername("airline1");
        when(flightAirlineRepository.findByUsername("airline1")).thenReturn(Optional.of(target));
        when(flightAirlineRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_FLIGHT_AIRLINE"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setPassword("newpass");

        EndUserResponseDTO result = service.updateEndUser("airline1", dto);
        
        assertNotNull(result);
        verify(flightAirlineRepository).save(any());
    }

    @Test
    void updateEndUser_updatesInsuranceProviderEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        InsuranceProvider target = new InsuranceProvider();
        target.setId(UUID.randomUUID());
        target.setUsername("insurance1");
        when(insuranceProviderRepository.findByUsername("insurance1")).thenReturn(Optional.of(target));
        when(insuranceProviderRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_INSURANCE_PROVIDER"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setUsername("newusername");

        EndUserResponseDTO result = service.updateEndUser("insurance1", dto);
        
        assertNotNull(result);
        verify(insuranceProviderRepository).save(any());
    }

    @Test
    void updateEndUser_updatesTourPackageVendorEntity() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        TourPackageVendor target = new TourPackageVendor();
        target.setId(UUID.randomUUID());
        target.setUsername("tour1");
        when(tourPackageVendorRepository.findByUsername("tour1")).thenReturn(Optional.of(target));
        when(tourPackageVendorRepository.save(any())).thenReturn(target);
        when(authRestService.resolveRoles(any())).thenReturn(List.of("ROLE_TOUR_PACKAGE_VENDOR"));

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();

        EndUserResponseDTO result = service.updateEndUser("tour1", dto);
        
        assertNotNull(result);
        verify(tourPackageVendorRepository).save(any());
    }

    @Test
    void getCustomerSaldo_customerNotFound_throwsException() {
        CustomerGetSaldoRequestDTO dto = new CustomerGetSaldoRequestDTO();
        dto.setUsername("unknown");
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            service.getCustomerSaldo("id", dto));
    }

    @Test
    void getCustomerSaldo_superadmin_success() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        UUID custId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(custId);
        customer.setUsername("cust1");
        customer.setSaldo(500L);
        
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(customer));

        CustomerGetSaldoRequestDTO dto = new CustomerGetSaldoRequestDTO();
        dto.setUsername("cust1");

        CustomerSaldoResponseDTO result = service.getCustomerSaldo(custId.toString(), dto);
        
        assertNotNull(result);
        assertEquals(500L, result.getSaldo());
    }

    @Test
    void setCustomerSaldo_customerNotFound_throwsException() {
        CustomerSetSaldoRequestDTO dto = new CustomerSetSaldoRequestDTO();
        dto.setUsername("unknown");
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            service.setCustomerSaldo("id", dto));
    }

    @Test
    void setCustomerSaldo_nonSuperadminSettingOther_throwsAccessDenied() {
        setAuthentication("cust1", "ROLE_CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(custId);
        customer.setUsername("cust2");
        
        when(customerRepository.findByUsername("cust2")).thenReturn(Optional.of(customer));

        CustomerSetSaldoRequestDTO dto = new CustomerSetSaldoRequestDTO();
        dto.setUsername("cust2");

        assertThrows(AccessDeniedException.class, () -> 
            service.setCustomerSaldo(custId.toString(), dto));
    }

    @Test
    void setCustomerSaldo_idMismatch_throwsAccessDenied() {
        setAuthentication("cust1", "ROLE_CUSTOMER");
        
        UUID custId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(custId);
        customer.setUsername("cust1");
        
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(customer));

        CustomerSetSaldoRequestDTO dto = new CustomerSetSaldoRequestDTO();
        dto.setUsername("cust1");

        assertThrows(AccessDeniedException.class, () -> 
            service.setCustomerSaldo(UUID.randomUUID().toString(), dto));
    }

    @Test
    void setCustomerSaldo_success() {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        
        UUID custId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(custId);
        customer.setUsername("cust1");
        customer.setSaldo(100L);
        
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenReturn(customer);

        CustomerSetSaldoRequestDTO dto = new CustomerSetSaldoRequestDTO();
        dto.setUsername("cust1");
        dto.setSaldo(500L);

        CustomerSaldoResponseDTO result = service.setCustomerSaldo(custId.toString(), dto);
        
        assertNotNull(result);
        verify(customerRepository).save(any());
    }
}
