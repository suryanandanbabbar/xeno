package ai.xenopilot.channelservice;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class SimulatorService {
    private final CallbackClient callbackClient;
    private final Random random = new Random();

    public SimulatorService(CallbackClient callbackClient) {
        this.callbackClient = callbackClient;
    }

    @Async
    public void simulateDelivery(ChannelSendRequest request) {
        try {
            // Wait 2 seconds before DELIVERED logic
            Thread.sleep(2000);

            // DELIVERED -> 100%
            if (random.nextDouble() > 1.00) {
                callbackClient.sendReceipt(request.communicationId(), "FAILED");
                return;
            }

            callbackClient.sendReceipt(request.communicationId(), "DELIVERED");

            // Wait 3 seconds before READ logic
            Thread.sleep(3000);

            // READ -> 80%
            if (random.nextDouble() > 0.80) {
                return;
            }

            callbackClient.sendReceipt(request.communicationId(), "READ");

            // Wait 2 seconds before CLICKED logic
            Thread.sleep(2000);

            // CLICKED -> 70%
            if (random.nextDouble() > 0.70) {
                return;
            }

            callbackClient.sendReceipt(request.communicationId(), "CLICKED");

            // Wait 2 seconds before CONVERTED logic
            Thread.sleep(2000);

            // CONVERTED -> 50%
            if (random.nextDouble() > 0.50) {
                return;
            }

            callbackClient.sendReceipt(request.communicationId(), "CONVERTED");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}