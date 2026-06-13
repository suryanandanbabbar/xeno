package ai.xenopilot.order;

import ai.xenopilot.common.ResourceNotFoundException;
import ai.xenopilot.customer.CsvImportPreviewResponse;
import ai.xenopilot.customer.CsvRecordPreview;
import ai.xenopilot.customer.Customer;
import ai.xenopilot.customer.CustomerRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    public Page<OrderResponse> list(UUID customerId, Pageable pageable) {
        Page<CustomerOrder> orders = customerId == null ? orderRepository.findAll(pageable) : orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = findCustomer(request.customerId());
        return OrderResponse.from(orderRepository.save(new CustomerOrder(customer, request.amount(), request.category(), request.purchaseDate())));
    }

    @Transactional
    public OrderResponse update(UUID id, OrderRequest request) {
        CustomerOrder order = findOrder(id);
        Customer customer = findCustomer(request.customerId());
        order.update(customer, request.amount(), request.category(), request.purchaseDate());
        return OrderResponse.from(order);
    }

    @Transactional
    public void delete(UUID id) {
        orderRepository.delete(findOrder(id));
    }

    public CsvImportPreviewResponse<OrderRequest> previewCsv(MultipartFile file) {
        List<CsvRecordPreview<OrderRequest>> records = parseCsv(file);
        return response(records);
    }

    @Transactional
    public CsvImportPreviewResponse<OrderRequest> importCsv(MultipartFile file) {
        List<CsvRecordPreview<OrderRequest>> records = parseCsv(file);
        orderRepository.saveAll(records.stream().filter(CsvRecordPreview::valid).map(CsvRecordPreview::data).map(request -> new CustomerOrder(findCustomer(request.customerId()), request.amount(), request.category(), request.purchaseDate())).toList());
        return response(records);
    }

    private CsvImportPreviewResponse<OrderRequest> response(List<CsvRecordPreview<OrderRequest>> records) {
        return new CsvImportPreviewResponse<>(records, records.stream().filter(CsvRecordPreview::valid).count(), records.stream().filter(record -> !record.valid()).count());
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private CustomerOrder findOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private List<CsvRecordPreview<OrderRequest>> parseCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().toList();
            List<CsvRecordPreview<OrderRequest>> records = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                List<String> errors = new ArrayList<>();
                OrderRequest request = orderRequest(splitCsv(lines.get(i)), errors);
                records.add(new CsvRecordPreview<>(i + 1, request, errors.isEmpty(), errors));
            }
            return records;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read CSV file");
        }
    }

    private OrderRequest orderRequest(String[] fields, List<String> errors) {
        if (fields.length < 4) {
            errors.add("expected columns: customerId,amount,category,purchaseDate");
            return null;
        }
        UUID customerId = null;
        BigDecimal amount = null;
        LocalDate purchaseDate = null;
        try {
            customerId = UUID.fromString(fields[0]);
            if (!customerRepository.existsById(customerId)) errors.add("customerId does not exist");
        } catch (IllegalArgumentException exception) {
            errors.add("customerId must be a UUID");
        }
        try {
            amount = new BigDecimal(fields[1]);
            if (amount.signum() <= 0) errors.add("amount must be positive");
        } catch (NumberFormatException exception) {
            errors.add("amount must be numeric");
        }
        if (fields[2].isBlank()) errors.add("category is required");
        try {
            purchaseDate = LocalDate.parse(fields[3]);
        } catch (Exception exception) {
            errors.add("purchaseDate must be YYYY-MM-DD");
        }
        return new OrderRequest(customerId, amount, fields[2], purchaseDate);
    }

    private String[] splitCsv(String line) {
        return Arrays.stream(line.split(",", -1)).map(value -> value.replace("\"", "").trim()).toArray(String[]::new);
    }
}
