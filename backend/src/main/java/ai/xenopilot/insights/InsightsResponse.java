package ai.xenopilot.insights;

public record InsightsResponse(
        String performanceSummary,
        String reasonsForSuccess,
        String reasonsForFailure,
        String recommendedImprovements,
        String nextBestAction
) {
}
