package ai.xenopilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, Jwt jwt, OpenAi openai) {
    public record Cors(String allowedOrigins) {}
    public record Jwt(String secret, long expirationMinutes) {}
    public record OpenAi(String apiKey, String model) {}
}
