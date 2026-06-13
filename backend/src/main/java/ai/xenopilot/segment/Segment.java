package ai.xenopilot.segment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "segments")
public class Segment {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String filterJson;

    @CreationTimestamp
    private Instant createdAt;

    protected Segment() {
    }

    public Segment(String name, String description, String filterJson) {
        update(name, description, filterJson);
    }

    public void update(String name, String description, String filterJson) {
        this.name = name;
        this.description = description;
        this.filterJson = filterJson;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getFilterJson() { return filterJson; }
    public Instant getCreatedAt() { return createdAt; }
}
