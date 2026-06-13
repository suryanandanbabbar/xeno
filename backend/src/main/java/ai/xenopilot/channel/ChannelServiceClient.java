package ai.xenopilot.channel;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChannelServiceClient {

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

        ResponseEntity<Void> response =
                restTemplate.postForEntity(url, payload, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Failed to dispatch communication to channel service");
        }
    }
}