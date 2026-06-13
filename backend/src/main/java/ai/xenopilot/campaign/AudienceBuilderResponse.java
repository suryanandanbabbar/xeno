package ai.xenopilot.campaign;

import ai.xenopilot.customer.CustomerResponse;
import java.util.List;
import java.util.UUID;

public record AudienceBuilderResponse(
        long audienceSize,
        List<CustomerResponse> customers,
        String explanation,
        String generatedFilters,
        UUID segmentId,
        String segmentName
) {
}
