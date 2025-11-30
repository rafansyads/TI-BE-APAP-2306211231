package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.profile.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthRestServiceUnitTest {

    @Test
    void resolveRoles_nullAndTypes() {
        var svc = new AuthRestService(mock(SuperadminRepository.class), mock(RentalVendorRepository.class), mock(FlightAirlineRepository.class), mock(InsuranceProviderRepository.class), mock(TourPackageVendorRepository.class), mock(CustomerRepository.class), mock(BCryptPasswordEncoder.class), mock(AccommodationOwnerRepository.class));
        assertEquals(0, svc.resolveRoles(null).size());

        Customer c = new Customer();
        assertEquals("ROLE_CUSTOMER", svc.resolveRoles(c).get(0));
        AccommodationOwner ao = new AccommodationOwner();
        assertEquals("ROLE_ACCOMMODATION_OWNER", svc.resolveRoles(ao).get(0));
    }

    @Test
    void existsUsername_checksAllRepos() {
        var superRepo = mock(SuperadminRepository.class);
        var custRepo = mock(CustomerRepository.class);
        when(superRepo.findByUsername("u")).thenReturn(Optional.empty());
        when(custRepo.findByUsername("u")).thenReturn(Optional.of(new Customer()));

        var svc = new AuthRestService(superRepo, mock(RentalVendorRepository.class), mock(FlightAirlineRepository.class), mock(InsuranceProviderRepository.class), mock(TourPackageVendorRepository.class), custRepo, mock(BCryptPasswordEncoder.class), mock(AccommodationOwnerRepository.class));
        assertTrue(svc.existsUsername("u"));
    }

    @Test
    void existsEmail_nullOrEmpty_returnsFalse_or_true() {
        var superRepo = mock(SuperadminRepository.class);
        var custRepo = mock(CustomerRepository.class);
        when(superRepo.findByEmailIgnoreCase("e@x.com")).thenReturn(Optional.of(new Superadmin()));

        var svc = new AuthRestService(superRepo, mock(RentalVendorRepository.class), mock(FlightAirlineRepository.class), mock(InsuranceProviderRepository.class), mock(TourPackageVendorRepository.class), custRepo, mock(BCryptPasswordEncoder.class), mock(AccommodationOwnerRepository.class));
        assertFalse(svc.existsEmail(null));
        assertTrue(svc.existsEmail("e@x.com"));
    }

    @Test
    void register_createsCustomerAndOwner() {
        var aoRepo = mock(AccommodationOwnerRepository.class);
        var custRepo = mock(CustomerRepository.class);
        var encoder = mock(BCryptPasswordEncoder.class);
        when(encoder.encode("p")).thenReturn("enc");

        var svc = new AuthRestService(mock(SuperadminRepository.class), mock(RentalVendorRepository.class), mock(FlightAirlineRepository.class), mock(InsuranceProviderRepository.class), mock(TourPackageVendorRepository.class), custRepo, encoder, aoRepo);

        RegisterRequestDTO payload = new RegisterRequestDTO();
        payload.setUsername("u"); payload.setPassword("p"); payload.setEmail("e@x.com"); payload.setName("N"); payload.setGender(true);
        when(custRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        var out = svc.register(payload);
        assertTrue(out instanceof Customer);

        // owner
        payload.setRole("ACCOMMODATION_OWNER");
        when(aoRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        var out2 = svc.register(payload);
        assertTrue(out2 instanceof AccommodationOwner);
    }

    @Test
    void findAggregateByUsername_returnsFromSecondRepo() {
        var superRepo = mock(SuperadminRepository.class);
        var aoRepo = mock(AccommodationOwnerRepository.class);
        when(superRepo.findByUsername("x")).thenReturn(Optional.empty());
        AccommodationOwner ao = new AccommodationOwner(); ao.setUsername("x");
        when(aoRepo.findByUsername("x")).thenReturn(Optional.of(ao));

        var svc = new AuthRestService(superRepo, mock(RentalVendorRepository.class), mock(FlightAirlineRepository.class), mock(InsuranceProviderRepository.class), mock(TourPackageVendorRepository.class), mock(CustomerRepository.class), mock(BCryptPasswordEncoder.class), aoRepo);
        var res = svc.findAggregateByUsername("x");
        assertNotNull(res);
        assertTrue(res instanceof EndUser);
    }
}
