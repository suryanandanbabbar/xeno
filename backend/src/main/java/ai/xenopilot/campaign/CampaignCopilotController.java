package ai.xenopilot.campaign;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaign-copilot")
public class CampaignCopilotController {
    private final CampaignCopilotService campaignCopilotService;

    public CampaignCopilotController(CampaignCopilotService campaignCopilotService) {
        this.campaignCopilotService = campaignCopilotService;
    }

    @PostMapping
    public ResponseEntity<CampaignCopilotResponse> generateCampaign(@Valid @RequestBody CampaignCopilotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignCopilotService.generateCampaign(request));
    }
}
