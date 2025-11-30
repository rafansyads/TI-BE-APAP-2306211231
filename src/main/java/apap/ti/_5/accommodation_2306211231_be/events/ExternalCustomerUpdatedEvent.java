package apap.ti._5.accommodation_2306211231_be.events;

import java.time.Instant;
import java.util.UUID;

public record ExternalCustomerUpdatedEvent(UUID id, String username, String name, String email, Instant occurredAt) {}
