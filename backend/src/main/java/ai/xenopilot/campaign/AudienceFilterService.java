package ai.xenopilot.campaign;

import ai.xenopilot.common.ResourceNotFoundException;
import ai.xenopilot.customer.CustomerRepository;
import ai.xenopilot.customer.CustomerResponse;
import ai.xenopilot.segment.Segment;
import ai.xenopilot.segment.SegmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AudienceFilterService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;

    public AudienceFilterService(NamedParameterJdbcTemplate jdbcTemplate, CustomerRepository customerRepository, SegmentRepository segmentRepository, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.customerRepository = customerRepository;
        this.segmentRepository = segmentRepository;
        this.objectMapper = objectMapper;
    }

    public List<CustomerResponse> matchSegment(UUID segmentId) {
        Segment segment = segmentRepository.findById(segmentId).orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
        return matchFilters(segment.getFilterJson());
    }

    public List<CustomerResponse> matchFilters(String filterJson) {
        List<UUID> ids = matchingCustomerIds(filterJson);
        if (ids.isEmpty()) return List.of();
        return customerRepository.findAllById(ids).stream().map(CustomerResponse::from).toList();
    }

    public long countFilters(String filterJson) {
        return matchingCustomerIds(filterJson).size();
    }

    private List<UUID> matchingCustomerIds(String filterJson) {
        JsonNode root = parse(filterJson);
        StringBuilder sql = new StringBuilder("""
                select c.id
                from customers c
                left join orders o on o.customer_id = c.id
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> where = new ArrayList<>();
        List<String> having = new ArrayList<>();

        if (root.hasNonNull("city") && !root.path("city").asText().isBlank()) {
            where.add("lower(c.city) = lower(:city)");
            params.addValue("city", root.path("city").asText());
        }
        if (root.hasNonNull("category") && !root.path("category").asText().isBlank()) {
            where.add("exists (select 1 from orders ox where ox.customer_id = c.id and lower(ox.category) = lower(:category))");
            params.addValue("category", root.path("category").asText());
        }
        appendNumericCondition(root.path("totalSpend"), "coalesce(sum(o.amount), 0)", "totalSpend", params, having);
        appendNumericCondition(root.path("orderCount"), "count(o.id)", "orderCount", params, having);
        JsonNode lastPurchase = root.path("lastPurchaseDate");
        if (lastPurchase.isObject() && lastPurchase.hasNonNull("value") && !lastPurchase.path("value").asText().isBlank()) {
            having.add("max(o.purchase_date) " + sqlOperator(lastPurchase.path("operator").asText("before")) + " :lastPurchaseDate");
            params.addValue("lastPurchaseDate", Date.valueOf(LocalDate.parse(lastPurchase.path("value").asText())));
        } else if (root.hasNonNull("daysSinceLastPurchase")) {
            LocalDate cutoff = LocalDate.now().minusDays(root.path("daysSinceLastPurchase").asLong());
            having.add("(max(o.purchase_date) is null or max(o.purchase_date) <= :daysSinceLastPurchaseCutoff)");
            params.addValue("daysSinceLastPurchaseCutoff", Date.valueOf(cutoff));
        }
        if (!where.isEmpty()) sql.append(" where ").append(String.join(" and ", where));
        sql.append(" group by c.id");
        if (!having.isEmpty()) sql.append(" having ").append(String.join(" and ", having));
        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

    private void appendNumericCondition(JsonNode node, String expression, String paramName, MapSqlParameterSource params, List<String> having) {
        if (!node.isObject() || !node.hasNonNull("value")) return;
        BigDecimal value = node.path("value").decimalValue();
        having.add(expression + " " + sqlOperator(node.path("operator").asText("gte")) + " :" + paramName);
        params.addValue(paramName, value);
    }

    private String sqlOperator(String operator) {
        return switch (operator) {
            case "gt" -> ">";
            case "lt", "before" -> "<";
            case "lte" -> "<=";
            case "eq" -> "=";
            default -> ">=";
        };
    }

    private JsonNode parse(String filterJson) {
        try {
            return objectMapper.readTree(filterJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Filter JSON is invalid");
        }
    }
}
