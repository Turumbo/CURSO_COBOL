package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.OrderReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderReviewRepository extends JpaRepository<OrderReview, Long> {
    Optional<OrderReview> findByOrderId(Long orderId);
}
