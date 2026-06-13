package ai.xenopilot.dashboard;

import ai.xenopilot.customer.CustomerRepository;
import ai.xenopilot.order.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public DashboardService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public DashboardStatsResponse stats() {
        long totalOrders = orderRepository.count();
        BigDecimal revenue = orderRepository.totalRevenue().setScale(2, RoundingMode.HALF_UP);
        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : revenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        return new DashboardStatsResponse(
                customerRepository.count(),
                totalOrders,
                revenue,
                averageOrderValue
        );
    }
}
