package ai.xenopilot.customer;

import ai.xenopilot.order.OrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CustomerProfileResponse(
        CustomerResponse customer,
        BigDecimal totalSpend,
        BigDecimal averageOrderValue,
        long orderCount,
        LocalDate lastPurchaseDate,
        Long daysSinceLastPurchase,
        List<OrderResponse> orders
) {
}
