package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, Long> {
    Optional<FiscalDocument> findByOrderId(Long orderId);
}
