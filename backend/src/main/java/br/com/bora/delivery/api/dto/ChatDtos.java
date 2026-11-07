package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.domain.OrderMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ChatDtos {
    private ChatDtos() {
    }

    public record SendMessageRequest(@NotBlank @Size(max = 1000) String message) {
    }

    public record MessageDto(Long id, Long orderId, Long senderAccountId, String senderName,
                             AccountRole senderRole, String message, Instant sentAt) {
        public static MessageDto from(OrderMessage message) {
            return new MessageDto(message.getId(), message.getOrderId(), message.getSenderAccountId(),
                    message.getSenderName(), message.getSenderRole(), message.getMessage(), message.getSentAt());
        }
    }
}
