package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.PaymentDtos.FiscalDocumentDto;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.domain.CustomerOrder;
import br.com.bora.delivery.domain.FiscalDocument;
import br.com.bora.delivery.repository.FiscalDocumentRepository;
import br.com.bora.delivery.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MockFiscalDocumentService {
    private static final String NOTICE =
            "Comprovante de demonstração sem validade fiscal. Não é NF-e, NFC-e ou NFS-e.";
    private final FiscalDocumentRepository documentRepository;
    private final OrderRepository orderRepository;

    public MockFiscalDocumentService(FiscalDocumentRepository documentRepository, OrderRepository orderRepository) {
        this.documentRepository = documentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public FiscalDocumentDto createForOrder(Long orderId, Long accountId, AccountRole role) {
        CustomerOrder order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (role == AccountRole.CUSTOMER && !order.getCustomerAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Este pedido pertence a outra conta.");
        }
        FiscalDocument document = documentRepository.findByOrderId(orderId)
                .orElseGet(() -> documentRepository.save(new FiscalDocument(orderId, order.getTotal())));
        return toDto(document);
    }

    private static FiscalDocumentDto toDto(FiscalDocument document) {
        return new FiscalDocumentDto(document.getOrderId(), document.getReference(), document.getDocumentType(),
                document.getAmount(), document.getCreatedAt(), document.isLegallyValid(), NOTICE);
    }
}
