package ai.xenopilot.customer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmail(String email);
    Optional<Customer> findByEmail(String email);

    @Query("""
            select c from Customer c
            where :search is null
               or lower(c.firstName) like lower(concat('%', :search, '%'))
               or lower(c.lastName) like lower(concat('%', :search, '%'))
               or lower(c.email) like lower(concat('%', :search, '%'))
               or lower(c.city) like lower(concat('%', :search, '%'))
            """)
    Page<Customer> search(@Param("search") String search, Pageable pageable);
    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrCityContainingIgnoreCase(
        String firstName,
        String lastName,
        String email,
        String city,
        Pageable pageable
);
}
