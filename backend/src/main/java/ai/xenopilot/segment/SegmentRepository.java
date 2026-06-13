package ai.xenopilot.segment;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentRepository extends JpaRepository<Segment, UUID> {
}
