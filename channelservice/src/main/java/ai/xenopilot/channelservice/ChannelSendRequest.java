package ai.xenopilot.channelservice;

import java.util.UUID;

public record ChannelSendRequest(
        UUID communicationId,
        String recipient,
        String message,
        String channel) {
}