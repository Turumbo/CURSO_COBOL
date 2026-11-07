package br.com.bora.delivery.api.dto;

import java.time.Instant;

public record PromotionDto(String id, String title, String subtitle, String code,
                           String badge, String theme, Instant expiresAt,
                           java.math.BigDecimal maximumDiscount,
                           java.math.BigDecimal percentOff,
                           java.math.BigDecimal fixedDiscount) {
}
