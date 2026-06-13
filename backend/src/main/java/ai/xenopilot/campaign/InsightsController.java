package ai.xenopilot.campaign;

import ai.xenopilot.insights.InsightsResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insights")
public class InsightsController {
    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<InsightsResponse> getCampaignInsights(@PathVariable UUID id) {
        return ResponseEntity.ok(insightsService.generateInsights(id));
    }
}
