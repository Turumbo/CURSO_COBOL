package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Long restaurantId,
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Size(max = 250) String deliveryAddress,
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 40) String promoCode,
        @NotEmpty @Size(max = 50) List<@Valid OrderItemRequest> items) {

    public record OrderItemRequest(
            @NotNull Long menuItemId,
            @Min(1) @Max(20) int quantity,
            @Size(max = 5) List<@Size(max = 60) String> customizations) {
        public OrderItemRequest(Long menuItemId, int quantity) {
            this(menuItemId, quantity, new ArrayList<>());
        }
    }
}
