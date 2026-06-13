package ai.xenopilot.campaign;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {
    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> listCampaigns(Pageable pageable) {
        return ResponseEntity.ok(campaignService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(@PathVariable UUID id,
            @Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.ok(campaignService.update(id, request));
    }

    @PostMapping("/{id}/launch")
    public ResponseEntity<LaunchCampaignResponse> launchCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.launch(id));
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<CampaignMetricsResponse> getCampaignMetrics(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getMetrics(id));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable UUID id) {

        campaignService.archive(id);

        return ResponseEntity.noContent().build();
    }
}
