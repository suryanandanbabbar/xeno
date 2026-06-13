package ai.xenopilot.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderRequest(
        @NotNull UUID customerId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotBlank @Size(max = 120) String category,
        @NotNull LocalDate purchaseDate
) {
}
