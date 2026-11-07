package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.PaymentDtos.AddDemoCardRequest;
import br.com.bora.delivery.api.dto.PaymentDtos.DemoCardDto;
import br.com.bora.delivery.security.AppPrincipal;
import br.com.bora.delivery.service.DemoCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/auth/me/payment-methods")
public class DemoCardController {
    private final DemoCardService cardService;

    public DemoCardController(DemoCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<DemoCardDto> list(@AuthenticationPrincipal AppPrincipal principal) {
        return cardService.list(principal.id(), principal.role());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoCardDto add(@Valid @RequestBody AddDemoCardRequest request,
                           @AuthenticationPrincipal AppPrincipal principal) {
        return cardService.add(principal.id(), principal.role(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppPrincipal principal) {
        cardService.delete(principal.id(), id, principal.role());
    }
}
