package ai.xenopilot.channel;

import ai.xenopilot.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {
    private final CommunicationRepository communicationRepository;
    private final CommunicationEventRepository communicationEventRepository;

    public ReceiptController(
            CommunicationRepository communicationRepository,
            CommunicationEventRepository communicationEventRepository) {
        this.communicationRepository = communicationRepository;
        this.communicationEventRepository = communicationEventRepository;
    }

    @PostMapping
    public ResponseEntity<Void> receiveEvent(@Valid @RequestBody ReceiptRequest request) {
        Communication communication = communicationRepository.findById(request.communicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Communication not found"));

        CommunicationStatus currentStatus = communication.getStatus();
        CommunicationStatus newStatus = mapEventTypeToStatus(request.eventType());

        if (currentStatus != null && !currentStatus.canTransitionTo(newStatus)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        communication.setStatus(newStatus);
        communicationRepository.save(communication);

        CommunicationEvent event = new CommunicationEvent(
                communication,
                request.eventType());

        communicationEventRepository.save(event);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private CommunicationStatus mapEventTypeToStatus(CommunicationEventType eventType) {
        return switch (eventType) {
            case CREATED -> CommunicationStatus.PENDING;
            case SENT -> CommunicationStatus.SENT;
            case DELIVERED -> CommunicationStatus.DELIVERED;
            case READ -> CommunicationStatus.READ;
            case CLICKED -> CommunicationStatus.CLICKED;
            case CONVERTED -> CommunicationStatus.CONVERTED;
            case FAILED -> CommunicationStatus.FAILED;
        };
    }

    public record ReceiptRequest(
            @NotNull UUID communicationId,
            @NotNull CommunicationEventType eventType) {
    }
}
