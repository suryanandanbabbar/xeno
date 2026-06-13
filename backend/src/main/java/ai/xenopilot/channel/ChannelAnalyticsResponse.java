package ai.xenopilot.channel;

import java.util.List;
import java.util.UUID;

public record ChannelAnalyticsResponse(
        UUID campaignId,
        String campaignName,
        List<ChannelMetricsData> channelMetrics
) {
    public record ChannelMetricsData(
            CommunicationChannel channel,
            long total,
            long sent,
            long delivered,
            long read,
            long clicked,
            long converted,
            long failed,
            double deliveryRate,
            double readRate,
            double clickRate,
            double conversionRate
    ) {
    }
}
