package ai.xenopilot.channel;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationRepository extends JpaRepository<Communication, UUID> {
    List<Communication> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);
    long countByCampaignId(UUID campaignId);
    long countByCampaignIdAndStatus(UUID campaignId, CommunicationStatus status);

    long countByStatus(CommunicationStatus status);

    List<Communication> findByStatus(CommunicationStatus status);

    long countByCampaignIdAndStatusIn(
            UUID campaignId,
            java.util.Collection<CommunicationStatus> statuses);
}
