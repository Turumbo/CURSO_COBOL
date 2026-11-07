package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.PromotionDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.math.BigDecimal;

@Service
public class PromotionService {
    public List<PromotionDto> banners() {
        Instant expiry = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1)
                .atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant();
        return List.of(
                new PromotionDto("welcome", "Sua próxima descoberta", "10% OFF no primeiro pedido",
                        "BEMVINDO10", "ATÉ R$ 10", "coral", expiry,
                        new BigDecimal("10.00"), new BigDecimal("0.10"), BigDecimal.ZERO),
                new PromotionDto("lunch", "Hora de dar uma pausa", "Entrega especial no almoço de hoje",
                        "BORAALMOCO", "R$ 5 OFF", "gold", expiry,
                        BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5.00")),
                new PromotionDto("surprise", "Uma surpresa para você", "Promoção relâmpago · por tempo limitado",
                        "SURPRESA15", "15% SURPRESA", "violet", Instant.now().plusSeconds(3600),
                        new BigDecimal("25.00"), new BigDecimal("0.15"), BigDecimal.ZERO)
        );
    }

    public PromotionDto currentSurprise() {
        return banners().get(Math.floorMod(LocalDate.now(ZoneId.of("America/Sao_Paulo")).getDayOfYear(), 3));
    }

    public PromotionValidation validate(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        return banners().stream()
                .filter(promotion -> promotion.code().equals(code))
                .findFirst()
                .map(promotion -> new PromotionValidation(true, code, "Cupom aplicado!",
                        promotion.maximumDiscount(), promotion.percentOff(), promotion.fixedDiscount()))
                .orElse(new PromotionValidation(false, code, "Cupom inválido ou expirado.",
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public record PromotionValidation(boolean valid, String code, String message,
                                      BigDecimal maximumDiscount, BigDecimal percentOff,
                                      BigDecimal fixedDiscount) {
    }
}
