package ai.xenopilot.campaign;

import ai.xenopilot.channel.CommunicationChannel;
import ai.xenopilot.segment.Segment;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String objective;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationChannel channel = CommunicationChannel.EMAIL;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(length = 4000)
    private String reasoning;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Campaign() {
    }

    public Campaign(String name, String objective, Segment segment, CommunicationChannel channel, String message, String reasoning) {
        update(name, objective, segment, channel, message, reasoning, CampaignStatus.DRAFT);
    }

    public void update(String name, String objective, Segment segment, CommunicationChannel channel, String message, String reasoning, CampaignStatus status) {
        this.name = name;
        this.objective = objective;
        this.segment = segment;
        this.channel = channel;
        this.message = message;
        this.reasoning = reasoning;
        this.status = status;
    }

    public void markLaunched() {
        this.status = CampaignStatus.LAUNCHED;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getObjective() { return objective; }
    public Segment getSegment() { return segment; }
    public CampaignStatus getStatus() { return status; }
    public CommunicationChannel getChannel() { return channel; }
    public String getMessage() { return message; }
    public String getReasoning() { return reasoning; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void markArchived() {
        this.status = CampaignStatus.ARCHIVED;
    }
}
