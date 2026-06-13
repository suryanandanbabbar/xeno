package ai.xenopilot.segment;

import ai.xenopilot.common.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SegmentService {
    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public SegmentService(SegmentRepository segmentRepository, ObjectMapper objectMapper) {
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    public Page<SegmentResponse> list(Pageable pageable) {
        return segmentRepository.findAll(pageable).map(SegmentResponse::from);
    }

    @Transactional
    public SegmentResponse create(SegmentRequest request) {
        validateJson(request.filterJson());
        return SegmentResponse.from(segmentRepository.save(new Segment(request.name(), request.description(), request.filterJson())));
    }

    @Transactional
    public SegmentResponse update(UUID id, SegmentRequest request) {
        validateJson(request.filterJson());
        Segment segment = findSegment(id);
        segment.update(request.name(), request.description(), request.filterJson());
        return SegmentResponse.from(segment);
    }

    @Transactional
    public void delete(UUID id) {
        segmentRepository.delete(findSegment(id));
    }

    private Segment findSegment(UUID id) {
        return segmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
    }

    private void validateJson(String filterJson) {
        try {
            objectMapper.readTree(filterJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException("filterJson must be valid JSON");
        }
    }
}
