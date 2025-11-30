package apap.ti._5.accommodation_2306211231_be.restservice.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.models.profile.Superadmin;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerGetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerSetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.EndUserUpdateRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.FlightAirlineRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.InsuranceProviderRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.RentalVendorRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.SuperadminRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.TourPackageVendorRepository;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;

public class EndUserRestServiceUnitTest {

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
    void findEndUserByUsername_returnsCustomer() {
        Customer c = new Customer();
        c.setUsername("u1");
        when(customerRepository.findByUsername("u1")).thenReturn(Optional.of(c));

        EndUser res = service.findEndUserByIdentifier("u1");
        assertNotNull(res);
        assertEquals("u1", res.getUsername());
    }

    @Test
    void findEndUserByEmail_returnsCustomer() {
        Customer c = new Customer();
        c.setEmail("e@example.com");
        when(customerRepository.findByEmailIgnoreCase("e@example.com")).thenReturn(Optional.of(c));

        EndUser res = service.findEndUserByIdentifier("e@example.com");
        assertNotNull(res);
        assertEquals("e@example.com", res.getEmail());
    }

    @Test
    void getCustomerSaldo_allowedForOwner() {
        Customer c = new Customer();
        c.setUsername("cust1");
        c.setId(UUID.randomUUID());
        c.setSaldo(150L);
        when(customerRepository.findByUsername("cust1")).thenReturn(Optional.of(c));

        setAuthentication("cust1");

        CustomerGetSaldoRequestDTO dto = new CustomerGetSaldoRequestDTO();
        dto.setUsername("cust1");

        CustomerSaldoResponseDTO resp = service.getCustomerSaldo(c.getId().toString(), dto);
        assertNotNull(resp);
        assertEquals("cust1", resp.getUsername());
        assertEquals(150L, resp.getSaldo());
    }

    @Test
    void getCustomerSaldo_deniedOnIdMismatch() {
        Customer c = new Customer();
        c.setUsername("cust2");
        c.setId(UUID.randomUUID());
        c.setSaldo(10L);
        when(customerRepository.findByUsername("cust2")).thenReturn(Optional.of(c));

        setAuthentication("cust2");

        CustomerGetSaldoRequestDTO dto = new CustomerGetSaldoRequestDTO();
        dto.setUsername("cust2");

        // pass wrong id
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                service.getCustomerSaldo(UUID.randomUUID().toString(), dto)
        );
    }

    @Test
    void setCustomerSaldo_superadminAllowed() {
        Customer c = new Customer();
        c.setUsername("cust3");
        c.setId(UUID.randomUUID());
        c.setSaldo(20L);
        when(customerRepository.findByUsername("cust3")).thenReturn(Optional.of(c));

        setAuthentication("sa", "ROLE_SUPERADMIN");

        CustomerSetSaldoRequestDTO dto = new CustomerSetSaldoRequestDTO();
        dto.setUsername("cust3");
        dto.setSaldo(500L);

        CustomerSaldoResponseDTO resp = service.setCustomerSaldo(c.getId().toString(), dto);
        assertNotNull(resp);
        assertEquals(500L, resp.getSaldo());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateEndUser_nonSuperadminCannotUpdateSaldo() {
        Customer c = new Customer();
        c.setUsername("cust4");
        c.setId(UUID.randomUUID());
        when(customerRepository.findByUsername("cust4")).thenReturn(Optional.of(c));

        // authentication as the same user (allowed to update profile fields) but not superadmin
        setAuthentication("cust4");

        EndUserUpdateRequestDTO dto = new EndUserUpdateRequestDTO();
        dto.setSaldo(100L);

        // should throw because only superadmin may update saldo
        assertThrows(IllegalArgumentException.class, () -> service.updateEndUser(c.getId().toString(), dto));
    }

    @Test
    void getEndUserDtoByIdentifier_mapsToDto() {
        Superadmin s = new Superadmin();
        s.setUsername("s1");
        when(superadminRepository.findByUsername("s1")).thenReturn(Optional.of(s));
        when(authRestService.resolveRoles(any())).thenReturn(List.of("SUPERADMIN"));

        EndUserResponseDTO dto = service.getEndUserDtoByIdentifier("s1");
        assertNotNull(dto);
        assertEquals("s1", dto.getUsername());
    }
}
