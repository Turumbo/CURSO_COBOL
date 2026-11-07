package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = {"restaurant", "items"})
    Optional<CustomerOrder> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"restaurant", "items"})
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    List<CustomerOrder> findAllByCustomerAccountIdOrderByCreatedAtDesc(Long customerAccountId);
}
