package ai.xenopilot.campaign;

public record CampaignMetricsResponse(long audienceSize, long communications, long dispatched, long delivered, long failed) {
}
