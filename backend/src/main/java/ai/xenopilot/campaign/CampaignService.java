package ai.xenopilot.campaign;

import ai.xenopilot.channel.*;
import ai.xenopilot.common.ResourceNotFoundException;
import ai.xenopilot.customer.Customer;
import ai.xenopilot.customer.CustomerRepository;
import ai.xenopilot.customer.CustomerResponse;
import ai.xenopilot.segment.Segment;
import ai.xenopilot.segment.SegmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final CommunicationRepository communicationRepository;
    private final SegmentRepository segmentRepository;
    private final CustomerRepository customerRepository;
    private final AudienceFilterService audienceFilterService;
    private final ChannelServiceClient channelServiceClient;

    public CampaignService(
            CampaignRepository campaignRepository,
            CommunicationRepository communicationRepository,
            SegmentRepository segmentRepository,
            CustomerRepository customerRepository,
            AudienceFilterService audienceFilterService,
            ChannelServiceClient channelServiceClient) {
        this.campaignRepository = campaignRepository;
        this.communicationRepository = communicationRepository;
        this.segmentRepository = segmentRepository;
        this.customerRepository = customerRepository;
        this.audienceFilterService = audienceFilterService;
        this.channelServiceClient = channelServiceClient;
    }

    public Page<CampaignResponse> list(Pageable pageable) {
        return campaignRepository.findAll(pageable).map(CampaignResponse::from);
    }

    public CampaignResponse getById(UUID id) {
        Campaign campaign = findCampaign(id);
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        Segment segment = segmentRepository.findById(request.segmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
        Campaign campaign = new Campaign(
                request.name(),
                request.objective(),
                segment,
                request.channel(),
                request.message(),
                request.reasoning());
        Campaign saved = campaignRepository.save(campaign);
        return CampaignResponse.from(saved);
    }

    @Transactional
    public CampaignResponse update(UUID id, CampaignRequest request) {
        Campaign campaign = findCampaign(id);
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Can only edit campaigns in DRAFT status");
        }
        Segment segment = segmentRepository.findById(request.segmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
        campaign.update(
                request.name(),
                request.objective(),
                segment,
                request.channel(),
                request.message(),
                request.reasoning(),
                request.status() != null ? request.status() : CampaignStatus.DRAFT);
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public void archive(UUID id) {

        Campaign campaign = findCampaign(id);

        if (campaign.getStatus() == CampaignStatus.ARCHIVED) {
            throw new IllegalStateException("Campaign already archived");
        }

        campaign.markArchived();

        campaignRepository.save(campaign);
    }

    @Transactional
    public LaunchCampaignResponse launch(UUID id) {
        Campaign campaign = findCampaign(id);

        List<CustomerResponse> audience = audienceFilterService.matchSegment(campaign.getSegment().getId());

        if (audience.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No customers matched the selected segment.");
        }

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Campaign must be in DRAFT status to launch");
        }

        List<Communication> communications = audience.stream()
                .map(customerResponse -> {
                    Customer customer = customerRepository.findById(customerResponse.id())
                            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    Communication comm = new Communication(
                            campaign,
                            customer,
                            campaign.getChannel(),
                            campaign.getMessage());
                    return comm;
                })
                .toList();

        communicationRepository.saveAll(communications);

        for (Communication comm : communications) {
            comm.setStatus(CommunicationStatus.SENT);
            channelServiceClient.send(comm);
        }
        communicationRepository.saveAll(communications);

        campaign.markLaunched();
        campaignRepository.save(campaign);

        return new LaunchCampaignResponse(
                campaign.getId(),
                audience.size(),
                communications.size(),
                CampaignStatus.LAUNCHED);
    }

    public CampaignMetricsResponse getMetrics(UUID campaignId) {
        Campaign campaign = findCampaign(campaignId);
        long audienceSize = audienceFilterService.countFilters(campaign.getSegment().getFilterJson());
        long totalCommunications = communicationRepository.countByCampaignId(campaignId);
        long sent = communicationRepository.countByCampaignIdAndStatus(campaignId, CommunicationStatus.SENT);
        long delivered = communicationRepository.countByCampaignIdAndStatus(campaignId, CommunicationStatus.DELIVERED);
        long failed = communicationRepository.countByCampaignIdAndStatus(campaignId, CommunicationStatus.FAILED);

        return new CampaignMetricsResponse(audienceSize, totalCommunications, sent, delivered, failed);
    }

    private Campaign findCampaign(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
    }

}
