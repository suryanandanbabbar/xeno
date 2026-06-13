package ai.xenopilot.channel;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ChannelServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ChannelServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${CHANNEL_SERVICE_URL:http://simulator:8081}")
    private String channelServiceUrl;

    public void send(Communication communication) {
        String url = channelServiceUrl + "/channel/send";

        Map<String, Object> payload = new HashMap<>();
        payload.put("communicationId", communication.getId());
        payload.put("recipient", communication.getCustomer().getEmail());
        payload.put("message", communication.getMessage());
        payload.put("channel", communication.getChannel().name());

        try {
            ResponseEntity<Void> response =
                    restTemplate.postForEntity(url, payload, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Channel service returned non-success status: {}", response.getStatusCode());
            }
        } catch (Exception ex) {
            log.warn("Channel service unavailable. Campaign launch will continue without delivery simulation. Reason: {}", ex.getMessage());
        }
    }
}
