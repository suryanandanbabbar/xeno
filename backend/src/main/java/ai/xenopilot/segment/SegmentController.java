package ai.xenopilot.segment;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/segments")
public class SegmentController {
    private final SegmentService segmentService;

    public SegmentController(SegmentService segmentService) {
        this.segmentService = segmentService;
    }

    @GetMapping
    public Page<SegmentResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return segmentService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SegmentResponse create(@Valid @RequestBody SegmentRequest request) {
        return segmentService.create(request);
    }

    @PutMapping("/{id}")
    public SegmentResponse update(@PathVariable UUID id, @Valid @RequestBody SegmentRequest request) {
        return segmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        segmentService.delete(id);
    }
}
