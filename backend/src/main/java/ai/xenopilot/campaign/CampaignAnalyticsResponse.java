package ai.xenopilot.campaign;

import java.util.UUID;

public record CampaignAnalyticsResponse(
        UUID campaignId,
        String campaignName,
        long sent,
        long delivered,
        long read,
        long clicked,
        long converted,
        long failed,
        long total,
        double deliveryRate,
        double readRate,
        double clickRate,
        double conversionRate
) {
}
