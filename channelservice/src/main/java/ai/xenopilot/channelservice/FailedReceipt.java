package ai.xenopilot.channelservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class FailedReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID communicationId;

    @Column(length = 4000)
    private String payload;

    @Column(length = 4000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    protected FailedReceipt() {
    }

    public FailedReceipt(UUID communicationId, String payload, String errorMessage) {
        this.communicationId = communicationId;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCommunicationId() {
        return communicationId;
    }

    public String getPayload() {
        return payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}