package ai.xenopilot.channelservice;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CallbackClient {
    private static final Logger logger = LoggerFactory.getLogger(CallbackClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final FailedReceiptRepository failedReceiptRepository;

    @Value("${BACKEND_URL:http://backend:8080}")
    private String backendUrl;

    public CallbackClient(FailedReceiptRepository failedReceiptRepository) {
        this.failedReceiptRepository = failedReceiptRepository;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendReceipt(UUID communicationId, String status) {
        logger.info("Attempting to send receipt: {} for {}", status, communicationId);
        String url = backendUrl + "/api/receipts";

        Map<String, Object> payload = new HashMap<>();
        payload.put("communicationId", communicationId);
        payload.put("eventType", status);

        restTemplate.postForEntity(url, payload, Void.class);
        logger.info("Successfully sent receipt: {} for {}", status, communicationId);
    }

    @Recover
    public void recover(Exception e, UUID communicationId, String status) {
        logger.error("All retries failed for receipt: {} for {}. Saving to DLQ.", status, communicationId, e);
        String payload = String.format("{\"communicationId\":\"%s\",\"status\":\"%s\"}", communicationId, status);
        FailedReceipt receipt = new FailedReceipt(communicationId, payload, e.getMessage());
        failedReceiptRepository.save(receipt);
    }
}