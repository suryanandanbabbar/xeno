package ai.xenopilot.campaign;

import ai.xenopilot.ai.OpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AudienceAiService {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public AudienceAiService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    public GeneratedAudience generateAudience(String prompt) {
        System.out.println("AUDIENCE AI SERVICE CALLED");
        System.out.println("OPENAI CONFIGURED = " + openAiClient.isConfigured());
        if (openAiClient.isConfigured()) {
            try {
                System.out.println("CALLING GROQ");
                String json = openAiClient.generateJson(systemPrompt(), prompt);
                JsonNode node = objectMapper.readTree(json);
                return new GeneratedAudience(node.path("explanation").asText("Generated from audience request"), node.path("filters").toString());
            } catch (Exception ignored) {
                System.out.println("FALLING BACK TO LOCAL AUDIENCE");
                return localAudience(prompt);
            }
        }
        return localAudience(prompt);
    }

    private GeneratedAudience localAudience(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        var node = objectMapper.createObjectNode();
        if (normalized.contains("spent above") || normalized.contains("spent more than") || normalized.contains("high value")) {
            node.putObject("totalSpend").put("operator", "gte").put("value", extractNumber(normalized, 5000));
        }
        if (normalized.contains("more than") && normalized.contains("orders")) {
            node.putObject("orderCount").put("operator", "gt").put("value", extractNumber(normalized, 3));
        }
        if (normalized.contains("haven't purchased") || normalized.contains("dormant") || normalized.contains("churn")) {
            node.put("daysSinceLastPurchase", extractNumber(normalized, 60));
        }
        if (normalized.contains("delhi")) node.put("city", "Delhi");
        if (normalized.contains("beauty")) node.put("category", "Beauty");
        if (normalized.contains("fashion")) node.put("category", "Fashion");
        if (node.isEmpty()) {
            node.putObject("orderCount").put("operator", "gte").put("value", 1);
        }
        return new GeneratedAudience("Matched audience using spend, order, recency, city, and category signals inferred from the request.", node.toString());
    }

    private int extractNumber(String text, int fallback) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private String systemPrompt() {
        return """
                Convert natural-language CRM audience requests into JSON only.
                Return shape: {"explanation":"...", "filters":{...}}.
                Supported filters:
                totalSpend: {"operator":"gte|gt|lte|lt|eq","value": number}
                orderCount: {"operator":"gte|gt|lte|lt|eq","value": number}
                lastPurchaseDate: {"operator":"before|after","value":"YYYY-MM-DD"}
                daysSinceLastPurchase: number
                city: string
                category: string
                Today is %s.
                """.formatted(LocalDate.now());
    }

    public record GeneratedAudience(String explanation, String filters) {
    }
}
