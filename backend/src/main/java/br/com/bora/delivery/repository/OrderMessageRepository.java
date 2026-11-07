package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {
    List<OrderMessage> findByOrderIdOrderBySentAtAsc(Long orderId);
    long countByOrderId(Long orderId);
}
