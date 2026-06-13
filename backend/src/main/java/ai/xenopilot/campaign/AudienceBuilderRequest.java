package ai.xenopilot.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AudienceBuilderRequest(@NotBlank @Size(max = 1000) String prompt, boolean persistSegment, String segmentName) {
}
