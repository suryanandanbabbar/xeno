package ai.xenopilot.channelservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    private final SimulatorService simulatorService;

    public ChannelController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody ChannelSendRequest request) {
        simulatorService.simulateDelivery(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}