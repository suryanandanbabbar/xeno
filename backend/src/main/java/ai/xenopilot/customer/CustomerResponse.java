package ai.xenopilot.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String city,
        Integer age,
        Gender gender,
        Instant createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCity(),
                customer.getAge(),
                customer.getGender(),
                customer.getCreatedAt()
        );
    }
}
