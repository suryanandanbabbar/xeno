package ai.xenopilot.auth;

import ai.xenopilot.user.UserProfileResponse;

public record AuthResponse(String token, UserProfileResponse user) {
}
