package ai.xenopilot.campaign;

import ai.xenopilot.segment.SegmentRequest;
import ai.xenopilot.segment.SegmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audience-builder")
public class AudienceBuilderController {
    private final AudienceAiService audienceAiService;
    private final AudienceFilterService audienceFilterService;
    private final SegmentService segmentService;

    public AudienceBuilderController(
            AudienceAiService audienceAiService,
            AudienceFilterService audienceFilterService,
            SegmentService segmentService) {
        this.audienceAiService = audienceAiService;
        this.audienceFilterService = audienceFilterService;
        this.segmentService = segmentService;
    }

    @PostMapping
    public ResponseEntity<AudienceBuilderResponse> generateAudience(@Valid @RequestBody AudienceBuilderRequest request) {
        AudienceAiService.GeneratedAudience generated = audienceAiService.generateAudience(request.prompt());
        
        System.out.println("AUDIENCE REQUEST RECEIVED: " + request.prompt());

        var customers = audienceFilterService.matchFilters(generated.filters());
        long audienceSize = customers.size();
        
        UUID segmentId = null;
        String segmentName = null;
        
        if (request.persistSegment() && request.segmentName() != null && !request.segmentName().isBlank()) {
            var segmentRequest = new SegmentRequest(
                    request.segmentName(),
                    "Generated from prompt: " + request.prompt(),
                    generated.filters());
            var savedSegment = segmentService.create(segmentRequest);
            segmentId = savedSegment.id();
            segmentName = savedSegment.name();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AudienceBuilderResponse(
                        audienceSize,
                        customers,
                        generated.explanation(),
                        generated.filters(),
                        segmentId,
                        segmentName));
    }
}
