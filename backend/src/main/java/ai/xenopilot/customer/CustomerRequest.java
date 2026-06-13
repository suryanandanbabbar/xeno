package ai.xenopilot.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @Email @NotBlank String email,
        @Size(max = 40) String phone,
        @Size(max = 120) String city,
        @Min(0) @Max(130) Integer age,
        Gender gender
) {
}
