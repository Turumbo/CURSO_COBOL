package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.DemoPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemoPaymentMethodRepository extends JpaRepository<DemoPaymentMethod, Long> {
    List<DemoPaymentMethod> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);
    Optional<DemoPaymentMethod> findByIdAndAccountId(Long id, Long accountId);
}
