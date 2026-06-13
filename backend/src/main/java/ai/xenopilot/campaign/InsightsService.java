package ai.xenopilot.campaign;

import ai.xenopilot.ai.OpenAiClient;
import ai.xenopilot.insights.InsightsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InsightsService {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsService analyticsService;

    public InsightsService(
            OpenAiClient openAiClient,
            ObjectMapper objectMapper,
            AnalyticsService analyticsService) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
        this.analyticsService = analyticsService;
    }

    public InsightsResponse generateInsights(UUID campaignId) {
        CampaignAnalyticsResponse analytics = analyticsService.getCampaignAnalytics(campaignId);
        
        if (openAiClient.isConfigured()) {
            try {
                String analyticsJson = objectMapper.writeValueAsString(analytics);
                String json = openAiClient.generateJson(systemPrompt(), analyticsJson);
                JsonNode node = objectMapper.readTree(json);
                
                return new InsightsResponse(
                        node.path("performanceSummary").asText("Campaign performance appears normal."),
                        node.path("reasonsForSuccess").asText("Check your analytics for details."),
                        node.path("reasonsForFailure").asText("Consider optimizing targeting and messaging."),
                        node.path("recommendedImprovements").asText("Test different channels and messaging."),
                        node.path("nextBestAction").asText("Review campaign settings and audience."));
            } catch (Exception ignored) {
                return localInsights(analytics);
            }
        }
        return localInsights(analytics);
    }

    private InsightsResponse localInsights(CampaignAnalyticsResponse analytics) {
        String summary;
        String success;
        String failure;
        String improvements;
        String nextAction;

        long total = analytics.total();
        if (total == 0) {
            summary = "Campaign has not sent any messages yet.";
            success = "Campaign is ready to launch.";
            failure = "No messages have been sent.";
            improvements = "Launch the campaign to start tracking metrics.";
            nextAction = "Launch this campaign to begin tracking communication performance.";
        } else {
            double deliveryRate = analytics.deliveryRate();
            double readRate = analytics.readRate();
            double clickRate = analytics.clickRate();
            double conversionRate = analytics.conversionRate();

            if (deliveryRate < 80) {
                summary = "Campaign has lower than expected delivery rates.";
                failure = "Many messages failed to deliver or are pending.";
            } else if (readRate < 30) {
                summary = "Campaign delivered well but engagement is low.";
                failure = "Recipients are not opening messages.";
            } else if (clickRate < 5) {
                summary = "Good engagement but low click-through rates.";
                failure = "Recipients open messages but rarely click.";
            } else {
                summary = "Campaign is performing well with good engagement across all metrics.";
                failure = "No significant issues detected.";
            }

            success = deliveryRate > 85 ? "Strong delivery performance." : "";
            success += readRate > 40 ? " Good open/read rates." : "";
            success += clickRate > 8 ? " Excellent click-through rates." : "";
            if (success.isBlank()) success = "Campaign meets baseline expectations.";

            improvements = "Consider A/B testing subject lines and message content. ";
            improvements += clickRate < 5 ? "Improve call-to-action clarity. " : "";
            improvements += readRate < 30 ? "Refine audience targeting. " : "";
            improvements += conversionRate < 2 ? "Optimize landing page or offer." : "";

            nextAction = conversionRate > 3 ? "Scale this campaign to larger audience." : 
                        clickRate > 8 ? "A/B test variations to improve conversion." :
                        readRate > 40 ? "Improve messaging to drive more clicks." :
                        "Review targeting and consider audience segmentation.";
        }

        return new InsightsResponse(summary, success, failure, improvements, nextAction);
    }

    private String systemPrompt() {
        return """
                Analyze campaign analytics and provide insights.
                Return JSON with:
                {
                  "performanceSummary": "Overall performance summary",
                  "reasonsForSuccess": "What worked well",
                  "reasonsForFailure": "What didn't work",
                  "recommendedImprovements": "How to improve",
                  "nextBestAction": "Recommended next step"
                }
                """;
    }
}
