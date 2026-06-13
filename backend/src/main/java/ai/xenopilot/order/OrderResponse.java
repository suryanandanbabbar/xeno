package ai.xenopilot.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String customerName,
        BigDecimal amount,
        String category,
        LocalDate purchaseDate
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order.getAmount(),
                order.getCategory(),
                order.getPurchaseDate()
        );
    }
}
