package ai.xenopilot.channel;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/communications")
public class CommunicationController {
    private final CommunicationRepository communicationRepository;
    private final CommunicationEventRepository communicationEventRepository;

    public CommunicationController(
            CommunicationRepository communicationRepository,
            CommunicationEventRepository communicationEventRepository) {
        this.communicationRepository = communicationRepository;
        this.communicationEventRepository = communicationEventRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunicationResponse> getCommunication(@PathVariable UUID id) {
        return communicationRepository.findById(id)
                .map(comm -> ResponseEntity.ok(CommunicationResponse.from(comm)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<CommunicationResponse>> getCommunicationsByCampaign(@PathVariable UUID campaignId) {
        List<CommunicationResponse> communications = communicationRepository
                .findByCampaignIdOrderByCreatedAtDesc(campaignId)
                .stream()
                .map(CommunicationResponse::from)
                .toList();
        return ResponseEntity.ok(communications);
    }

    @GetMapping("/{communicationId}/events")
    public ResponseEntity<List<CommunicationEventResponse>> getCommunicationEvents(@PathVariable UUID communicationId) {
        List<CommunicationEventResponse> events = communicationEventRepository
                .findByCommunicationIdOrderByTimestampDesc(communicationId)
                .stream()
                .map(CommunicationEventResponse::from)
                .toList();
        return ResponseEntity.ok(events);
    }
    @GetMapping("/campaign/{campaignId}/analytics")
    public ResponseEntity<Map<String, Object>> getCampaignAnalytics(
            @PathVariable UUID campaignId) {

        long total = communicationRepository.countByCampaignId(campaignId);
        long sent = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.SENT);
        long delivered = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.DELIVERED);
        long read = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.READ);
        long clicked = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.CLICKED);
        long converted = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.CONVERTED);
        long failed = communicationRepository.countByCampaignIdAndStatus(
                campaignId,
                CommunicationStatus.FAILED);

        double deliveryRate = total == 0 ? 0 : (delivered * 100.0) / total;
        double readRate = delivered == 0 ? 0 : (read * 100.0) / delivered;
        double clickRate = read == 0 ? 0 : (clicked * 100.0) / read;
        double conversionRate = clicked == 0 ? 0 : (converted * 100.0) / clicked;

        Map<String, Object> analytics = new java.util.HashMap<>();
        analytics.put("total", total);
        analytics.put("sent", sent);
        analytics.put("delivered", delivered);
        analytics.put("read", read);
        analytics.put("clicked", clicked);
        analytics.put("converted", converted);
        analytics.put("failed", failed);
        analytics.put("deliveryRate", deliveryRate);
        analytics.put("readRate", readRate);
        analytics.put("clickRate", clickRate);
        analytics.put("conversionRate", conversionRate);

        return ResponseEntity.ok(analytics);
    }
}
