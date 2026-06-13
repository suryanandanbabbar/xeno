package ai.xenopilot.order;

import ai.xenopilot.customer.CsvImportPreviewResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderResponse> list(@RequestParam(required = false) UUID customerId, @PageableDefault(size = 20) Pageable pageable) {
        return orderService.list(customerId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
        return orderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        orderService.delete(id);
    }

    @PostMapping("/import/preview")
    public CsvImportPreviewResponse<OrderRequest> previewImport(@RequestParam("file") MultipartFile file) {
        return orderService.previewCsv(file);
    }

    @PostMapping("/import")
    public CsvImportPreviewResponse<OrderRequest> importCsv(@RequestParam("file") MultipartFile file) {
        return orderService.importCsv(file);
    }
}
