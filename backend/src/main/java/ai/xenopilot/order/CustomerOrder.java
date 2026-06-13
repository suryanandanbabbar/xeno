package ai.xenopilot.order;

import ai.xenopilot.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    protected CustomerOrder() {
    }

    public CustomerOrder(Customer customer, BigDecimal amount, String category, LocalDate purchaseDate) {
        update(customer, amount, category, purchaseDate);
    }

    public void update(Customer customer, BigDecimal amount, String category, LocalDate purchaseDate) {
        this.customer = customer;
        this.amount = amount;
        this.category = category;
        this.purchaseDate = purchaseDate;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
}
