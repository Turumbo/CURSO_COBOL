package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.ReviewDtos.CreateReviewRequest;
import br.com.bora.delivery.api.dto.ReviewDtos.ReviewDto;
import br.com.bora.delivery.security.AppPrincipal;
import br.com.bora.delivery.service.CommunicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final CommunicationService communicationService;

    public ReviewController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody CreateReviewRequest request,
                            @AuthenticationPrincipal AppPrincipal principal) {
        return communicationService.createReview(request, principal);
    }

    @GetMapping("/orders/{orderId}")
    public ReviewDto find(@PathVariable Long orderId, @AuthenticationPrincipal AppPrincipal principal) {
        return communicationService.findReview(orderId, principal);
    }
}
