package ai.xenopilot.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

import ai.xenopilot.channel.CommunicationChannel;

public record CampaignRequest(
        @NotBlank @Size(max = 180) String name,
        @NotBlank @Size(max = 1000) String objective,
        @NotNull UUID segmentId,
        @NotNull CommunicationChannel channel,
        @NotBlank @Size(max = 4000) String message,
        @Size(max = 4000) String reasoning,
        CampaignStatus status
) {
}
