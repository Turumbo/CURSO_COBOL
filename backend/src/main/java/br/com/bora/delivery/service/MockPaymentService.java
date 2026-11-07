package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.PaymentDtos.PaymentTransactionDto;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.domain.CustomerOrder;
import br.com.bora.delivery.domain.PaymentTransaction;
import br.com.bora.delivery.repository.OrderRepository;
import br.com.bora.delivery.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MockPaymentService {
    private static final String NOTICE =
            "Simulação local: nenhuma cobrança, transferência ou autorização financeira foi realizada.";
    private final PaymentTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    public MockPaymentService(PaymentTransactionRepository transactionRepository, OrderRepository orderRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void recordSimulation(CustomerOrder order) {
        transactionRepository.save(new PaymentTransaction(
                order.getId(),
                order.getPaymentMethod(),
                "MOCK-" + UUID.randomUUID(),
                order.getTotal(),
                NOTICE));
    }

    @Transactional(readOnly = true)
    public PaymentTransactionDto findForOrder(Long orderId, Long accountId, AccountRole role) {
        CustomerOrder order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        ensureCanAccess(order, accountId, role);
        return transactionRepository.findByOrderId(orderId)
                .map(PaymentTransactionDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Transação demonstrativa não encontrada."));
    }

    private static void ensureCanAccess(CustomerOrder order, Long accountId, AccountRole role) {
        if (role == AccountRole.CUSTOMER && !order.getCustomerAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Este pedido pertence a outra conta.");
        }
    }
}
