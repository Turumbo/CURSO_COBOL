package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.ChatDtos.MessageDto;
import br.com.bora.delivery.api.dto.ChatDtos.SendMessageRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/chat/orders/{orderId}/messages")
public class ChatController {
    private final CommunicationService communicationService;

    public ChatController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping
    public List<MessageDto> list(@PathVariable Long orderId,
                                 @AuthenticationPrincipal AppPrincipal principal) {
        return communicationService.listMessages(orderId, principal);
    }

    @PostMapping
    public MessageDto send(@PathVariable Long orderId,
                           @Valid @RequestBody SendMessageRequest request,
                           @AuthenticationPrincipal AppPrincipal principal) {
        return communicationService.sendMessage(orderId, request.message(), principal);
    }
}
