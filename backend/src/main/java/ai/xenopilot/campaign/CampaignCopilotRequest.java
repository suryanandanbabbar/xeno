package ai.xenopilot.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CampaignCopilotRequest(@NotBlank @Size(max = 1000) String goal) {
}
