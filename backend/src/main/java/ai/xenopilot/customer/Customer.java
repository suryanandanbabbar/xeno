package ai.xenopilot.customer;

import ai.xenopilot.order.CustomerOrder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String city;
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.UNSPECIFIED;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrder> orders = new ArrayList<>();

    protected Customer() {
    }

    public Customer(String firstName, String lastName, String email, String phone, String city, Integer age, Gender gender) {
        update(firstName, lastName, email, phone, city, age, gender);
    }

    public void update(String firstName, String lastName, String email, String phone, String city, Integer age, Gender gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email.toLowerCase();
        this.phone = phone;
        this.city = city;
        this.age = age;
        this.gender = gender == null ? Gender.UNSPECIFIED : gender;
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public Integer getAge() { return age; }
    public Gender getGender() { return gender; }
    public Instant getCreatedAt() { return createdAt; }
}
