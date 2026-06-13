package ai.xenopilot.channel;

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

@Entity
@Table(name = "communication_events")
public class CommunicationEvent {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "communication_id", nullable = false)
    private Communication communication;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private CommunicationEventType eventType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "source", length = 100)
    private String source;

    protected CommunicationEvent() {
    }

    public CommunicationEvent(
        Communication communication,
        CommunicationEventType eventType,
        Instant timestamp) {
            this.communication = communication;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }

        public CommunicationEvent(
            Communication communication,
            CommunicationEventType eventType) {

        this(
            communication,
            eventType,
            Instant.now()
        );
    }

    public CommunicationEvent(
        Communication communication,
        CommunicationEventType eventType,
        Instant timestamp,
        String source) {

        this.communication = communication;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.source = source;
    }

    public UUID getId() { return id; }
    public Communication getCommunication() { return communication; }
    public CommunicationEventType getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public String getSource() {
        return source;
    }
}
