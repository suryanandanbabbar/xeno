package ai.xenopilot.campaign;

import ai.xenopilot.channel.ChannelAnalyticsResponse;
import ai.xenopilot.channel.Communication;
import ai.xenopilot.channel.CommunicationChannel;
import ai.xenopilot.channel.CommunicationEventRepository;
import ai.xenopilot.channel.CommunicationEventType;
import ai.xenopilot.channel.CommunicationRepository;
import ai.xenopilot.channel.CommunicationStatus;
import ai.xenopilot.common.ResourceNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final CampaignRepository campaignRepository;
    private final CommunicationRepository communicationRepository;
    private final CommunicationEventRepository communicationEventRepository;

    public AnalyticsService(
            CampaignRepository campaignRepository,
            CommunicationRepository communicationRepository,
            CommunicationEventRepository communicationEventRepository) {
        this.campaignRepository = campaignRepository;
        this.communicationRepository = communicationRepository;
        this.communicationEventRepository = communicationEventRepository;
    }

    public CampaignAnalyticsResponse getCampaignAnalytics(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        long total = communicationRepository.countByCampaignId(campaignId);

        long sent = total;

        long delivered = communicationEventRepository
                .countDistinctCommunicationsByCampaignAndEventType(
                        campaignId,
                        CommunicationEventType.DELIVERED);

        long read = communicationEventRepository
                .countDistinctCommunicationsByCampaignAndEventType(
                        campaignId,
                        CommunicationEventType.READ);

        long clicked = communicationEventRepository
                .countDistinctCommunicationsByCampaignAndEventType(
                        campaignId,
                        CommunicationEventType.CLICKED);

        long converted = communicationEventRepository
                .countDistinctCommunicationsByCampaignAndEventType(
                        campaignId,
                        CommunicationEventType.CONVERTED);

        long failed = communicationEventRepository
                .countDistinctCommunicationsByCampaignAndEventType(
                        campaignId,
                        CommunicationEventType.FAILED);

        double deliveryRate = total > 0 ? (delivered * 100.0) / total : 0;
        double readRate = delivered > 0 ? (read * 100.0) / delivered : 0;
        double clickRate = read > 0 ? (clicked * 100.0) / read : 0;
        double conversionRate = clicked > 0 ? (converted * 100.0) / clicked : 0;
        
        return new CampaignAnalyticsResponse(
                campaign.getId(),
                campaign.getName(),
                sent,
                delivered,
                read,
                clicked,
                converted,
                failed,
                total,
                deliveryRate,
                readRate,
                clickRate,
                conversionRate);
    }

    public ChannelAnalyticsResponse getChannelAnalytics(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        List<Communication> communications = communicationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);
        
        Map<CommunicationChannel, ChannelData> channelData = new HashMap<>();
        
        for (Communication comm : communications) {
            CommunicationChannel channel = comm.getChannel();
            ChannelData data = channelData.getOrDefault(channel, new ChannelData(0, 0, 0, 0, 0, 0, 0));
            
            data.total++;
            if (comm.getStatus() == CommunicationStatus.SENT) data.sent++;
            else if (comm.getStatus() == CommunicationStatus.DELIVERED) data.delivered++;
            else if (comm.getStatus() == CommunicationStatus.READ) data.read++;
            else if (comm.getStatus() == CommunicationStatus.CLICKED) data.clicked++;
            else if (comm.getStatus() == CommunicationStatus.CONVERTED) data.converted++;
            else if (comm.getStatus() == CommunicationStatus.FAILED) data.failed++;
            
            channelData.put(channel, data);
        }
        
        List<ChannelAnalyticsResponse.ChannelMetricsData> metrics = channelData.entrySet().stream()
                .map(entry -> {
                    CommunicationChannel channel = entry.getKey();
                    ChannelData data = entry.getValue();
                    double deliveryRate = data.total > 0 ? (data.delivered * 100.0) / data.total : 0;
                    double readRate = data.delivered > 0 ? (data.read * 100.0) / data.delivered : 0;
                    double clickRate = data.read > 0 ? (data.clicked * 100.0) / data.read : 0;
                    double conversionRate = data.clicked > 0 ? (data.converted * 100.0) / data.clicked : 0;
                    return new ChannelAnalyticsResponse.ChannelMetricsData(
                            channel,
                            data.total,
                            data.sent,
                            data.delivered,
                            data.read,
                            data.clicked,
                            data.converted,
                            data.failed,
                            deliveryRate,
                            readRate,
                            clickRate,
                            conversionRate);
                })
                .toList();
        
        return new ChannelAnalyticsResponse(campaign.getId(), campaign.getName(), metrics);
    }

    private static class ChannelData {
        long sent;
        long delivered;
        long read;
        long clicked;
        long converted;
        long failed;
        long total;

        ChannelData(long sent, long delivered, long read, long clicked,
                    long converted, long failed, long total) {
            this.sent = sent;
            this.delivered = delivered;
            this.read = read;
            this.clicked = clicked;
            this.converted = converted;
            this.failed = failed;
            this.total = total;
        }
    }
}
