package ai.xenopilot.campaign;

import java.util.UUID;

public record LaunchCampaignResponse(UUID campaignId, long audienceSize, long communicationsCreated, CampaignStatus status) {
}
