package ai.xenopilot.campaign;

import java.time.Instant;
import java.util.UUID;

import ai.xenopilot.channel.CommunicationChannel;

public record CampaignResponse(
        UUID id,
        String name,
        String objective,
        UUID segmentId,
        String segmentName,
        CampaignStatus status,
        CommunicationChannel channel,
        String message,
        String reasoning,
        Instant createdAt,
        Instant updatedAt
) {
    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getObjective(),
                campaign.getSegment().getId(),
                campaign.getSegment().getName(),
                campaign.getStatus(),
                campaign.getChannel(),
                campaign.getMessage(),
                campaign.getReasoning(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt()
        );
    }
}
