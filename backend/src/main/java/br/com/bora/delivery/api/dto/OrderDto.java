package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.CustomerOrder;
import br.com.bora.delivery.domain.OrderLine;
import br.com.bora.delivery.domain.OrderStatus;
import br.com.bora.delivery.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Arrays;

public record OrderDto(
        Long id,
        String customerName,
        String deliveryAddress,
        double latitude,
        double longitude,
        PaymentMethod paymentMethod,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discount,
        BigDecimal total,
        String promoCode,
        Instant createdAt,
        Instant statusUpdatedAt,
        RestaurantSummary restaurant,
        List<OrderItemDto> items) {

    public static OrderDto from(CustomerOrder order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getLatitude(),
                order.getLongitude(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getDiscount(),
                order.getTotal(),
                order.getPromoCode(),
                order.getCreatedAt(),
                order.getStatusUpdatedAt(),
                new RestaurantSummary(order.getRestaurant().getId(), order.getRestaurant().getName(),
                        order.getRestaurant().getDeliveryMinutes(), order.getRestaurant().getLatitude(),
                        order.getRestaurant().getLongitude()),
                order.getItems().stream().map(OrderItemDto::from).toList());
    }

    public record RestaurantSummary(Long id, String name, int deliveryMinutes, double latitude, double longitude) {
    }

    public record OrderItemDto(Long id, String name, BigDecimal unitPrice, int quantity, List<String> customizations) {
        public static OrderItemDto from(OrderLine item) {
            List<String> options = item.getCustomizations() == null || item.getCustomizations().isBlank()
                    ? List.of() : Arrays.asList(item.getCustomizations().split(", "));
            return new OrderItemDto(item.getId(), item.getItemName(), item.getUnitPrice(),
                    item.getQuantity(), options);
        }
    }
}
