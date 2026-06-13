package ai.xenopilot.channel;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunicationEventRepository extends JpaRepository<CommunicationEvent, UUID> {
    List<CommunicationEvent> findByCommunicationCampaignIdOrderByTimestampDesc(UUID campaignId);
    List<CommunicationEvent> findByCommunicationIdOrderByTimestampDesc(UUID communicationId);

    long countByCommunicationCampaignId(UUID campaignId);

    long countByCommunicationCampaignIdAndEventType(
            UUID campaignId,
            CommunicationEventType eventType);

    @Query("""
        SELECT COUNT(DISTINCT ce.communication.id)
        FROM CommunicationEvent ce
        WHERE ce.communication.campaign.id = :campaignId
        AND ce.eventType = :eventType
    """)
    long countDistinctCommunicationsByCampaignAndEventType(
            @Param("campaignId") UUID campaignId,
            @Param("eventType") CommunicationEventType eventType);

    boolean existsByCommunicationIdAndEventType(
            UUID communicationId,
            CommunicationEventType eventType);
}
