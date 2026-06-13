package ai.xenopilot.customer;

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
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Page<CustomerResponse> list(@RequestParam(required = false) String search, @PageableDefault(size = 20) Pageable pageable) {
        return customerService.search(search, pageable);
    }

    @GetMapping("/{id}")
    public CustomerProfileResponse get(@PathVariable UUID id) {
        return customerService.getProfile(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        customerService.delete(id);
    }

    @PostMapping("/import/preview")
    public CsvImportPreviewResponse<CustomerRequest> previewImport(@RequestParam("file") MultipartFile file) {
        return customerService.previewCsv(file);
    }

    @PostMapping("/import")
    public CsvImportPreviewResponse<CustomerRequest> importCsv(@RequestParam("file") MultipartFile file) {
        return customerService.importCsv(file);
    }
}
