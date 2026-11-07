package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.PaymentDtos.AddDemoCardRequest;
import br.com.bora.delivery.api.dto.PaymentDtos.DemoCardDto;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.domain.DemoPaymentMethod;
import br.com.bora.delivery.repository.DemoPaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemoCardService {
    private final DemoPaymentMethodRepository repository;

    public DemoCardService(DemoPaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DemoCardDto> list(Long accountId, AccountRole role) {
        requireCustomer(role);
        return repository.findAllByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(DemoCardDto::from).toList();
    }

    @Transactional
    public DemoCardDto add(Long accountId, AccountRole role, AddDemoCardRequest request) {
        requireCustomer(role);
        DemoPaymentMethod method = repository.save(new DemoPaymentMethod(
                accountId, request.brand(), request.lastFour()));
        return DemoCardDto.from(method);
    }

    @Transactional
    public void delete(Long accountId, Long methodId, AccountRole role) {
        requireCustomer(role);
        DemoPaymentMethod method = repository.findByIdAndAccountId(methodId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cartão demonstrativo não encontrado."));
        repository.delete(method);
    }

    private static void requireCustomer(AccountRole role) {
        if (role != AccountRole.CUSTOMER) {
            throw new IllegalArgumentException("Somente contas de cliente podem gerenciar cartões demonstrativos.");
        }
    }
}
