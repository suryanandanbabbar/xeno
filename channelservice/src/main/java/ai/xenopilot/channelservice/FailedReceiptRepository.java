package ai.xenopilot.channelservice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FailedReceiptRepository extends JpaRepository<FailedReceipt, UUID> {
}