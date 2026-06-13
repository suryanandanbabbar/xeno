package ai.xenopilot.channel;

import ai.xenopilot.campaign.Campaign;
import ai.xenopilot.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "communications")
public class Communication {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationStatus status = CommunicationStatus.PENDING;

    @Column(nullable = false, length = 4000)
    private String message;

    @CreationTimestamp
    private Instant createdAt;

    @Column
    private Instant sentAt;

    @Column
    private Instant deliveredAt;

    @Column
    private Instant readAt;

    @Column
    private Instant clickedAt;

    @Column
    private Instant convertedAt;

    @Column
    private Instant failedAt;

    protected Communication() {
    }

    public Communication(Campaign campaign, Customer customer, CommunicationChannel channel, String message) {
        this.campaign = campaign;
        this.customer = customer;
        this.channel = channel;
        this.message = message;
    }

    public void setStatus(CommunicationStatus status) {

    if (this.status == null) {
        this.status = status;
        return;
    }

    if (!this.status.canTransitionTo(status)) {
        return;
    }

    this.status = status;

    Instant now = Instant.now();

    switch (status) {
        case SENT -> this.sentAt = now;
        case DELIVERED -> this.deliveredAt = now;
        case READ -> this.readAt = now;
        case CLICKED -> this.clickedAt = now;
        case CONVERTED -> this.convertedAt = now;
        case FAILED -> this.failedAt = now;
        default -> {
        }
    }
    }   

    public UUID getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public Customer getCustomer() { return customer; }
    public CommunicationChannel getChannel() { return channel; }
    public CommunicationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getReadAt() { return readAt; }
    public Instant getClickedAt() { return clickedAt; }
    public Instant getConvertedAt() { return convertedAt; }
    public Instant getFailedAt() { return failedAt; }
}
