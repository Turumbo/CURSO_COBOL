package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.CreateOrderRequest;
import br.com.bora.delivery.api.dto.OrderDto;
import br.com.bora.delivery.api.dto.PaymentDtos.FiscalDocumentDto;
import br.com.bora.delivery.api.dto.PaymentDtos.PaymentTransactionDto;
import br.com.bora.delivery.service.MockFiscalDocumentService;
import br.com.bora.delivery.service.MockPaymentService;
import br.com.bora.delivery.service.OrderService;
import br.com.bora.delivery.security.AppPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final MockPaymentService paymentService;
    private final MockFiscalDocumentService fiscalDocumentService;

    public OrderController(OrderService orderService, MockPaymentService paymentService,
                           MockFiscalDocumentService fiscalDocumentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.fiscalDocumentService = fiscalDocumentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@Valid @RequestBody CreateOrderRequest request,
                           @AuthenticationPrincipal AppPrincipal principal) {
        return orderService.create(request, principal.id(), principal.name());
    }

    @GetMapping("/{id}")
    public OrderDto findById(@PathVariable Long id, @AuthenticationPrincipal AppPrincipal principal) {
        return orderService.findById(id, principal.id(), principal.role());
    }

    @GetMapping
    public List<OrderDto> listMine(@AuthenticationPrincipal AppPrincipal principal) {
        return orderService.listAll(principal.role(), principal.id());
    }

    @GetMapping("/{id}/payment")
    public PaymentTransactionDto payment(@PathVariable Long id, @AuthenticationPrincipal AppPrincipal principal) {
        return paymentService.findForOrder(id, principal.id(), principal.role());
    }

    @PostMapping("/{id}/fiscal-document")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalDocumentDto createFiscalDocument(@PathVariable Long id,
                                                  @AuthenticationPrincipal AppPrincipal principal) {
        return fiscalDocumentService.createForOrder(id, principal.id(), principal.role());
    }
}
