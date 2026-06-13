package ai.xenopilot.campaign;

import ai.xenopilot.ai.OpenAiClient;
import ai.xenopilot.channel.CommunicationChannel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CampaignCopilotService {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public CampaignCopilotService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    public CampaignCopilotResponse generateCampaign(CampaignCopilotRequest request) {
        if (openAiClient.isConfigured()) {
            try {
                String json = openAiClient.generateJson(systemPrompt(), request.goal());
                JsonNode node = objectMapper.readTree(json);
                return new CampaignCopilotResponse(
                        node.path("campaignName").asText("Campaign"),
                        node.path("campaignObjective").asText(request.goal()),
                        node.path("recommendedAudience").asText("All customers"),
                        node.path("generatedFilters").toString(),
                        parseCommunicationChannel(node.path("recommendedChannel").asText("EMAIL")),
                        node.path("messageContent").asText(""),
                        node.path("campaignReasoning").asText(""),
                        node.path("estimatedAudienceSize").asLong(1000));
            } catch (Exception ignored) {
                return localCampaign(request.goal());
            }
        }
        return localCampaign(request.goal());
    }

    private CampaignCopilotResponse localCampaign(String goal) {
        String normalized = goal.toLowerCase(Locale.ROOT);
        
        String campaignName = "Smart Campaign";
        String audience = "All customers";
        CommunicationChannel channel = CommunicationChannel.EMAIL;
        String message = "Check out our latest offer!";
        String reasoning = "Automatically generated campaign";
        long estimatedSize = 5000;

        if (normalized.contains("dormant") || normalized.contains("churn")) {
            campaignName = "Win-Back Campaign";
            audience = "Inactive customers (no purchase > 90 days)";
            channel = CommunicationChannel.EMAIL;
            message = "We miss you! Get 20% off your next purchase.";
            reasoning = "Targeting customers who haven't engaged recently to re-activate them.";
            estimatedSize = 1200;
        } else if (normalized.contains("repeat") || normalized.contains("repeat purchase")) {
            campaignName = "Loyalty Boost Campaign";
            audience = "Repeat customers";
            channel = CommunicationChannel.EMAIL;
            message = "Thank you for being a loyal customer. Here's an exclusive offer.";
            reasoning = "Rewarding customers who have made multiple purchases.";
            estimatedSize = 3500;
        } else if (normalized.contains("beauty")) {
            campaignName = "Beauty Product Launch";
            audience = "Customers interested in beauty products";
            channel = CommunicationChannel.EMAIL;
            message = "Introducing our new beauty collection - be the first to explore!";
            reasoning = "Launching new beauty products to interested segments.";
            estimatedSize = 2100;
        } else if (normalized.contains("fashion")) {
            campaignName = "Fashion Season Campaign";
            audience = "Fashion shoppers";
            channel = CommunicationChannel.EMAIL;
            message = "New season, new styles. Shop our latest collection now!";
            reasoning = "Promoting seasonal fashion trends to relevant customers.";
            estimatedSize = 2800;
        } else if (normalized.contains("sms") || normalized.contains("text")) {
            campaignName = "Quick SMS Offer";
            audience = "Mobile-first customers";
            channel = CommunicationChannel.SMS;
            message = "Exclusive offer: 15% OFF today only. Shop now!";
            reasoning = "Using SMS for immediate engagement with time-sensitive offer.";
            estimatedSize = 1500;
        }

        var filters = objectMapper.createObjectNode();
        if (normalized.contains("high value")) {
            filters.putObject("totalSpend").put("operator", "gte").put("value", 1000);
        }
        if (normalized.contains("frequent")) {
            filters.putObject("orderCount").put("operator", "gte").put("value", 5);
        }

        return new CampaignCopilotResponse(
                campaignName,
                goal,
                audience,
                filters.toString(),
                channel,
                message,
                reasoning,
                estimatedSize);
    }

    private CommunicationChannel parseCommunicationChannel(String channel) {
        return switch (channel.toUpperCase()) {
            case "SMS" -> CommunicationChannel.SMS;
            case "WHATSAPP" -> CommunicationChannel.WHATSAPP;
            case "RCS" -> CommunicationChannel.RCS;
            default -> CommunicationChannel.EMAIL;
        };
    }

    private String systemPrompt() {
        return """
                Convert a business goal into a comprehensive campaign strategy.
                Return JSON with this structure:
                {
                  "campaignName": "Name for the campaign",
                  "campaignObjective": "Full objective",
                  "recommendedAudience": "Description of target audience",
                  "generatedFilters": "JSON filters for audience matching",
                  "recommendedChannel": "EMAIL|SMS|WHATSAPP|RCS",
                  "messageContent": "Campaign message/subject line",
                  "campaignReasoning": "Why this approach will work",
                  "estimatedAudienceSize": number
                }
                """;
    }
}
