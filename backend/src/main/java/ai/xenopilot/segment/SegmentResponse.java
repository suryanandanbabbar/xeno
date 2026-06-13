package ai.xenopilot.segment;

import java.time.Instant;
import java.util.UUID;

public record SegmentResponse(UUID id, String name, String description, String filterJson, Instant createdAt) {
    public static SegmentResponse from(Segment segment) {
        return new SegmentResponse(segment.getId(), segment.getName(), segment.getDescription(), segment.getFilterJson(), segment.getCreatedAt());
    }
}
