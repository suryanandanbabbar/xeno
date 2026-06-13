package ai.xenopilot.dashboard;

import java.math.BigDecimal;

public record DashboardStatsResponse(long totalCustomers, long totalOrders, BigDecimal revenue, BigDecimal averageOrderValue) {
}
