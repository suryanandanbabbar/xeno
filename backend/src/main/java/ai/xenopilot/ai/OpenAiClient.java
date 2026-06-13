package ai.xenopilot.ai;

import ai.xenopilot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiClient(AppProperties appProperties, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.baseUrl("https://api.groq.com/openai/v1").build();
    }

    public boolean isConfigured() {
        return appProperties.openai().apiKey() != null && !appProperties.openai().apiKey().isBlank();
    }

    public String generateJson(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        System.out.println("=== GROQ DEBUG ===");
        System.out.println("Model: " + appProperties.openai().model());
        System.out.println("Configured: " + isConfigured());

        Map<String, Object> payload = Map.of(
                "model", appProperties.openai().model(),
                "messages", List.of(
                        Map.of(
                                "role",
                                "system",
                                "content",
                                systemPrompt + "\nReturn ONLY valid JSON. Do not include markdown fences or explanations."
                        ),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + appProperties.openai().apiKey())
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
        System.out.println("Groq response: " + response);
        JsonNode content = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (!content.isMissingNode()) {
            return content.asText();
        }

        return "{}";
    }
}
