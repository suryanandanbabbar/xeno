package ai.xenopilot.channel;

import java.time.Instant;
import java.util.UUID;

public record CommunicationEventResponse(UUID id, UUID communicationId, CommunicationEventType eventType, Instant timestamp) {
    public static CommunicationEventResponse from(CommunicationEvent event) {
        return new CommunicationEventResponse(event.getId(), event.getCommunication().getId(), event.getEventType(), event.getTimestamp());
    }
}
