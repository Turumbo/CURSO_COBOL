package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
