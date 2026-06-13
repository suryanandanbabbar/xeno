package ai.xenopilot.campaign;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.xenopilot.channel.ChannelAnalyticsResponse;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<CampaignAnalyticsResponse> getCampaignAnalytics(@PathVariable UUID id) {
        return ResponseEntity.ok(analyticsService.getCampaignAnalytics(id));
    }

    @GetMapping("/channels/{campaignId}")
    public ResponseEntity<ChannelAnalyticsResponse> getChannelAnalytics(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(analyticsService.getChannelAnalytics(campaignId));
    }
}
