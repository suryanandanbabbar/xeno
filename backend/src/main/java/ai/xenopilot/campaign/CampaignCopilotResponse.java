package ai.xenopilot.campaign;

import ai.xenopilot.channel.CommunicationChannel;

public record CampaignCopilotResponse(
        String campaignName,
        String campaignObjective,
        String recommendedAudience,
        String generatedFilters,
        CommunicationChannel recommendedChannel,
        String messageContent,
        String campaignReasoning,
        long estimatedAudienceSize
) {
}
