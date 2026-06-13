package ai.xenopilot.customer;

import ai.xenopilot.common.ResourceNotFoundException;
import ai.xenopilot.order.CustomerOrder;
import ai.xenopilot.order.OrderRepository;
import ai.xenopilot.order.OrderResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    // public Page<CustomerResponse> search(String search, Pageable pageable) {
    //     String normalized = search == null || search.isBlank() ? null : search.trim();
    //     return customerRepository.search(normalized, pageable).map(CustomerResponse::from);
    // }

    public CustomerProfileResponse getProfile(UUID id) {
        Customer customer = findCustomer(id);
        List<CustomerOrder> orders = orderRepository.findByCustomerIdOrderByPurchaseDateDesc(id);
        BigDecimal totalSpend = orders.stream().map(CustomerOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long orderCount = orders.size();
        BigDecimal averageOrderValue = orderCount == 0 ? BigDecimal.ZERO : totalSpend.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP);
        LocalDate lastPurchaseDate = orders.stream().map(CustomerOrder::getPurchaseDate).max(LocalDate::compareTo).orElse(null);
        Long daysSinceLastPurchase = lastPurchaseDate == null ? null : ChronoUnit.DAYS.between(lastPurchaseDate, LocalDate.now());
        return new CustomerProfileResponse(CustomerResponse.from(customer), totalSpend, averageOrderValue, orderCount, lastPurchaseDate, daysSinceLastPurchase, orders.stream().map(OrderResponse::from).toList());
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        ensureEmailAvailable(request.email().toLowerCase(), null);
        Customer customer = customerRepository.save(toCustomer(request));
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        ensureEmailAvailable(request.email().toLowerCase(), id);
        customer.update(request.firstName(), request.lastName(), request.email(), request.phone(), request.city(), request.age(), request.gender());
        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
    }

    public CsvImportPreviewResponse<CustomerRequest> previewCsv(MultipartFile file) {
        List<CsvRecordPreview<CustomerRequest>> records = parseCsv(file);
        return new CsvImportPreviewResponse<>(records, records.stream().filter(CsvRecordPreview::valid).count(), records.stream().filter(record -> !record.valid()).count());
    }

    @Transactional
    public CsvImportPreviewResponse<CustomerRequest> importCsv(MultipartFile file) {
        List<CsvRecordPreview<CustomerRequest>> records = parseCsv(file);
        List<Customer> customers = records.stream().filter(CsvRecordPreview::valid).map(CsvRecordPreview::data).map(this::toCustomer).toList();
        customerRepository.saveAll(customers);
        return new CsvImportPreviewResponse<>(records, records.stream().filter(CsvRecordPreview::valid).count(), records.stream().filter(record -> !record.valid()).count());
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private Customer toCustomer(CustomerRequest request) {
        return new Customer(request.firstName(), request.lastName(), request.email(), request.phone(), request.city(), request.age(), request.gender());
    }

    private void ensureEmailAvailable(String email, UUID currentCustomerId) {
        customerRepository.findByEmail(email).ifPresent(existing -> {
            if (currentCustomerId == null || !existing.getId().equals(currentCustomerId)) {
                throw new IllegalArgumentException("Customer email is already registered");
            }
        });
    }

    private List<CsvRecordPreview<CustomerRequest>> parseCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().toList();
            List<CsvRecordPreview<CustomerRequest>> records = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String[] fields = splitCsv(lines.get(i));
                List<String> errors = new ArrayList<>();
                CustomerRequest request = customerRequest(fields, errors);
                if (request != null && customerRepository.existsByEmail(request.email().toLowerCase())) {
                    errors.add("email already exists");
                }
                records.add(new CsvRecordPreview<>(i + 1, request, errors.isEmpty(), errors));
            }
            return records;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read CSV file");
        }
    }

    private CustomerRequest customerRequest(String[] fields, List<String> errors) {
        if (fields.length < 7) {
            errors.add("expected columns: firstName,lastName,email,phone,city,age,gender");
            return null;
        }
        if (fields[0].isBlank()) errors.add("firstName is required");
        if (fields[1].isBlank()) errors.add("lastName is required");
        if (!fields[2].contains("@")) errors.add("valid email is required");
        Integer age = null;
        if (!fields[5].isBlank()) {
            try {
                age = Integer.parseInt(fields[5]);
            } catch (NumberFormatException exception) {
                errors.add("age must be a number");
            }
        }
        Gender gender = Gender.UNSPECIFIED;
        if (!fields[6].isBlank()) {
            try {
                gender = Gender.valueOf(fields[6].trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                errors.add("gender must be MALE, FEMALE, OTHER, or UNSPECIFIED");
            }
        }
        return new CustomerRequest(fields[0].trim(), fields[1].trim(), fields[2].trim(), fields[3].trim(), fields[4].trim(), age, gender);
    }

    private String[] splitCsv(String line) {
        return Arrays.stream(line.split(",", -1)).map(value -> value.replace("\"", "").trim()).toArray(String[]::new);
    }

    public Page<CustomerResponse> search(String search, Pageable pageable) {

    if (search == null || search.isBlank()) {
        return customerRepository.findAll(pageable)
                .map(CustomerResponse::from);
    }

    return customerRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrCityContainingIgnoreCase(
                    search,
                    search,
                    search,
                    search,
                    pageable)
            .map(CustomerResponse::from);
    }
}
