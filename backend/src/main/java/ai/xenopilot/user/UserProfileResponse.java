package ai.xenopilot.user;

import java.util.UUID;

public record UserProfileResponse(UUID id, String name, String email, UserRole role) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
