package ai.xenopilot.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByCustomerIdOrderByPurchaseDateDesc(UUID customerId);
    Page<CustomerOrder> findByCustomerId(UUID customerId, Pageable pageable);
    long countByCustomerId(UUID customerId);

    @Query("select coalesce(sum(o.amount), 0) from CustomerOrder o")
    BigDecimal totalRevenue();

}
