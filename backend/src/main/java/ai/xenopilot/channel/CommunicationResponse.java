package ai.xenopilot.channel;

import java.time.Instant;
import java.util.UUID;

public record CommunicationResponse(
        UUID id,
        UUID campaignId,
        UUID customerId,
        String customerName,
        CommunicationChannel channel,
        CommunicationStatus status,
        String message,
        Instant createdAt,
        Instant sentAt,
        Instant deliveredAt,
        Instant readAt,
        Instant clickedAt,
        Instant convertedAt,
        Instant failedAt
) {
    public static CommunicationResponse from(Communication communication) {
        return new CommunicationResponse(
                communication.getId(),
                communication.getCampaign().getId(),
                communication.getCustomer().getId(),
                communication.getCustomer().getFirstName() + " " + communication.getCustomer().getLastName(),
                communication.getChannel(),
                communication.getStatus(),
                communication.getMessage(),
                communication.getCreatedAt(),
                communication.getSentAt(),
                communication.getDeliveredAt(),
                communication.getReadAt(),
                communication.getClickedAt(),
                communication.getConvertedAt(),
                communication.getFailedAt()
        );
    }
}
